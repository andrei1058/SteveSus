package com.andrei1058.stevesus.api.arena;

import com.andrei1058.stevesus.api.locale.Locale;
import org.bukkit.entity.Player;

import java.util.List;

public interface Team {

    /**
     * Get members list.
     */
    List<Player> getMembers();

    /**
     * Check if a member is in this team.
     */
    boolean isMember(Player player);

    /**
     * Check if this team can kill target player.
     * Always false for crew mates.
     */
    boolean canKill(Player player);

    /**
     * Add a player to this team. Used to assign player to this team.
     */
    void addPlayer(Player player);

    /**
     * Remove a player from this team. Used when a player leaves the arena.
     *
     * @param abandon true if player has abandoned the game.
     */
    void removePlayer(Player player, boolean abandon);

    /**
     * Get team's display name for the given player (language).
     */
    String getDisplayName(Player player);

    /**
     * Get team's display name in the given language.
     */
    String getDisplayName(Locale locale);

    /**
     * Team name identifier. Used for language paths and other configurations.
     */
    String getIdentifier();

    /**
     * Check if this team can vote.
     */
    boolean canVote();

    /**
     * Set if a team can vote.
     * Useful if you add a detective team and you want to disable voting for crew and imposters.
     */
    void setCanVote(boolean toggle);
}