package com.andrei1058.stevesus.arena.listener;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.GameListener;
import com.andrei1058.stevesus.api.arena.PlayerCorpse;
import com.andrei1058.stevesus.api.arena.team.Team;
import com.andrei1058.stevesus.arena.ArenaManager;
import com.andrei1058.stevesus.hook.glowing.GlowingManager;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerMoveListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.isCancelled()) return;
        Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer(event.getPlayer());
        if (arena == null) return;
        if (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getZ() != event.getTo().getZ() || event.getFrom().getBlockY() != event.getTo().getBlockY()) {
            final Location location = event.getFrom();
            final Player player = event.getPlayer();
            if (arena.isCantMove(player)) {
                location.setYaw(event.getTo().getYaw());
                location.setPitch(event.getTo().getPitch());
                location.setZ(event.getFrom().getBlockZ() + 0.5);
                location.setX(event.getFrom().getBlockX() + 0.5);
                player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
            } else {
                Team playerTeam = arena.getPlayerTeam(player);
                for (PlayerCorpse corpse : arena.getDeadBodies()) {
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

                if (arena.getKillDistance() > 0) {
                    tickGlowingEffect(player, arena);
                }

                for (GameListener listener : arena.getGameListeners()){
                    listener.onPlayerMove(arena, player, location, playerTeam);
                }
            }
        }
    }

    private static void tickGlowingEffect(Player player, Arena arena) {
        Player currentlyGlowing = null;
        Player nearest = null;
        double distance = arena.getKillDistance();
        Team playerTeam = arena.getPlayerTeam(player);

        for (Player inGame : arena.getPlayers()) {
            if (GlowingManager.isGlowing(inGame, player)) {
                currentlyGlowing = inGame;
            }
            double currentDistance;
            if ((currentDistance = inGame.getLocation().distance(player.getLocation())) <= distance) {
                if (playerTeam != null && playerTeam.canKill(inGame)) {
                    nearest = inGame;
                    distance = currentDistance;
                } else {
                    // maybe nearest can kill him and is not moving, so refresh
                    Team nearestTeam = arena.getPlayerTeam(inGame);
                    if (nearestTeam != null && nearestTeam.canKill(player)) {
                        Player nearestsTarget = null;
                        for (Player inGame2 : arena.getPlayers()) {
                            if (GlowingManager.isGlowing(inGame2, inGame)) {
                                nearestsTarget = inGame2;
                                break;
                            }
                        }
                        if (nearestsTarget == null) {
                            GlowingManager.setGlowing(player, inGame);
                        } else if (!nearestsTarget.equals(player) && currentDistance < nearestsTarget.getLocation().distance(inGame.getLocation())) {
                            GlowingManager.removeGlowing(nearestsTarget, inGame);
                            GlowingManager.setGlowing(player, inGame);
                        }
                    }
                }
            } else {
                GlowingManager.removeGlowing(player, inGame);
            }
        }
        if (currentlyGlowing != null) {
            if (currentlyGlowing.equals(nearest)){
                return;
            }
            if (currentlyGlowing.getLocation().distance(player.getLocation()) > arena.getKillDistance()) {
                GlowingManager.removeGlowing(currentlyGlowing, player);
            }
        }
        if (nearest != null) {
            GlowingManager.setGlowing(nearest, player);
        }
    }
}
