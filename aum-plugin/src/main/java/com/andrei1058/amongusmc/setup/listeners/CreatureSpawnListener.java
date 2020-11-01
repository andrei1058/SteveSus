package com.andrei1058.amongusmc.setup.listeners;

import com.andrei1058.amongusmc.setup.SetupManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CreatureSpawnListener implements Listener {

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent e){
        if (e.isCancelled())return;
        if (e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM)
        if (SetupManager.getINSTANCE().getSession(e.getEntity().getWorld().getName()) != null) {
            e.setCancelled(true);
        }
    }
}
