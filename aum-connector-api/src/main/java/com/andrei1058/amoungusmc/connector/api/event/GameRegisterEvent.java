package com.andrei1058.amoungusmc.connector.api.event;

import com.andrei1058.amoungusmc.common.api.arena.DisplayableArena;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GameRegisterEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final DisplayableArena arena;

    /**
     * Triggered when a new arena is received.
     *
     * @param arena arena.
     */
    public GameRegisterEvent(DisplayableArena arena) {
        this.arena = arena;
    }

    /**
     * Get arena.
     *
     * @return arena.
     */
    public DisplayableArena getArena() {
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
