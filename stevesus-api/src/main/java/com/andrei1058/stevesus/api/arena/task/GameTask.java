package com.andrei1058.stevesus.api.arena.task;

import com.andrei1058.stevesus.api.arena.Arena;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * This is the actual task instance used by arenas.
 */
public abstract class GameTask {

    /**
     * Get task manager.
     */
    public abstract TaskHandler getHandler();

    /**
     * Triggered when current task is cancelled.
     * If player gets killed, emergency meeting etc.
     */
    public abstract void onInterrupt(Player player, Arena arena);

    /**
     * Use eventually when game starts.
     * Enable visual details for the given players. Yes, it will be triggered for multiple players.
     */
    public abstract void assignToPlayer(Player player, Arena arena);

    /**
     * Use eventually when game starts.
     * Enable visual details for the given players.
     */
    public abstract void assignToPlayers(List<Player> players, Arena arena);

    /**
     * Get list of players having this task.
     */
    public abstract List<Player> getAssignedPlayers();
}
