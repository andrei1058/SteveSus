package dev.andrei1058.game.server;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.SettingsManagerBuilder;
import com.andrei1058.hologramapi.HologramAPI;
import com.andrei1058.spigot.versionsupport.ParticleSupport;
import com.andrei1058.spigot.versionsupport.PlayerNPCSupport;
import dev.andrei1058.game.SteveSus;
import dev.andrei1058.game.api.server.DisconnectHandler;
import dev.andrei1058.game.api.server.ServerType;
import com.andrei1058.stevesus.arena.listener.*;
import dev.andrei1058.game.arena.listener.spectator.SpectatorListener;
import dev.andrei1058.game.arena.sabotage.listener.SabotageListener;
import dev.andrei1058.game.commanditem.CommandItemListener;
import dev.andrei1058.game.arena.listener.*;
import dev.andrei1058.game.common.api.packet.DefaultChannel;
import dev.andrei1058.game.config.MainConfig;
import dev.andrei1058.game.server.bungee.CacheCleanerTask;
import dev.andrei1058.game.server.bungee.JoinQuitListenerBungee;
import dev.andrei1058.game.server.bungee.channel.PlayerJoinChannel;
import dev.andrei1058.game.server.bungee.remote.ArenaUpdateListener;
import dev.andrei1058.game.server.bungee.remote.RemoteLobby;
import dev.andrei1058.game.server.bungee.remote.task.CheckNewLobbiesTask;
import dev.andrei1058.game.server.bungee.remote.task.ServerPingTask;
import dev.andrei1058.game.server.bungeelegacy.JoinQuitListenerBungeeLegacy;
import dev.andrei1058.game.server.common.JoinCommonListener;
import dev.andrei1058.game.server.common.PlayerChatListener;
import dev.andrei1058.game.server.common.ServerQuitListener;
import dev.andrei1058.game.server.disconnect.InternalDisconnectHandler;
import dev.andrei1058.game.server.multiarena.listener.JoinQuitListenerMultiArena;
import dev.andrei1058.game.server.multiarena.listener.LobbyProtectionListener;
import dev.andrei1058.game.stats.StatsGainListener;
import dev.andrei1058.game.worldmanager.MapLoadListener;
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
    private final String serverName;
    private HologramAPI hologramAPI;
    private PlayerNPCSupport playerNPCSupport;
    private static ParticleSupport particleSupport;

    private ServerManager() {
        if (!SteveSus.getInstance().getDataFolder().exists()) {
            //noinspection ResultOfMethodCallIgnored
            SteveSus.getInstance().getDataFolder().mkdir();
        }
        config = SettingsManagerBuilder.withYamlFile(new File(SteveSus.getInstance().getDataFolder(), "config.yml")).configurationData(MainConfig.class).useDefaultMigrationService().create();
        serverType = config.getProperty(MainConfig.SERVER_TYPE);
        serverName = config.getProperty(MainConfig.SERVER_DISPLAY_NAME);
    }

    public static void onLoad() {
        if (INSTANCE == null) {
            INSTANCE = new ServerManager();
        }
    }

    @SuppressWarnings("InstantiationOfUtilityClass")
    public static void onEnable() {
        if (INSTANCE == null) return;
        INSTANCE.config.reload();

        // register hologram api
        try {
            INSTANCE.hologramAPI = new HologramAPI(SteveSus.getInstance());
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        particleSupport = ParticleSupport.SupportBuilder.load();

        // set default player removal adapter
        getINSTANCE().setDisconnectHandler(new InternalDisconnectHandler());

        // Handle Bungee Mode
        if (getINSTANCE().getServerType() == ServerType.BUNGEE) {

            // Register Bungee Mode Related Listener
            for (Listener listener : new Listener[]{new JoinQuitListenerBungee(), new ArenaUpdateListener()}) {
                Bukkit.getPluginManager().registerEvents(listener, SteveSus.getInstance());
            }

            // Start cache (remote lobby related) cleaner task
            cacheCleanerTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(SteveSus.getInstance(), new CacheCleanerTask(), 20L, 20L).getTaskId();
            // Initialize check new lobbies task
            checkNewLobbiesTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(SteveSus.getInstance(), new CheckNewLobbiesTask(), 20L, INSTANCE.getConfig().getProperty(MainConfig.BUNGEE_AUTO_SCALE_UPDATE_LOBBIES_INTERVAL) * 20).getTaskId();
            // Initialize arena life check task
            serverPingTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(SteveSus.getInstance(), new ServerPingTask(), 40L, INSTANCE.getConfig().getProperty(MainConfig.BUNGEE_AUTO_SCALE_PING_LOBBIES_INTERVAL) * 20).getTaskId();
            // Connect to remote lobbies
            INSTANCE.getConfig().getProperty(MainConfig.BUNGEE_AUTO_SCALE_LOBBY_SOCKETS).forEach(RemoteLobby::new);

            // register packet listeners
            SteveSus.newChain().delay(10).sync(() -> {
                if (!SteveSus.getInstance().getPacketsHandler().registerIncomingPacketChannel(DefaultChannel.PLAYER_JOIN_CHANNEL.toString(), new PlayerJoinChannel())) {
                    SteveSus.getInstance().getLogger().severe("Could not register remote packet channel: " + DefaultChannel.PLAYER_JOIN_CHANNEL.toString());
                }
            }).execute();

        } else if (getINSTANCE().getServerType() == ServerType.MULTI_ARENA) {

            // Handle Multi Arena
            // Register Multi Arena Mode Related Listener
            for (Listener listener : new Listener[]{new JoinQuitListenerMultiArena()}) {
                Bukkit.getPluginManager().registerEvents(listener, SteveSus.getInstance());
            }

            // Register Lobby listener
            LobbyProtectionListener.init(false);

        } else {

            // Handle Bungee Legacy
            for (Listener listener : new Listener[]{new JoinQuitListenerBungeeLegacy()}) {
                Bukkit.getPluginManager().registerEvents(listener, SteveSus.getInstance());
            }
        }

        // Common listeners
        for (Listener listener : new Listener[]{new ServerQuitListener(), new JoinCommonListener(), new PlayerChatListener(), new CommandItemListener(),
                new DropPickListener(), new BreakPlaceListener(), new RestartArenaListener(), new DamageListener(), new CreatureSpawnListener(),
                new SpectatorListener(), new WeatherListener(), new PlayerSneakEvent(), new PlayerMoveListener(), new EntityInteractListener(),
                new InventoryListener(), new SabotageListener(), new EntityTargetListener(), new StatsGainListener(),new MapLoadListener()}) {
            Bukkit.getPluginManager().registerEvents(listener, SteveSus.getInstance());
        }

        if (getINSTANCE().getServerType() != ServerType.BUNGEE_LEGACY) {
            // register listener that handles active arenas
            Bukkit.getPluginManager().registerEvents(new CloneArenaListener(), SteveSus.getInstance());
        }


        // Register bungee channel
        SteveSus.getInstance().getServer().getMessenger().registerOutgoingPluginChannel(SteveSus.getInstance(), "BungeeCord");
        //Bukkit.getMessenger().registerIncomingPluginChannel(SteveSus.getInstance(), "Return", new PluginChannelListener());

        INSTANCE.playerNPCSupport = PlayerNPCSupport.SupportBuilder.load();
    }

    public static void onDisable() {
        if (getINSTANCE().getServerType() == ServerType.BUNGEE) {
            long startTime = System.currentTimeMillis();
            Bukkit.getScheduler().cancelTask(cacheCleanerTaskId);
            Bukkit.getScheduler().cancelTask(checkNewLobbiesTaskId);
            Bukkit.getScheduler().cancelTask(serverPingTaskId);
            RemoteLobby.getSockets().values().forEach(RemoteLobby::close);
            SteveSus.debug("Took " + (System.currentTimeMillis() - startTime) + "ms to disable " + ServerManager.class.getSimpleName() + ".");
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
        SteveSus.getInstance().getLogger().info("Using DisconnectHandler: " + disconnectHandler.getName());
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

    public HologramAPI getHologramAPI() {
        return hologramAPI;
    }

    public PlayerNPCSupport getPlayerNPCSupport() {
        return playerNPCSupport;
    }

    public static ParticleSupport getParticleSupport() {
        return particleSupport;
    }
}
