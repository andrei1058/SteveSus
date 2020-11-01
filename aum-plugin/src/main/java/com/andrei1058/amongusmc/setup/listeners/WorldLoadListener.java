package com.andrei1058.amongusmc.setup.listeners;

import com.andrei1058.amongusmc.api.arena.Arena;
import com.andrei1058.amongusmc.api.setup.SetupSession;
import com.andrei1058.amongusmc.arena.ArenaManager;
import com.andrei1058.amongusmc.setup.SetupManager;
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
                if (entity instanceof Creature){
                    entity.remove();
                }
            });
        }

        // handle enable queue
        Arena arena = ArenaManager.getINSTANCE().getFromEnableQueue(worldName);
        if (arena != null){
            ArenaManager.getINSTANCE().removeFromEnableQueue(worldName);
            e.getWorld().getEntities().forEach(entity -> {
                if (entity instanceof Creature){
                    entity.remove();
                }
            });
            arena.init(e.getWorld());
            ArenaManager.getINSTANCE().addArena(arena);
        }
    }
}
