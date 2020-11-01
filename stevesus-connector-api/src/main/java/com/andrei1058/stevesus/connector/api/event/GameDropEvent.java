package com.andrei1058.stevesus.connector.api.event;

import com.andrei1058.stevesus.common.api.arena.DisplayableArena;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GameDropEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final DisplayableArena arena;

    /**
     * Triggered when an arena is removed from the local cache. Usually when the game has ended.
     *
     * @param arena  arena.
     */
    public GameDropEvent(DisplayableArena arena) {
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
