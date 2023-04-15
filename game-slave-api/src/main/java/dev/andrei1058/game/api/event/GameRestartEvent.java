package dev.andrei1058.game.api.event;

import dev.andrei1058.game.api.arena.GameArena;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameRestartEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private int gameId;
    private GameArena gameArena;

    /**
     * Triggered when a game is finished and marked as respawning.
     *
     * @param gameId game id.
     * @param gameArena  arena.
     */
    public GameRestartEvent(int gameId, GameArena gameArena) {
        this.gameId = gameId;
        this.gameArena = gameArena;
    }

    public int getGameId() {
        return gameId;
    }

    public GameArena getArena() {
        return gameArena;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
