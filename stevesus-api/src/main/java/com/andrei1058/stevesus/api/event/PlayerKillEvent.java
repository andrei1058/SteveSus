package com.andrei1058.stevesus.api.event;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.team.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class PlayerKillEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Arena arena;
    private final Player killer;
    private final Player victim;
    private boolean cancelled;
    private Team destinationTeam;

    public PlayerKillEvent(Arena arena, Player killer, Player victim, Team destinationTeam) {
        this.arena = arena;
        this.killer = killer;
        this.victim = victim;
        this.destinationTeam = destinationTeam;
    }

    /**
     * When destination team is null the death player is moved to spectators.
     */
    public void setDestinationTeam(@Nullable Team destinationTeam) {
        this.destinationTeam = destinationTeam;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public Player getKiller() {
        return killer;
    }

    public Player getVictim() {
        return victim;
    }

    public Arena getArena() {
        return arena;
    }

    /**
     * When destination team is null the death player is moved to spectators.
     */
    @Nullable
    public Team getDestinationTeam() {
        return destinationTeam;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
