package dev.andrei1058.game.api.event;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.arena.sabotage.SabotageBase;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameSabotageDeactivateEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final GameArena gameArena;
    private final boolean forceDisable;
    private final SabotageBase sabotageBase;
    private final List<Player> players = new ArrayList<>();

    public GameSabotageDeactivateEvent(GameArena gameArena, SabotageBase sabotageBase, boolean forceDisabled, Player... fixer) {
        this.gameArena = gameArena;
        this.forceDisable = forceDisabled;
        this.sabotageBase = sabotageBase;
        players.addAll(Arrays.asList(fixer));
    }

    public GameArena getArena() {
        return gameArena;
    }

    public SabotageBase getSabotageBase() {
        return sabotageBase;
    }

    /**
     * If was disabled by a dead body report etc.
     */
    public boolean isForceDisable() {
        return forceDisable;
    }

    /**
     * Players who contributed to fix this sabotage.
     */
    public List<Player> getPlayers() {
        return players;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}