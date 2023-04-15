package dev.andrei1058.game.api.event;

import dev.andrei1058.game.api.arena.GameArena;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GameDisableEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final int gameId;
    private final GameArena gameArena;

    /**
     * Triggered when an arena is disabled by command, server shutdown or other.
     *
     * @param gameArena  arena.
     * @param gameId game id.
     */
    public GameDisableEvent(int gameId, GameArena gameArena) {
        this.gameId = gameId;
        this.gameArena = gameArena;
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
    public GameArena getArena() {
        return gameArena;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
