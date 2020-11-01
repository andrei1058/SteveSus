package com.andrei1058.amongusmc.server.bungee.remote.task;

import com.andrei1058.amongusmc.AmongUsMc;
import com.andrei1058.amongusmc.common.packet.PingPacket;
import com.andrei1058.amongusmc.server.bungee.remote.RemoteLobby;
import com.andrei1058.amoungusmc.common.api.packet.DefaultChannel;

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
        AmongUsMc.getInstance().getPacketsHandler().sendPacket(DefaultChannel.PING.toString(), PING_PACKET, false);
        RemoteLobby.getSockets().values().forEach(lobby -> {
            if (lobby.isConnected() && lobby.isTimedOut()) {
                AmongUsMc.debug("Remote Lobby timed out: " + lobby.getName());
                lobby.close();
            }
        });
    }
}
