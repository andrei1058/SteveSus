package dev.andrei1058.game.api.event;

import dev.andrei1058.game.api.arena.GameArena;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerGameLeaveEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final GameArena gameArena;
    private final Player player;
    private final boolean spectator;
    private final boolean abandon;

    public PlayerGameLeaveEvent(GameArena gameArena, Player player, boolean spectator, boolean abandon){
        this.gameArena = gameArena;
        this.player = player;
        this.spectator = spectator;
        this.abandon = abandon;
    }

    public Player getPlayer() {
        return player;
    }

    public GameArena getArena() {
        return gameArena;
    }

    public boolean isSpectator() {
        return spectator;
    }

    public boolean isAbandon() {
        return abandon;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
