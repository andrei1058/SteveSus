package com.andrei1058.stevesus.api.event;

import com.andrei1058.stevesus.api.arena.Arena;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameInitializedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private Arena arena;
    private String template;
    private String cloneWorldName;

    /**
     * Triggered when an arena is declared as ready.
     *
     * @param arena          arena.
     * @param template       original world name.
     * @param cloneWorldName cloned world name.
     */
    public GameInitializedEvent(Arena arena, String template, String cloneWorldName) {
        this.arena = arena;
        this.template = template;
        this.cloneWorldName = cloneWorldName;
    }

    public Arena getArena() {
        return arena;
    }

    public String getTemplate() {
        return template;
    }

    public String getCloneWorldName() {
        return cloneWorldName;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
