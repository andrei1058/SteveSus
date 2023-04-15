package dev.andrei1058.game.api.event;

import dev.andrei1058.game.api.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerToSpectatorEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Arena arena;
    private final Player player;

    /**
     * This is triggered when a player was eliminated and moved to spectators.
     * <p>
     * This event is triggered by the following methods:
     * {@link Arena#switchToSpectator(Player)}.
     *
     * @param arena     target arena.
     * @param player    player.
     */
    public PlayerToSpectatorEvent(Arena arena, Player player) {
        this.arena = arena;
        this.player = player;
    }

    public Arena getArena() {
        return arena;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}