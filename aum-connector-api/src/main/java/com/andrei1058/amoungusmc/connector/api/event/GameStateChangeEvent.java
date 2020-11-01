package com.andrei1058.amoungusmc.connector.api.event;

import com.andrei1058.amoungusmc.common.api.arena.DisplayableArena;
import com.andrei1058.amoungusmc.common.api.arena.GameState;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GameStateChangeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final DisplayableArena arena;
    private final GameState oldState;
    private final GameState newState;

    /**
     * Triggered when a remote arena changes its status.
     */
    public GameStateChangeEvent(DisplayableArena arena, GameState oldState, GameState newState) {
        this.arena = arena;
        this.oldState = oldState;
        this.newState = newState;
    }

    public DisplayableArena getArena() {
        return arena;
    }

    public GameState getNewState() {
        return newState;
    }

    public GameState getOldState() {
        return oldState;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
