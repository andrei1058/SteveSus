package com.andrei1058.stevesus.arena.listener;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.arena.ArenaHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

public class BreakPlaceListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled()) return;
        Arena arena = ArenaHandler.getINSTANCE().getArenaByPlayer(e.getPlayer());
        if (arena != null){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.isCancelled()) return;
        Arena arena = ArenaHandler.getINSTANCE().getArenaByPlayer(e.getPlayer());
        if (arena != null){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onIceMelt(BlockFadeEvent e) {
        if (e.isCancelled()) return;
        Arena arena = ArenaHandler.getINSTANCE().getArenaByWorld(e.getBlock().getWorld().getName());
        if (arena != null){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent e) {
        if (e.isCancelled()) return;
        Arena arena = ArenaHandler.getINSTANCE().getArenaByPlayer(e.getPlayer());
        if (arena != null){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        if (e.isCancelled()) return;
        Arena arena = ArenaHandler.getINSTANCE().getArenaByPlayer(e.getPlayer());
        if (arena != null){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        if (e.isCancelled()) return;
        Arena arena = ArenaHandler.getINSTANCE().getArenaByWorld(e.getLocation().getWorld().getName());
        if (arena != null){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e) {
        if (e.isCancelled()) return;
        Arena arena = ArenaHandler.getINSTANCE().getArenaByWorld(e.getBlock().getWorld().getName());
        if (arena != null){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPaintingRemove(HangingBreakByEntityEvent e) {
        if (e.isCancelled()) return;
        Arena arena = ArenaHandler.getINSTANCE().getArenaByWorld(e.getEntity().getWorld().getName());
        if (arena != null){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        if (e.isCancelled()) return;
        Arena arena = ArenaHandler.getINSTANCE().getArenaByWorld(e.getBlock().getWorld().getName());
        if (arena != null){
            e.setCancelled(true);
        }
    }
}
