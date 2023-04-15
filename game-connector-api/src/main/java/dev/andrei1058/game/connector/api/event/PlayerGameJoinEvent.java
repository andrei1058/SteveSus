package dev.andrei1058.game.connector.api.event;

import dev.andrei1058.game.common.api.arena.DisplayableArena;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerGameJoinEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final DisplayableArena arena;
    private final Player player;
    private final boolean spectator;
    private boolean cancelled = false;

    /**
     * This is triggered when a player was added to an arena via and the player passed all checks (available slots, game state, party, etc.).
     * {@link DisplayableArena#joinPlayer(Player, boolean)}, {@link DisplayableArena#joinSpectator(Player, String)}.
     *
     * @param arena     target arena.
     * @param player    player.
     * @param spectator if joined as spectator.
     */
    public PlayerGameJoinEvent(DisplayableArena arena, Player player, boolean spectator) {
        this.arena = arena;
        this.player = player;
        this.spectator = spectator;
    }

    public DisplayableArena getArena() {
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
