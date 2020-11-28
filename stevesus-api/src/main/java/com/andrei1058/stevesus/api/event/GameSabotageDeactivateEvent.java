package com.andrei1058.stevesus.api.event;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.sabotage.SabotageBase;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameSabotageDeactivateEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Arena arena;
    private final boolean forceDisable;
    private final SabotageBase sabotageBase;

    public GameSabotageDeactivateEvent(Arena arena, SabotageBase sabotageBase, boolean forceDisabled) {
        this.arena = arena;
        this.forceDisable = forceDisabled;
        this.sabotageBase = sabotageBase;
    }

    public Arena getArena() {
        return arena;
    }

    public SabotageBase getSabotageBase() {
        return sabotageBase;
    }

    /**
     * If was disabled by a dead body report etc.
     */
    public boolean isForceDisable() {
        return forceDisable;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}