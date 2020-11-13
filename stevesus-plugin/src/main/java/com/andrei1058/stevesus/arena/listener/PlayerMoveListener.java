package com.andrei1058.stevesus.arena.listener;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.arena.ArenaManager;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerMoveListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.isCancelled()) return;
        Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer(event.getPlayer());
        if (arena == null) return;
        if (arena.isCantMove(event.getPlayer())) {
            int minY = Math.min(event.getFrom().getBlockY(), event.getTo().getBlockY());
            int maxY = Math.max(event.getFrom().getBlockY(), event.getTo().getBlockY());
            if (event.getFrom().getBlockX() != event.getTo().getBlockX() || event.getFrom().getBlockZ() != event.getTo().getBlockZ() || maxY - minY > 2) {
                Location newLocation = event.getFrom();
                newLocation.setYaw(event.getTo().getYaw());
                newLocation.setPitch(event.getTo().getPitch());
                newLocation.setZ(event.getFrom().getBlockZ() + 0.5);
                newLocation.setX(event.getFrom().getBlockX() + 0.5);
                event.getPlayer().teleport(newLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
            }
        }
    }
}
