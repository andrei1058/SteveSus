package com.andrei1058.amongusmc.api.server;

import org.bukkit.entity.Player;

public interface DisconnectHandler {

    String getName();

    void performDisconnect(Player player);
}
