package com.andrei1058.amongusmc.api.event;

import com.andrei1058.amongusmc.api.arena.Arena;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.UUID;

public class GameFinishEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Arena arena;
    private final LinkedList<UUID> winners;

    /**
     * Triggered when a game is finished.
     *
     * @param arena  arena.
     */
    public GameFinishEvent(Arena arena, LinkedList<UUID> winners) {
        this.arena = arena;
        this.winners = winners;
    }

    public LinkedList<UUID> getWinners() {
        return winners;
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
