package com.andrei1058.stevesus.setup.listeners;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.setup.SetupSession;
import com.andrei1058.stevesus.arena.ArenaManager;
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
            SteveSus.newChain().delay(10).sync(() -> {
                setupSession.onStart(e.getWorld());
                e.getWorld().getEntities().forEach(entity -> {
                    if (entity instanceof Creature) {
                        entity.remove();
                    }
                });
            }).execute();
            SteveSus.newChain().delay(20 * 4).sync(() -> SetupManager.getINSTANCE().initializeSavedTasks(setupSession, e.getWorld().getName())).execute();
        }

        // handle enable queue
        Arena arena = ArenaManager.getINSTANCE().getFromEnableQueue(worldName);
        if (arena != null) {
            ArenaManager.getINSTANCE().removeFromEnableQueue(worldName);
            e.getWorld().getEntities().forEach(entity -> {
                if (entity instanceof Creature) {
                    entity.remove();
                }
            });
            arena.init(e.getWorld());
            ArenaManager.getINSTANCE().addArena(arena);
        }
    }
}
