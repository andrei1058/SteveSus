package dev.andrei1058.game.api.event;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.arena.task.GameTask;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerTaskDoneEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final GameArena gameArena;
    private final GameTask task;
    private final Player player;

    public PlayerTaskDoneEvent(GameArena gameArena, GameTask task, Player player) {
        this.gameArena = gameArena;
        this.task = task;
        this.player = player;
    }

    public GameTask getTask() {
        return task;
    }

    public GameArena getArena() {
        return gameArena;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
