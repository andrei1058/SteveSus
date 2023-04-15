package com.andrei1058.stevesus.connector.socket;

import com.andrei1058.stevesus.common.api.packet.CommunicationHandler;
import com.andrei1058.stevesus.common.api.packet.DefaultChannel;
import com.andrei1058.stevesus.connector.SteveSusConnector;
import com.andrei1058.stevesus.connector.config.ConnectorConfig;
import com.andrei1058.stevesus.connector.socket.channel.*;
import com.andrei1058.stevesus.connector.socket.slave.SlaveServerSocket;
import com.andrei1058.stevesus.connector.socket.slave.SlaveSocketListener;
import com.andrei1058.stevesus.connector.socket.task.TimeOutCheckerTask;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class SocketManager {

    public static int TIME_OUT_TOLERANCE;
    private static boolean initialized;

    private SocketManager() {
    }

    public static void init() {
        if (initialized) return;
        initialized = true;

        TIME_OUT_TOLERANCE = SteveSusConnector.getConnectorConfig().getProperty(ConnectorConfig.TIME_OUT_TOLERANCE) * 1000;

        CommunicationHandler comm = SteveSusConnector.getInstance().getPacketsHandler();
        comm.registerIncomingPacketChannel(DefaultChannel.ARENA_FULL_DATA.toString(), new ArenaFullDataChannel());
        comm.registerIncomingPacketChannel(DefaultChannel.PLAYER_COUNT_UPDATE.toString(), new PlayerCountChannel());
        comm.registerIncomingPacketChannel(DefaultChannel.ARENA_STATUS_UPDATE.toString(), new GameStateChannel());
        comm.registerIncomingPacketChannel(DefaultChannel.GAME_DROP.toString(), new GameDropChannel());
        comm.registerIncomingPacketChannel(DefaultChannel.PING.toString(), new SlavePingChannel());

        // Listen for arenas
        try {
            SlaveSocketListener.init(SteveSusConnector.getConnectorConfig().getProperty(ConnectorConfig.LISTEN_PORT));
            SteveSusConnector.getInstance().getLogger().info("Listener for arenas on port: " + SteveSusConnector.getConnectorConfig().getProperty(ConnectorConfig.LISTEN_PORT));
        } catch (IOException e) {
            e.printStackTrace();
            SteveSusConnector.getInstance().getLogger().severe("Could not listen for arenas on port: " + SteveSusConnector.getConnectorConfig().getProperty(ConnectorConfig.LISTEN_PORT));
            SteveSusConnector.getInstance().getLogger().severe("Try using something else!");
            SteveSusConnector.getInstance().getLogger().severe("You have to update it on arena servers as well!");
            Bukkit.getPluginManager().disablePlugin(SteveSusConnector.getInstance());
        }

        // initialize time out checker task
        TimeOutCheckerTask.init();
    }

    @Nullable
    public static SlaveServerSocket getSlaveByBungeeName(@NotNull String name){
        return SlaveServerSocket.getSockets().values().stream().filter(slave -> slave.getName() != null && slave.getName().equals(name)).findFirst().orElse(null);
    }
}
