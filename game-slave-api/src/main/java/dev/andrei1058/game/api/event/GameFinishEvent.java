package dev.andrei1058.game.api.event;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.arena.team.Team;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

public class GameFinishEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final GameArena gameArena;
    private final LinkedList<Team> winners;

    /**
     * Triggered when a game is finished.
     *
     * @param gameArena  arena.
     */
    public GameFinishEvent(GameArena gameArena, LinkedList<Team> winners) {
        this.gameArena = gameArena;
        this.winners = winners;
    }

    public LinkedList<Team> getWinners() {
        return winners;
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
