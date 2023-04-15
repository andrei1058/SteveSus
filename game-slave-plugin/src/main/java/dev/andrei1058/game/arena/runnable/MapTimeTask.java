package dev.andrei1058.game.arena.runnable;

import dev.andrei1058.game.arena.ArenaManager;

/**
 * Used to change map day cycle.
 */
public class MapTimeTask implements Runnable {

    @Override
    public void run() {
        ArenaManager.getINSTANCE().getArenas().forEach(arena -> {
            if (arena.getWorld() != null && arena.getTime() != null) {
                if (!arena.getTime().isInRange(arena.getWorld().getTime())) {
                    arena.getWorld().setTime(arena.getTime().getStartTick());
                }
            }
        });
    }
}
