package dev.andrei1058.game.api.event;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.common.api.arena.GameState;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GameStateChangeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final GameArena gameArena;
    private final GameState oldState;
    private final GameState newState;

    public GameStateChangeEvent(GameArena gameArena, GameState oldState, GameState newState){
        this.gameArena = gameArena;
        this.oldState = oldState;
        this.newState = newState;
    }

    public GameArena getArena() {
        return gameArena;
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
