package com.andrei1058.stevesus.api.event;

import com.andrei1058.stevesus.api.arena.Arena;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerGameJoinEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Arena arena;
    private final Player player;
    private final boolean spectator;
    private boolean cancelled = false;

    /**
     * This is triggered before a player is added to an arena and the player passed all checks (available slots, game state, party, etc.).
     * You can cancel this event for example if you have a penalty system.
     * <p>
     * This event is triggered by the following methods:
     * {@link Arena#addPlayer(Player, boolean)}, {@link Arena#joinPlayer(Player, boolean)}, {@link Arena#addSpectator(Player, Location)}, {@link Arena#joinSpectator(Player, String)}.
     *
     * @param arena     target arena.
     * @param player    player.
     * @param spectator if joined as spectator.
     */
    public PlayerGameJoinEvent(Arena arena, Player player, boolean spectator) {
        this.arena = arena;
        this.player = player;
        this.spectator = spectator;
    }

    public Arena getArena() {
        return arena;
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * @return true if the player joined as spectator.
     */
    @SuppressWarnings("unused")
    public boolean isSpectator() {
        return spectator;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Cancel game Join Event.
     */
    @SuppressWarnings("unused")
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Check if event was cancelled.
     */
    public boolean isCancelled() {
        return cancelled;
    }
}
