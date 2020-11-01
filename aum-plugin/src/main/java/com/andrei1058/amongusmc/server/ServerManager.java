package com.andrei1058.amongusmc.server;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.SettingsManagerBuilder;
import com.andrei1058.amongusmc.AmongUsMc;
import com.andrei1058.amongusmc.api.server.DisconnectHandler;
import com.andrei1058.amongusmc.api.server.ServerType;
import com.andrei1058.amongusmc.arena.listener.*;
import com.andrei1058.amongusmc.arena.listener.spectator.SpectatorListener;
import com.andrei1058.amongusmc.commanditem.CommandItemListener;
import com.andrei1058.amongusmc.config.MainConfig;
import com.andrei1058.amongusmc.server.bungee.CacheCleanerTask;
import com.andrei1058.amongusmc.server.bungee.JoinQuitListenerBungee;
import com.andrei1058.amongusmc.server.bungee.channel.PlayerJoinChannel;
import com.andrei1058.amongusmc.server.bungee.remote.ArenaUpdateListener;
import com.andrei1058.amongusmc.server.bungee.remote.RemoteLobby;
import com.andrei1058.amongusmc.server.bungee.remote.task.CheckNewLobbiesTask;
import com.andrei1058.amongusmc.server.bungee.remote.task.ServerPingTask;
import com.andrei1058.amongusmc.server.bungeelegacy.JoinQuitListenerBungeeLegacy;
import com.andrei1058.amongusmc.server.common.JoinCommonListener;
import com.andrei1058.amongusmc.server.common.PlayerChatListener;
import com.andrei1058.amongusmc.server.common.ServerQuitListener;
import com.andrei1058.amongusmc.server.disconnect.InternalDisconnectHandler;
import com.andrei1058.amongusmc.server.multiarena.listener.JoinQuitListenerMultiArena;
import com.andrei1058.amongusmc.server.multiarena.listener.LobbyProtectionListener;
import com.andrei1058.amoungusmc.common.api.packet.DefaultChannel;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.io.File;

public class ServerManager {

    private static ServerManager INSTANCE;
    private final ServerType serverType;
    private final SettingsManager config;
    private static int cacheCleanerTaskId;
    private static int checkNewLobbiesTaskId;
    private static int serverPingTaskId;
    private DisconnectHandler disconnectHandler;
    private boolean debuggingLogs = true;
    private String serverName;

    private ServerManager() {
        if (!AmongUsMc.getInstance().getDataFolder().exists()) {
            //noinspection ResultOfMethodCallIgnored
            AmongUsMc.getInstance().getDataFolder().mkdir();
        }
        config = SettingsManagerBuilder.withYamlFile(new File(AmongUsMc.getInstance().getDataFolder(), "config.yml")).configurationData(MainConfig.class).useDefaultMigrationService().create();
        serverType = config.getProperty(MainConfig.SERVER_TYPE);
        serverName = config.getProperty(MainConfig.SERVER_DISPLAY_NAME);
    }

    public static void onLoad() {
        if (INSTANCE == null) {
            INSTANCE = new ServerManager();
        }
    }

