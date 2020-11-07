package com.andrei1058.stevesus.setup.listeners;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.setup.SetupSession;
import com.andrei1058.stevesus.arena.ArenaHandler;
import com.andrei1058.stevesus.setup.SetupManager;
import org.bukkit.entity.Creature;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldLoadListener implements Listener {

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {

        String worldName = e.getWorld().getName();

        // start setup session waiting for this map
        SetupSession setupSession = SetupManager.getINSTANCE().getSession(worldName);
        if (setupSession != null) {
            setupSession.onStart(e.getWorld());
            e.getWorld().getEntities().forEach(entity -> {
                if (entity instanceof Creature) {
                    entity.remove();
                }
            });
            SetupManager.getINSTANCE().initializeSavedTasks(setupSession, e.getWorld().getName());
        }

        // handle enable queue
        Arena arena = ArenaHandler.getINSTANCE().getFromEnableQueue(worldName);
        if (arena != null) {
            ArenaHandler.getINSTANCE().removeFromEnableQueue(worldName);
            e.getWorld().getEntities().forEach(entity -> {
                if (entity instanceof Creature) {
                    entity.remove();
                }
            });
            arena.init(e.getWorld());
            ArenaHandler.getINSTANCE().addArena(arena);
        }
    }
}
