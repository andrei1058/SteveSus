package dev.andrei1058.game.setup.listeners;

import dev.andrei1058.game.setup.SetupManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CreatureSpawnListener implements Listener {

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent e){
        if (e.isCancelled())return;
        if (!(e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM || e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG)) {
            if (SetupManager.getINSTANCE().getSession(e.getEntity().getWorld().getName()) != null) {
                e.setCancelled(true);
            }
        }
    }
}
