package com.andrei1058.stevesus.api.event;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.common.api.arena.GameState;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GameStateChangeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Arena arena;
    private final GameState oldState;
    private final GameState newState;

    public GameStateChangeEvent(Arena arena, GameState oldState, GameState newState){
        this.arena = arena;
        this.oldState = oldState;
        this.newState = newState;
    }

    public Arena getArena() {
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
