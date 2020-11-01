package com.andrei1058.amongusmc.server.multiarena.listener;

import com.andrei1058.amongusmc.AmongUsMc;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldLoadListener implements Listener {

    public WorldLoadListener() {
        AmongUsMc.debug("Registered " + getClass().getSimpleName());
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        LobbyProtectionListener.init(false);
        if (LobbyProtectionListener.isInitialized()) {
            AmongUsMc.debug("Unregistered " + getClass().getSimpleName());
            WorldLoadEvent.getHandlerList().unregister(this);
        }
    }
}
