package dev.andrei1058.game.server.bungee.remote.task;

import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.server.bungee.remote.RemoteLobby;

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
