package dev.andrei1058.game.api.event;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.arena.team.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class PlayerKillEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final GameArena gameArena;
    private final Player killer;
    private final Player victim;
    private boolean cancelled;
    private Team destinationTeam;

    public PlayerKillEvent(GameArena gameArena, Player killer, Player victim, Team destinationTeam) {
        this.gameArena = gameArena;
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

    public GameArena getArena() {
        return gameArena;
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
