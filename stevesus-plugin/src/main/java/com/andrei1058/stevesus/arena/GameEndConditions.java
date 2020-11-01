package com.andrei1058.stevesus.arena;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.event.GameFinishEvent;
import com.andrei1058.stevesus.common.api.arena.GameState;
import org.bukkit.Bukkit;

import java.util.LinkedList;
import java.util.UUID;

public class GameEndConditions {

    private GameEndConditions() {
    }

    /**
     * Check if it is the case to assign a winner and or restart arena.
     * <p>
     * This method will handle everything. Sending messages etc.
     * Works with IN_GAME status only.
     */
    public static void tickGameEndConditions(Arena arena) {
        if (arena.getGameState() != GameState.IN_GAME) return;

        // if no players left
        if (arena.getPlayers().isEmpty()) {
            arena.switchState(GameState.ENDING);

            LinkedList<UUID> winners = new LinkedList<>();
            arena.getPlayers().forEach(player -> winners.add(player.getUniqueId()));
            Bukkit.getPluginManager().callEvent(new GameFinishEvent(arena, winners));
            arena.setCountdown(3);
        }
    }
}
