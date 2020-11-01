package com.andrei1058.amongusmc.api.event;

import com.andrei1058.amongusmc.api.arena.Arena;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GameDisableEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final int gameId;
    private final Arena arena;

    /**
     * Triggered when an arena is disabled by command, server shutdown or other.
     *
     * @param arena  arena.
     * @param gameId game id.
     */
    public GameDisableEvent(int gameId, Arena arena) {
        this.gameId = gameId;
        this.arena = arena;
    }

    /**
     * Get game id.
     *
     * @return game id.
     */
    public int getGameId() {
        return gameId;
    }

    /**
     * Get arena.
     *
     * @return arena.
     */
    public Arena getArena() {
        return arena;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
