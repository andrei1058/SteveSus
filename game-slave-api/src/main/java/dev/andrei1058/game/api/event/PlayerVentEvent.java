package dev.andrei1058.game.api.event;

import dev.andrei1058.game.api.arena.Arena;
import dev.andrei1058.game.api.arena.vent.Vent;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerVentEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Arena arena;
    private final Player player;
    private final Vent vent;

    public PlayerVentEvent(Arena arena, Player player, Vent vent) {
        this.arena = arena;
        this.player = player;
        this.vent = vent;
    }

    public Arena getArena() {
        return arena;
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