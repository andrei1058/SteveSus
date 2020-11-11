package com.andrei1058.stevesus.api.arena.task;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.common.api.arena.GameState;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * This is the actual task instance used by arenas.
 */
public abstract class GameTask {

    /**
     * Get task manager.
     */
    public abstract TaskProvider getHandler();

    /**
     * Get string used to by the server owner to remember this task configuration.
     * Used for 'distinct' purposes. Like {@link #equals(Object)}.
     */
    public abstract String getLocalName();

    /**
     * Triggered when current task is cancelled.
     * If player gets killed, emergency meeting etc.
     */
    public abstract void onInterrupt(Player player, Arena arena);

    /**
     * Get player current stage.
     */
    public abstract int getCurrentStage(Player player);

    /**
     * Get stages.
     */
    public abstract int getTotalStages(Player player);

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
    public abstract Set<UUID> getAssignedPlayers();

    /**
     * Check if the given player has this task.
     */
    public abstract boolean hasTask(Player player);

    /**
     * Game state listener.
     */
    public void onGameStateChange(GameState oldState, GameState newState, Arena arena) {
    }

    /**
     * Player Join listener.
     * Use it to send your custom packets/ content etc.
     */
    public void onPlayerJoin(Arena arena, Player player, boolean spectator) {
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GameTask)) return false;
        GameTask task = ((GameTask) obj);
        return task.getHandler().equals(this.getHandler()) && this.getLocalName().equals(task.getLocalName());
    }
}
