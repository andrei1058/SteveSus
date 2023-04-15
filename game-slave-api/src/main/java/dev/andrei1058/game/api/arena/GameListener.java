package dev.andrei1058.game.api.arena;

import dev.andrei1058.game.api.arena.meeting.MeetingStage;
import dev.andrei1058.game.api.arena.team.Team;
import dev.andrei1058.game.api.arena.vent.Vent;
import dev.andrei1058.game.common.api.arena.GameState;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

/**
 * Should improve a bit of performance I guess.
 */
@SuppressWarnings("unused")
public interface GameListener {


    /**
     * Player join listener.
     */
    default void onPlayerJoin(GameArena gameArena, Player player) {

    }

    /**
     * Player leave listener.
     * Remove your custom boss bar etc.
     *
     * @param spectator true if is spectator.
     */
    default void onPlayerLeave(GameArena gameArena, Player player, boolean spectator) {

    }

    /**
     * Player moved to spectators.
     * Remove your custom boss bar etc.
     */
    default void onPlayerToSpectator(GameArena gameArena, Player player) {

    }

    /**
     * On entity interact.
     */
    default void onPlayerInteractEntity(GameArena gameArena, Player player, Entity entity) {

    }

    /**
     * On player interact.
     */
    default void onPlayerInteract(GameArena gameArena, Player player, PlayerInteractEvent event, boolean hasItemInHand) {
        //todo
    }

    /**
     * On punch.
     * (pvp is not allowed btw)
     */
    default void onEntityPunch(GameArena gameArena, Player player, Entity entity) {

    }

    default void onGameStateChange(GameArena gameArena, GameState oldState, GameState newState) {
    }

    default void onPlayerToggleSneakEvent(GameArena gameArena, Player player, boolean isSneaking) {

    }

    default void onPlayerToggleFly(GameArena gameArena, Player player, boolean isFlying) {

    }

    default void onMeetingStageChange(GameArena gameArena, MeetingStage oldStage, MeetingStage newStage) {

    }

    /**
     * This won't be triggered if {@link GameArena#isCantMove(Player)}.
     */
    default void onPlayerMove(GameArena gameArena, Player player, Location from, @Nullable Team playerTeam) {

    }

    default void onInventoryClose(GameArena gameArena, Player player, Inventory inventory) {

    }

    default void onPlayerVent(GameArena gameArena, Player player, Vent vent) {

    }

    default void onPlayerUnVent(GameArena gameArena, Player player, Vent vent) {

    }

    default void onPlayerSwitchVent(GameArena gameArena, Player player, Vent vent) {

    }

    /**
     * Not triggered if bukkit event got cancelled.
     */
    default void onPlayerKill(GameArena gameArena, Player killer, Player victim, Team destinationTeam, PlayerCorpse corpse) {

    }
}
