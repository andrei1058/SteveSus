package com.andrei1058.stevesus.connector;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.SettingsManagerBuilder;
import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.command.CommonCmdManager;
import com.andrei1058.stevesus.common.database.DatabaseManager;
import com.andrei1058.stevesus.common.hook.HookManager;
import com.andrei1058.stevesus.common.party.PartyManager;
import com.andrei1058.stevesus.common.selector.SelectorManager;
import com.andrei1058.stevesus.common.stats.StatsManager;
import com.andrei1058.stevesus.connector.arena.ArenaManager;
import com.andrei1058.stevesus.connector.common.ConnectorCommonProvider;
import com.andrei1058.stevesus.connector.config.ConnectorConfig;
import com.andrei1058.stevesus.connector.hook.papi.PlaceholderAdditions;
import com.andrei1058.stevesus.connector.language.LanguageManager;
import com.andrei1058.stevesus.connector.listener.AdminJoinListener;
import com.andrei1058.stevesus.connector.socket.SocketManager;
import com.andrei1058.stevesus.connector.socket.slave.SlaveSocketListener;
import com.andrei1058.stevesus.common.api.CommonProvider;
import com.andrei1058.stevesus.common.api.locale.CommonLocaleManager;
import com.andrei1058.stevesus.common.api.packet.CommunicationHandler;
import com.andrei1058.stevesus.connector.api.ConnectorAPI;
import com.andrei1058.spigot.versionsupport.ChatSupport;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.dependency.SoftDependency;
import org.bukkit.plugin.java.annotation.dependency.SoftDependsOn;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.Website;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import java.io.File;

@Plugin(name = "SteveSus-Connector", version = "1.0-alpha")
@Description(value = "A plugin that helps you join Among Us arenas.")
@Author(value = "andrei1058")
@Website(value = "www.andrei1058.com")
@ApiVersion(value = ApiVersion.Target.v1_13)
@Command(name = "au")
@SoftDependsOn({@SoftDependency("Vault"), @SoftDependency("PlaceholderAPI")})
public class SteveSusConnector extends JavaPlugin implements ConnectorAPI {

    public static final byte SERVER_VERSION = Byte.parseByte(Bukkit.getServer().getClass().getName().split("\\.")[3].split("_")[1]);
    public static SteveSusConnector INSTANCE;
    private static TaskChainFactory taskChainFactory;
    private static ChatSupport chatSupport;
    private static SettingsManager config;
    private static String serverName;

    public static SteveSusConnector getInstance() {
        return INSTANCE;
    }

    @Override
    public void onLoad() {
        INSTANCE = this;

        if (SERVER_VERSION < 12) {
            getLogger().severe("Sorry but your server version is not supported!");
            getLogger().severe("Please use 1.12+. We won't support older versions!");
            return;
        }

        // create plugin data folder
        if (!INSTANCE.getDataFolder().exists()) {
            if (!INSTANCE.getDataFolder().mkdir()) {
                this.getLogger().severe("Could not create plugin data folder!");
            }
        }

        config = SettingsManagerBuilder.withYamlFile(new File(getInstance().getDataFolder(), "config.yml"))
                .configurationData(ConnectorConfig.class).useDefaultMigrationService().create();
        serverName = config.getProperty(ConnectorConfig.SERVER_DISPLAY_NAME);

        // Initialize Language Manager
        LanguageManager.onLoad();

        // Initialize Database & change database config location if needed
        DatabaseManager.onLoad(this, getConnectorConfig().getProperty(ConnectorConfig.DATABASE_PATH));
    }

    @Override
    public void onEnable() {
        if (SERVER_VERSION < 12) {
            getLogger().severe("Sorry but your server version is not supported!");
            getLogger().severe("Please use 1.12+. We won't support older versions!");
            Bukkit.getPluginManager().disablePlugin(INSTANCE);
            return;
        }

        // Load chat util
        chatSupport = ChatSupport.SupportBuilder.load();
        if (chatSupport == null) {
            getLogger().severe("Server version not supported");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        //

        // Initialize task chain
        taskChainFactory = BukkitTaskChainFactory.create(this);

        // Initialize Common Manager
        CommonManager.init(ConnectorCommonProvider.getInstance(), this);

        // Initialize Commands after commons
        CommonCmdManager.onEnable(null);
        CommonManager.getINSTANCE().getCommonProvider().setMainCommand(CommonCmdManager.getINSTANCE().getMainCmd());

        // Initialize Hooks: Soft-Depend -> Vault, PlaceholderAPI. Before arena manager.
        HookManager.onEnable(true, "stevesus", new PlaceholderAdditions());

        // Initialize Stats Manager
        File statsDir = this.getDataFolder();
        String customStatsPath;
        if (!(customStatsPath = getConnectorConfig().getProperty(ConnectorConfig.STATS_PATH)).isEmpty()) {
            File newPath = new File(customStatsPath);
            if (newPath.isDirectory()) {
                statsDir = newPath;
                this.getLogger().info("Set stats configuration path to: " + statsDir);
            } else {
                this.getLogger().warning("Tried to set stats configuration path to: " + statsDir + " but it does not seem like a directory.");
            }
        }
        StatsManager.init(statsDir);

        // Initialize Arena Manager before commons
        ArenaManager.init();

        // Call this before initializing commons
        LanguageManager.onEnable();

        // load arena selector if enabled & change config location if needed
        if (CommonManager.getINSTANCE().getCommonProvider().isEnableArenaSelector()) {
            SelectorManager.init(this, getConnectorConfig().getProperty(ConnectorConfig.SELECTOR_PATH));
        }

        // Register channels before accepting new arenas because they will send data on connect
        SocketManager.init();

        // Initialize Party Manager
        PartyManager.init(getConnectorConfig().getProperty(ConnectorConfig.PARTIES_ENABLE), getConnectorConfig().getProperty(ConnectorConfig.PARTIES_ENABLE_COMMAND)
                , getConnectorConfig().getProperty(ConnectorConfig.PARTIES_DISBAND_AFTER), getConnectorConfig().getProperty(ConnectorConfig.PARTIES_SIZE_LIMIT));


        // Initialize Metrics
        Metrics metrics = new Metrics(this, 9089);
        metrics.addCustomChart(new Metrics.SimplePie("default_language", () ->
                ConnectorCommonProvider.getInstance().getCommonLocaleManager().getDefaultLocale().getIsoCode()));
        //todo add more metrics
        //

        // some listeners
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Bukkit.getPluginManager().registerEvents(new AdminJoinListener(), this);
    }

    @Override
    public void onDisable() {
        SlaveSocketListener.onDisable();
    }

    public static <T> TaskChain<T> newChain() {
        return taskChainFactory.newChain();
    }

    @SuppressWarnings("unused")
    public static <T> TaskChain<T> newSharedChain(String name) {
        return taskChainFactory.newSharedChain(name);
    }

    @Override
    public CommonProvider getCommonProvider() {
        return ConnectorCommonProvider.getInstance();
    }

    @Override
    public CommonLocaleManager getLocaleManager() {
        return CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager();
    }

    @Override
    public CommunicationHandler getPacketsHandler() {
        return CommonManager.getINSTANCE().getCommonProvider().getPacketsHandler();
    }

    public static void debug(String msg) {
        if (ConnectorCommonProvider.getInstance().isDebuggingLogs()) {
            SteveSusConnector.getInstance().getLogger().info(ChatColor.AQUA + "[DEBUG] " + msg);
        }
    }

    public static SettingsManager getConnectorConfig() {
        return config;
    }

    public static String getServerName() {
        return serverName;
    }
}
