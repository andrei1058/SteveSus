package dev.andrei1058.game.api.event;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.arena.vent.Vent;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerVentEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final GameArena gameArena;
    private final Player player;
    private final Vent vent;

    public PlayerVentEvent(GameArena gameArena, Player player, Vent vent) {
        this.gameArena = gameArena;
        this.player = player;
        this.vent = vent;
    }

    public GameArena getArena() {
        return gameArena;
    }

    public Player getPlayer() {
        return player;
    }

    public Vent getVent() {
        return vent;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}