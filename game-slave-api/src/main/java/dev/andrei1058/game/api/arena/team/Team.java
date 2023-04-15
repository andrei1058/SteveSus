package dev.andrei1058.game.api.arena.team;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.locale.Locale;
import org.bukkit.entity.Player;

import java.util.List;

@SuppressWarnings("unused")
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
     *
     * @param gameStartAssign will be true if trying to assign a player to this team at game start.
     * @return false if player could not be assigned to that team.
     */
    boolean addPlayer(Player player, boolean gameStartAssign);

    /**
     * Remove a player from this team. Used when a player leaves the arena.
     *
     */
    void removePlayer(Player player);

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
     * Check if team has tasks.
     */
    boolean canHaveTasks();

    /**
     * Set if a team can vote.
     * Useful if you add a detective team and you want to disable voting for crew and imposters.
     */
    void setCanVote(boolean toggle);

    /**
     * Check if this team can report bodies.
     */
    boolean canReportBody();

    /**
     * Check if this team can use meeting button.
     */
    boolean canUseMeetingButton();

    /**
     * Get game arena.
     */
    GameArena getArena();

    /**
     * ASYNC.
     *
     * @return true if the given player shouldn't receive chat messages from team members.
     */
    boolean chatFilter(Player player);

    /**
     * Check if this team is innocent.
     * False if is impostor team etc.
     */
    boolean isInnocent();

    boolean canBeVoted();
}
