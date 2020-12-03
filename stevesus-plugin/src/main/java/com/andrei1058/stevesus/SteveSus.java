package com.andrei1058.stevesus;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import com.andrei1058.spigot.commandlib.fast.FastRootCommand;
import com.andrei1058.spigot.versionsupport.ChatSupport;
import com.andrei1058.stevesus.api.SteveSusAPI;
import com.andrei1058.stevesus.api.locale.LocaleManager;
import com.andrei1058.stevesus.api.prevention.PreventionHandler;
import com.andrei1058.stevesus.api.server.DisconnectHandler;
import com.andrei1058.stevesus.api.server.GameSound;
import com.andrei1058.stevesus.api.server.PluginPermission;
import com.andrei1058.stevesus.api.server.ServerType;
import com.andrei1058.stevesus.api.setup.SetupHandler;
import com.andrei1058.stevesus.arena.ArenaManager;
import com.andrei1058.stevesus.arena.meeting.VoteGUIManager;
import com.andrei1058.stevesus.command.SlaveCommandManager;
import com.andrei1058.stevesus.command.filter.CommandFilter;
import com.andrei1058.stevesus.commanditem.CommandItemsManager;
import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.api.CommonProvider;
import com.andrei1058.stevesus.common.api.packet.CommunicationHandler;
import com.andrei1058.stevesus.common.command.CommonCmdManager;
import com.andrei1058.stevesus.common.database.DatabaseManager;
import com.andrei1058.stevesus.common.hook.HookManager;
import com.andrei1058.stevesus.common.party.PartyManager;
import com.andrei1058.stevesus.common.selector.SelectorManager;
import com.andrei1058.stevesus.common.stats.StatsManager;
import com.andrei1058.stevesus.config.MainConfig;
import com.andrei1058.stevesus.hook.corpse.CorpseManager;
import com.andrei1058.stevesus.hook.glowing.GlowingManager;
import com.andrei1058.stevesus.hook.packetlistener.PacketListenerHook;
import com.andrei1058.stevesus.hook.papi.PlaceholderAdditions;
import com.andrei1058.stevesus.language.LanguageManager;
import com.andrei1058.stevesus.prevention.PreventionManager;
import com.andrei1058.stevesus.server.ServerCommonProvider;
import com.andrei1058.stevesus.server.ServerManager;
import com.andrei1058.stevesus.setup.SetupManager;
import com.andrei1058.stevesus.sidebar.GameSidebarManager;
import com.andrei1058.stevesus.teleporter.TeleporterManager;
import com.andrei1058.stevesus.worldmanager.WorldManager;
import com.andrei1058.stevesus.worldmanager.generator.VoidChunkGenerator;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.ServicePriority;
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

@Plugin(name = "SteveSus", version = "1.1.4-beta")
@Description(value = "A murder mystery mini-game")
@Author(value = "andrei1058")
@Website(value = "www.andrei1058.com")
@ApiVersion(value = ApiVersion.Target.v1_13)
@Command(name = "ss")
@SoftDependsOn({@SoftDependency("Vault"), @SoftDependency("PlaceholderAPI"), @SoftDependency("CorpseReborn"), @SoftDependency("GlowAPI"), @SoftDependency("PacketListenerAPI")})
public class SteveSus extends JavaPlugin implements SteveSusAPI {

    private static SteveSus INSTANCE;
    private static TaskChainFactory taskChainFactory;
    public static final byte SERVER_VERSION = Byte.parseByte(Bukkit.getServer().getClass().getName().split("\\.")[3].split("_")[1]);

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

        // Initialize Server Manager
        ServerManager.onLoad();

        // Initialize Language Manager
        LanguageManager.onLoad();

