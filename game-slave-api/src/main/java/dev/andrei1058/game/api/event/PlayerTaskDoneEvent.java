package dev.andrei1058.game.api.event;

import dev.andrei1058.game.api.arena.Arena;
import dev.andrei1058.game.api.arena.task.GameTask;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerTaskDoneEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Arena arena;
    private final GameTask task;
    private final Player player;

    public PlayerTaskDoneEvent(Arena arena, GameTask task, Player player) {
        this.arena = arena;
        this.task = task;
        this.player = player;
    }

    public GameTask getTask() {
        return task;
    }

    public Arena getArena() {
        return arena;
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