    public static void onEnable() {
        if (INSTANCE == null) return;
        INSTANCE.config.reload();

        // set default player removal adapter
        getINSTANCE().setDisconnectHandler(new InternalDisconnectHandler());

        // Handle Bungee Mode
        if (getINSTANCE().getServerType() == ServerType.BUNGEE) {

            // Register Bungee Mode Related Listener
            for (Listener listener : new Listener[]{new JoinQuitListenerBungee(), new ArenaUpdateListener()}) {
                Bukkit.getPluginManager().registerEvents(listener, AmongUsMc.getInstance());
            }

            // Start cache (remote lobby related) cleaner task
            cacheCleanerTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(AmongUsMc.getInstance(), new CacheCleanerTask(), 20L, 20L).getTaskId();
            // Initialize check new lobbies task
            checkNewLobbiesTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(AmongUsMc.getInstance(), new CheckNewLobbiesTask(), 20L, INSTANCE.getConfig().getProperty(MainConfig.BUNGEE_AUTO_SCALE_UPDATE_LOBBIES_INTERVAL) * 20).getTaskId();
            // Initialize arena life check task
            serverPingTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(AmongUsMc.getInstance(), new ServerPingTask(), 40L, INSTANCE.getConfig().getProperty(MainConfig.BUNGEE_AUTO_SCALE_PING_LOBBIES_INTERVAL) * 20).getTaskId();
            // Connect to remote lobbies
            INSTANCE.getConfig().getProperty(MainConfig.BUNGEE_AUTO_SCALE_LOBBY_SOCKETS).forEach(RemoteLobby::new);

            // register packet listeners
            AmongUsMc.newChain().delay(10).sync(() -> {
                if (!AmongUsMc.getInstance().getPacketsHandler().registerIncomingPacketChannel(DefaultChannel.PLAYER_JOIN_CHANNEL.toString(), new PlayerJoinChannel())) {
                    AmongUsMc.getInstance().getLogger().severe("Could not register remote packet channel: " + DefaultChannel.PLAYER_JOIN_CHANNEL.toString());
                }
            }).execute();

        } else if (getINSTANCE().getServerType() == ServerType.MULTI_ARENA) {

            // Handle Multi Arena
            // Register Multi Arena Mode Related Listener
            for (Listener listener : new Listener[]{new JoinQuitListenerMultiArena()}) {
                Bukkit.getPluginManager().registerEvents(listener, AmongUsMc.getInstance());
            }

            // Register Lobby listener
            LobbyProtectionListener.init(false);

        } else {

            // Handle Bungee Legacy
            for (Listener listener : new Listener[]{new JoinQuitListenerBungeeLegacy()}) {
                Bukkit.getPluginManager().registerEvents(listener, AmongUsMc.getInstance());
            }
        }

        // Common listeners
        for (Listener listener : new Listener[]{new ServerQuitListener(), new JoinCommonListener(), new PlayerChatListener(), new CommandItemListener(),
                new DropPickListener(), new BreakPlaceListener(), new RestartArenaListener(), new DamageListener(), new CreatureSpawnListener(),
                new SpectatorListener()}) {
            Bukkit.getPluginManager().registerEvents(listener, AmongUsMc.getInstance());
        }

        if (getINSTANCE().getServerType() != ServerType.BUNGEE_LEGACY) {
            // register listener that handles active arenas
            Bukkit.getPluginManager().registerEvents(new CloneArenaListener(), AmongUsMc.getInstance());
        }


        // Register bungee channel
        AmongUsMc.getInstance().getServer().getMessenger().registerOutgoingPluginChannel(AmongUsMc.getInstance(), "BungeeCord");
        Bukkit.getMessenger().registerIncomingPluginChannel(AmongUsMc.getInstance(), "Return", new PluginChannelListener());
    }

    public static void onDisable() {
        if (getINSTANCE().getServerType() == ServerType.BUNGEE) {
            long startTime = System.currentTimeMillis();
            Bukkit.getScheduler().cancelTask(cacheCleanerTaskId);
            Bukkit.getScheduler().cancelTask(checkNewLobbiesTaskId);
            Bukkit.getScheduler().cancelTask(serverPingTaskId);
            RemoteLobby.getSockets().values().forEach(RemoteLobby::close);
            AmongUsMc.debug("Took " + (System.currentTimeMillis() - startTime) + "ms to disable " + ServerManager.class.getSimpleName() + ".");
        }
    }

    public SettingsManager getConfig() {
        return config;
    }

    public ServerType getServerType() {
        return serverType;
    }

    public static ServerManager getINSTANCE() {
        return INSTANCE;
    }

    public DisconnectHandler getDisconnectHandler() {
        return disconnectHandler;
    }

    public void setDisconnectHandler(DisconnectHandler disconnectHandler) {
        AmongUsMc.getInstance().getLogger().info("Using DisconnectHandler: " + disconnectHandler.getName());
        this.disconnectHandler = disconnectHandler;
    }

    public void setDebuggingLogs(boolean debuggingLogs) {
        this.debuggingLogs = debuggingLogs;
    }

    public boolean isDebuggingLogs() {
        return debuggingLogs;
    }

    public String getServerName() {
        return serverName;
    }
}