        // Initialize Database & change database config location if needed
        DatabaseManager.onLoad(this, ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.DATABASE_PATH));

        // Initialize World Manager
        WorldManager.onLoad();
    }

    @Override
    public void onEnable() {

        if (SERVER_VERSION < 12) {
            getLogger().severe("Sorry but your server version is not supported!");
            getLogger().severe("Please use 1.12+. We won't support older versions!");
            Bukkit.getPluginManager().disablePlugin(INSTANCE);
            return;
        }

        // Initialize API
        Bukkit.getServicesManager().register(SteveSusAPI.class, getInstance(), this, ServicePriority.Normal);

        // Initialize task chain
        taskChainFactory = BukkitTaskChainFactory.create(this);

        ServerManager.onEnable();

        // Load chat util
        ChatSupport chatSupport = ChatSupport.SupportBuilder.load();
        if (chatSupport == null) {
            getLogger().severe("Server version not supported");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        //

        // Initialize Common Manager before arena manager
        CommonManager.init(ServerCommonProvider.getInstance(), this);

        // Initialize Commands after commons
        CommonCmdManager.onEnable(new SlaveCommandManager());
        CommonManager.getINSTANCE().getCommonProvider().setMainCommand(CommonCmdManager.getINSTANCE().getMainCmd());

        // Initialize Server Setup Manager after commons
        SetupManager.init();

        // Call this after initializing commons
        LanguageManager.onEnable();

        // Hooks: Vault etc. Before Arena manager.
        HookManager.onEnable(true, "stevesus", new PlaceholderAdditions());

        // Initialize Arena Manager after commons
        ArenaManager.onEnable();

        // load arena selector if enabled & change config location if needed
        if (CommonManager.getINSTANCE().getCommonProvider().isEnableArenaSelector()) {
            SelectorManager.init(this, ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.SELECTOR_PATH));
        }

        // Initialize Stats Manager
        File statsDir = this.getDataFolder();
        String customStatsPath;
        if (!(customStatsPath = ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.STATS_PATH)).isEmpty()) {
            File newPath = new File(customStatsPath);
            if (newPath.isDirectory()) {
                statsDir = newPath;
                this.getLogger().info("Set stats configuration path to: " + statsDir);
            } else {
                this.getLogger().warning("Tried to set stats configuration path to: " + statsDir + " but it does not seem like a directory.");
            }
        }
        StatsManager.init(statsDir);

        // Initialize Party Manager
        PartyManager.init(ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.PARTIES_ENABLE), ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.PARTIES_ENABLE_COMMAND)
                , ServerManager.getINSTANCE().getServerType() == ServerType.MULTI_ARENA ? 0 : ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.PARTIES_DISBAND_AFTER),
                ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.PARTIES_SIZE_LIMIT));

        // Initialize Metrics
        Metrics metrics = new Metrics(this, 9016);
        metrics.addCustomChart(new Metrics.SimplePie("default_language", () ->
                LanguageManager.getINSTANCE().getDefaultLocale().getIsoCode()));
        //todo add more metrics
        //

        // Register permissions
        PluginPermission.init();

        // Register command filters
        CommandFilter.init();

        // Initialize game sounds
        File soundsDir = this.getDataFolder();
        String customSoundsPath;
        if (!(customSoundsPath = ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.SOUNDS_PATH)).isEmpty()) {
            File newPath = new File(customSoundsPath);
            if (newPath.isDirectory()) {
                soundsDir = newPath;
                this.getLogger().info("Set sounds configuration path to: " + soundsDir);
            } else {
                this.getLogger().warning("Tried to set sounds configuration path to: " + soundsDir + " but it does not seem like a directory.");
            }
        }
        GameSound.init(soundsDir);
        //

        // Initialize command items
        CommandItemsManager.init();

        // Initialize teleporter (spectator)
        File teleporterDir = this.getDataFolder();
        String customTeleporterDir;
        if (!(customTeleporterDir = ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.TELEPORTER_PATH)).isEmpty()) {
            File newPath = new File(customTeleporterDir);
            if (newPath.isDirectory()) {
                teleporterDir = newPath;
                this.getLogger().info("Set teleporter configuration path to: " + teleporterDir);
            } else {
                this.getLogger().warning("Tried to set teleporter configuration path to: " + teleporterDir + " but it does not seem like a directory.");
            }
        }
        TeleporterManager.init(teleporterDir);

        // Initialize Abuse Prevention
        PreventionManager.onEnable();

        // Initialize Scoreboard Manager
        try {
            GameSidebarManager.onEnable();
        } catch (InstantiationException e) {
            this.getLogger().severe("Could not initialize Sidebar Manager. Server version not supported!");
            Bukkit.getPluginManager().disablePlugin(INSTANCE);
        }

        // Initialize Vote GUI Manager
        File directory = this.getDataFolder();
        String customDir;
        if (!(customDir = ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.EXCLUSION_PATH)).isEmpty()) {
            File newPath = new File(customDir);
            if (newPath.isDirectory()) {
                directory = newPath;
                this.getLogger().info("Set exclusion vote configuration path to: " + directory);
            } else {
                this.getLogger().warning("Tried to set exclusion vote configuration path to: " + directory + " but it does not seem like a directory.");
            }
        }
        VoteGUIManager.init(directory);

        // corpse manager
        CorpseManager.init();

        // glowing manager
        GlowingManager.init();

        // Initialize packet listener hook
        PacketListenerHook.init();
    }

    @Override
    public void onDisable() {
        long startTime = System.currentTimeMillis();
        DatabaseManager.onDisable();
        ServerManager.onDisable();
        ArenaManager.onDisable();
        Bukkit.getServicesManager().unregister(SteveSusAPI.class);
        SteveSus.debug("Took " + (System.currentTimeMillis() - startTime) + "ms to disable this plugin.");
    }

    public static void debug(String msg) {
        if (ServerManager.getINSTANCE().isDebuggingLogs()) {
            SteveSus.getInstance().getLogger().info(ChatColor.AQUA + "[DEBUG] " + msg);
        }
    }

    public SetupHandler getSetupHandler() {
        return SetupManager.getINSTANCE();
    }

    @Override
    public CommonProvider getCommonProvider() {
        return ServerCommonProvider.getInstance();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public FastRootCommand getMainCommand() {
        return CommonCmdManager.getINSTANCE().getMainCmd();
    }

    @Override
    public CommunicationHandler getPacketsHandler() {
        return CommonManager.getINSTANCE().getCommonProvider().getPacketsHandler();
    }

    @Override
    public DisconnectHandler getDisconnectHandler() {
        return ServerManager.getINSTANCE().getDisconnectHandler();
    }

    @Override
    public void setDisconnectHandler(DisconnectHandler disconnectHandler) {
        ServerManager.getINSTANCE().setDisconnectHandler(disconnectHandler);
    }

    @Override
    public PreventionHandler getPreventionHandler() {
        return PreventionManager.getInstance();
    }

    @Override
    public com.andrei1058.stevesus.api.arena.ArenaHandler getArenaHandler() {
        return ArenaManager.getINSTANCE();
    }

    @Override
    public LocaleManager getLocaleHandler() {
        return LanguageManager.getINSTANCE();
    }

    public static SteveSus getInstance() {
        return INSTANCE;
    }

    public static <T> TaskChain<T> newChain() {
        return taskChainFactory.newChain();
    }

    @SuppressWarnings("unused")
    public static <T> TaskChain<T> newSharedChain(String name) {
        return taskChainFactory.newSharedChain(name);
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new VoidChunkGenerator();
    }
}
