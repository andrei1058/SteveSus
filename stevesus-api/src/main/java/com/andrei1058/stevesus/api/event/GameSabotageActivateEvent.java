package com.andrei1058.stevesus.api.event;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.sabotage.SabotageBase;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameSabotageActivateEvent  extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Arena arena;
    private final SabotageBase sabotageBase;

    public GameSabotageActivateEvent(Arena arena, SabotageBase sabotageBase) {
        this.arena = arena;
        this.sabotageBase = sabotageBase;
    }

    public Arena getArena() {
        return arena;
    }

    public SabotageBase getSabotageBase() {
        return sabotageBase;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}