package com.andrei1058.amongusmc.api.event;

import com.andrei1058.amongusmc.api.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class SpectatorFirstPersonEvent extends Event {

    public enum SpectateAction {
        /**
         * When started spectating in first person.
         */
        START,
        /**
         * When stopped spectating in first person.
         */
        STOP;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    private final Arena arena;
    private final Player spectator;
    private final Player target;
    private final SpectateAction action;
    private boolean cancelled = false;

    /**
     * This is triggered when a player was eliminated and moved to spectators.
     * <p>
     * This event is triggered by the following methods:
     * {@link Arena#switchToSpectator(Player)}.
     *
     * @param arena     target arena.
     * @param spectator spectator.
     * @param target    target.
     * @param action    check if started or stopped spectating.
     */
    public SpectatorFirstPersonEvent(Arena arena, Player spectator, Player target, SpectateAction action) {
        this.arena = arena;
        this.spectator = spectator;
        this.target = target;
        this.action = action;
    }

    public Arena getArena() {
        return arena;
    }

    public Player getSpectator() {
        return spectator;
    }

    public Player getTarget() {
        return target;
    }

    public SpectateAction getAction() {
        return action;
    }

    /**
     * You can cancel only {@link SpectateAction#START}.
     */
    public void setCancelled(boolean toggle) {
        this.cancelled = toggle;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
