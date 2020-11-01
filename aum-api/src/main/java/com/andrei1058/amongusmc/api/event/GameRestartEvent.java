package com.andrei1058.amongusmc.api.event;

import com.andrei1058.amongusmc.api.arena.Arena;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameRestartEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private int gameId;
    private Arena arena;

    /**
     * Triggered when a game is finished and marked as respawning.
     *
     * @param gameId game id.
     * @param arena  arena.
     */
    public GameRestartEvent(int gameId, Arena arena) {
        this.gameId = gameId;
        this.arena = arena;
    }

    public int getGameId() {
        return gameId;
    }

    public Arena getArena() {
        return arena;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
