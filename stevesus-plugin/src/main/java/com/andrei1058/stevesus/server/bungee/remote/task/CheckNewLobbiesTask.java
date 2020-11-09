package com.andrei1058.stevesus.server.bungee.remote.task;

import com.andrei1058.stevesus.arena.ArenaManager;
import com.andrei1058.stevesus.server.bungee.remote.RemoteLobby;

public class CheckNewLobbiesTask implements Runnable {

    @Override
    public void run() {
        ArenaManager.getINSTANCE().getArenas().forEach(arena -> {
            RemoteLobby.getSockets().values().forEach(lobby -> {
                if (!lobby.isConnected()) {
                    lobby.connect();
                }
            });
        });
    }
}
