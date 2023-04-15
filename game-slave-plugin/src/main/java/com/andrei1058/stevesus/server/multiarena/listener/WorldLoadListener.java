package com.andrei1058.stevesus.server.multiarena.listener;

import com.andrei1058.stevesus.SteveSus;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldLoadListener implements Listener {

    public WorldLoadListener() {
        SteveSus.debug("Registered " + getClass().getSimpleName());
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        LobbyProtectionListener.init(false);
        if (LobbyProtectionListener.isInitialized()) {
            SteveSus.debug("Unregistered " + getClass().getSimpleName());
            WorldLoadEvent.getHandlerList().unregister(this);
        }
    }
}
