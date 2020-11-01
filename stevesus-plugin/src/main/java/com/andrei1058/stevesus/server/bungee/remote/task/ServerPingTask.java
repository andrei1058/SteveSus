package com.andrei1058.stevesus.server.bungee.remote.task;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.common.api.packet.DefaultChannel;
import com.andrei1058.stevesus.common.packet.PingPacket;
import com.andrei1058.stevesus.server.bungee.remote.RemoteLobby;

/**
 * Let remote servers know this server isn't dead and sent arenas are up to date.
 */
public class ServerPingTask implements Runnable {

    private static PingPacket PING_PACKET;

    public ServerPingTask() {
        PING_PACKET = new PingPacket();
    }

    @Override
    public void run() {
        SteveSus.getInstance().getPacketsHandler().sendPacket(DefaultChannel.PING.toString(), PING_PACKET, false);
        RemoteLobby.getSockets().values().forEach(lobby -> {
            if (lobby.isConnected() && lobby.isTimedOut()) {
                SteveSus.debug("Remote Lobby timed out: " + lobby.getName());
                lobby.close();
            }
        });
    }
}
