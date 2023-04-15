package dev.andrei1058.game.arena.listener;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.arena.GameListener;
import dev.andrei1058.game.api.arena.PlayerCorpse;
import dev.andrei1058.game.api.arena.team.Team;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.hook.glowing.GlowingManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

public class PlayerMoveListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onSpring(PlayerToggleSprintEvent event) {
        if (!event.isSprinting()) return;
        GameArena gameArena = ArenaManager.getINSTANCE().getArenaByPlayer(event.getPlayer());
        if (gameArena == null) return;
        if (!gameArena.getLiveSettings().isSprintAllowed()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.isCancelled()) return;
        GameArena gameArena = ArenaManager.getINSTANCE().getArenaByPlayer(event.getPlayer());
        if (gameArena == null) return;
        if (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getZ() != event.getTo().getZ() || event.getFrom().getBlockY() != event.getTo().getBlockY()) {
            final Location location = event.getFrom();
            final Player player = event.getPlayer();
            if (gameArena.isCantMove(player)) {
                location.setYaw(event.getTo().getYaw());
                location.setPitch(event.getTo().getPitch());
                location.setZ(event.getFrom().getBlockZ() + 0.5);
                location.setX(event.getFrom().getBlockX() + 0.5);
                player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
            } else {
                Team playerTeam = gameArena.getPlayerTeam(player);
                for (PlayerCorpse corpse : gameArena.getDeadBodies()) {
                    if (corpse.getHologram() != null) {
                        if (corpse.isInRange(location)) {
                            if (corpse.getHologram().isHiddenFor(player)) {
                                if (playerTeam != null && playerTeam.canReportBody()) {
                                    corpse.getHologram().show(player);
                                }
                            }
                        } else {
                            if (!corpse.getHologram().isHiddenFor(player)) {
                                corpse.getHologram().hide(player);
                            }
                        }
                    }
                }

                if (gameArena.getLiveSettings().getKillDistance().getCurrentValue() > 0) {
                    tickGlowingEffect(player, gameArena);
                }

                for (GameListener listener : gameArena.getGameListeners()) {
                    listener.onPlayerMove(gameArena, player, location, playerTeam);
                }
            }
        }
    }

    private static void tickGlowingEffect(Player player, GameArena gameArena) {
        Player currentlyGlowing = null;
        Player nearest = null;
        Player clone = null;
        double distance = gameArena.getLiveSettings().getKillDistance().getCurrentValue();
        Team playerTeam = gameArena.getPlayerTeam(player);

        for (Player inGame : gameArena.getPlayers()) {
            if (GlowingManager.isGlowing(inGame, player)) {
                currentlyGlowing = inGame;
            }
            double currentDistance;
            if (gameArena.getCamHandler() != null && gameArena.getCamHandler().isOnCam(inGame, gameArena)){
                clone = gameArena.getCamHandler().getClone(inGame.getUniqueId());
                if (clone == null){
                    continue;
                } else {
                    currentDistance = clone.getLocation().distance(player.getLocation());
                }
            } else {
                currentDistance = inGame.getLocation().distance(player.getLocation());
            }
            if (currentDistance <= distance) {
                if (playerTeam != null && playerTeam.canKill(inGame)) {
                    nearest = inGame;
                    distance = currentDistance;
                } else {
                    // maybe nearest can kill him and is not moving, so refresh
                    Team nearestTeam = gameArena.getPlayerTeam(inGame);
                    if (nearestTeam != null && nearestTeam.canKill(player)) {
                        if (gameArena.getCamHandler() != null && gameArena.getCamHandler().isOnCam(inGame, gameArena)){
                            break;
                        }
                        Player nearestsTarget = null;
                        for (Player inGame2 : gameArena.getPlayers()) {
                            if (GlowingManager.isGlowing(inGame2, inGame)) {
                                nearestsTarget = inGame2;
                                break;
                            }
                        }
                        if (nearestsTarget == null) {
                            GlowingManager.setGlowingRed(player, inGame, gameArena);
                        } else if (!nearestsTarget.equals(player) && (gameArena.getCamHandler() != null && !gameArena.getCamHandler().isOnCam(inGame, gameArena)) && currentDistance < nearestsTarget.getLocation().distance(inGame.getLocation())) {
                            GlowingManager.getInstance().removeGlowing(nearestsTarget, inGame);
                            GlowingManager.setGlowingRed(player, inGame, gameArena);
                        }
                    }
                }
            } else {
                // if exiting a killer range update glowing on someone in his range if is not moving
                if (GlowingManager.isGlowing(player, inGame)) {
                    GlowingManager.getInstance().removeGlowing(player, inGame);
                    // todo send glowing on nearest player to inGame
                }
                //clone = null;
            }
        }
        if (currentlyGlowing == null && gameArena.getCamHandler() != null) {
            for (Player cloned : gameArena.getCamHandler().getClones()) {
                if (GlowingManager.isGlowing(cloned, player)) {
                    currentlyGlowing = cloned;
                }
            }
        }
        if (currentlyGlowing != null) {
            if (currentlyGlowing.equals(clone == null ? nearest : clone)) {
                return;
            }
            GlowingManager.getInstance().removeGlowing(currentlyGlowing, player);
        }
        if (nearest != null) {
            GlowingManager.setGlowingRed(clone == null ? nearest : clone, player, gameArena);
        }
    }
}
