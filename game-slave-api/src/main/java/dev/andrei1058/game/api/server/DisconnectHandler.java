package dev.andrei1058.game.api.server;

import org.bukkit.entity.Player;

public interface DisconnectHandler {

    String getName();

    void performDisconnect(Player player);
}
