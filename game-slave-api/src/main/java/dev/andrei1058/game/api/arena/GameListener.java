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
    default void onPlayerJoin(Arena arena, Player player) {

    }

    /**
     * Player leave listener.
     * Remove your custom boss bar etc.
     *
     * @param spectator true if is spectator.
     */
    default void onPlayerLeave(Arena arena, Player player, boolean spectator) {

    }

    /**
     * Player moved to spectators.
     * Remove your custom boss bar etc.
     */
    default void onPlayerToSpectator(Arena arena, Player player) {

    }

    /**
     * On entity interact.
     */
    default void onPlayerInteractEntity(Arena arena, Player player, Entity entity) {

    }

    /**
     * On player interact.
     */
    default void onPlayerInteract(Arena arena, Player player, PlayerInteractEvent event, boolean hasItemInHand) {
        //todo
    }

    /**
     * On punch.
     * (pvp is not allowed btw)
     */
    default void onEntityPunch(Arena arena, Player player, Entity entity) {

    }

    default void onGameStateChange(Arena arena, GameState oldState, GameState newState) {
    }

    default void onPlayerToggleSneakEvent(Arena arena, Player player, boolean isSneaking) {

    }

    default void onPlayerToggleFly(Arena arena, Player player, boolean isFlying) {

    }

    default void onMeetingStageChange(Arena arena, MeetingStage oldStage, MeetingStage newStage) {

    }

    /**
     * This won't be triggered if {@link Arena#isCantMove(Player)}.
     */
    default void onPlayerMove(Arena arena, Player player, Location from, @Nullable Team playerTeam) {

    }

    default void onInventoryClose(Arena arena, Player player, Inventory inventory) {

    }

    default void onPlayerVent(Arena arena, Player player, Vent vent) {

    }

    default void onPlayerUnVent(Arena arena, Player player, Vent vent) {

    }

    default void onPlayerSwitchVent(Arena arena, Player player, Vent vent) {

    }

    /**
     * Not triggered if bukkit event got cancelled.
     */
    default void onPlayerKill(Arena arena, Player killer, Player victim, Team destinationTeam, PlayerCorpse corpse) {

    }
}
