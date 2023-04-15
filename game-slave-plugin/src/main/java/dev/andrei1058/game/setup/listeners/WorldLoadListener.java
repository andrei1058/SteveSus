package dev.andrei1058.game.setup.listeners;

import dev.andrei1058.game.SteveSus;
import dev.andrei1058.game.api.arena.Arena;
import dev.andrei1058.game.api.setup.SetupSession;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.setup.SetupManager;
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
