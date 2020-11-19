package com.andrei1058.stevesus.api.arena.task;

import com.andrei1058.stevesus.api.arena.Arena;
import org.bukkit.entity.Player;

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
     * Triggered on emergency meeting, player leave, player kill etc.
     * Check if given player has task assigned and was doing this task here.
     */
    public abstract void onInterrupt(Player player, Arena arena);

    /**
     * Get player current stage.
     */
    public abstract int getCurrentStage(Player player);

    /**
     * Get player current stage.
     */
    public abstract int getCurrentStage(UUID player);

    /**
     * Get stages.
     */
    public abstract int getTotalStages(Player player);

    /**
     * Get stages.
     */
    public abstract int getTotalStages(UUID player);

    /**
     * Use eventually when game starts.
     * Enable visual details for the given players. Yes, it will be triggered for multiple players.
     */
    public abstract void assignToPlayer(Player player, Arena arena);

    /**
     * Get list of players having this task.
     */
    public abstract Set<UUID> getAssignedPlayers();

    /**
     * Check if the given player has this task.
     */
    public abstract boolean hasTask(Player player);

    /**
     * Check if the given player is doing this task.
     */
    public boolean isDoingTask(Player player) {
        return isDoingTask(player.getUniqueId());
    }

    /**
     * Check if the given player is doing this task.
     */
    public abstract boolean isDoingTask(UUID player);

    /**
     * Enable task indicators.
     * Indicators help players find their assigned tasks.
     * Indicators should be active by default.
     */
    public abstract void enableIndicators();

    /**
     * Disable task indicators. Usually triggered during sabotages.
     * Indicators help players find their assigned tasks.
     * Indicators should be active by default.
     */
    public abstract void disableIndicators();

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GameTask)) return false;
        GameTask task = ((GameTask) obj);
        return task.getHandler().equals(this.getHandler()) && this.getLocalName().equals(task.getLocalName());
    }
}
