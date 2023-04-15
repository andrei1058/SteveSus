package dev.andrei1058.game.setup.listeners;

import dev.andrei1058.game.api.setup.SetupSession;
import dev.andrei1058.game.setup.SetupManager;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.*;

public class SetupSessionListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        //if (event.isCancelled()) return;
        SetupSession setupSession = SetupManager.getINSTANCE().getSession(event.getPlayer());
        if (setupSession == null) return;
        setupSession.getSetupListeners().forEach(listener -> listener.onPlayerInteract(setupSession, event));
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        //if (event.isCancelled()) return;
        SetupSession setupSession = SetupManager.getINSTANCE().getSession(event.getPlayer());
        if (setupSession == null) return;
        setupSession.getSetupListeners().forEach(listener -> listener.onPlayerInteractEntity(setupSession, event));
    }

    @EventHandler
    public void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
        //if (event.isCancelled()) return;
        SetupSession setupSession = SetupManager.getINSTANCE().getSession(event.getPlayer());
        if (setupSession == null) return;
        setupSession.getSetupListeners().forEach(listener -> listener.onPlayerInteractAtEntity(setupSession, event));
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (event.isCancelled()) return;
        SetupSession setupSession = SetupManager.getINSTANCE().getSession(event.getPlayer());
        if (setupSession == null) return;
        setupSession.getSetupListeners().forEach(listener -> listener.onPlayerDropItem(setupSession, event));
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        //if (event.isCancelled()) return;
        if (event.getEntity().getType() != EntityType.PLAYER) return;
        SetupSession setupSession = SetupManager.getINSTANCE().getSession(event.getEntity().getWorld().getName());
        if (setupSession == null) return;
        setupSession.getSetupListeners().forEach(listener -> listener.onPlayerPickupItem(setupSession, event));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        //if (event.isCancelled()) return;
        SetupSession setupSession = SetupManager.getINSTANCE().getSession(event.getPlayer());
        if (setupSession == null) return;
        setupSession.getSetupListeners().forEach(listener -> listener.onBlockBreak(setupSession, event));
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        //if (event.isCancelled()) return;
        SetupSession setupSession = SetupManager.getINSTANCE().getSession(event.getPlayer());
        if (setupSession == null) return;
        setupSession.getSetupListeners().forEach(listener -> listener.onBlockPlace(setupSession, event));
    }

    @EventHandler
    public void onHangingPlace(HangingPlaceEvent event) {
        //if (event.isCancelled()) return;
        SetupSession setupSession = SetupManager.getINSTANCE().getSession(event.getPlayer());
        if (setupSession == null) return;
        setupSession.getSetupListeners().forEach(listener -> listener.onHangingPlace(setupSession, event));
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        //if (event.isCancelled()) return;
        SetupSession setupSession = SetupManager.getINSTANCE().getSession(event.getEntity().getWorld().getName());
        if (setupSession == null) return;
        setupSession.getSetupListeners().forEach(listener -> listener.onHangingBreakByEntity(setupSession, event));
    }

    @EventHandler
    public void onHandSwitch(PlayerSwapHandItemsEvent event) {
        if (event.isCancelled()) return;
        SetupSession setupSession = SetupManager.getINSTANCE().getSession(event.getPlayer());
        if (setupSession == null) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onHangingBreak(HangingBreakEvent event) {
        //if (event.isCancelled()) return;
        SetupSession setupSession = SetupManager.getINSTANCE().getSession(event.getEntity().getWorld().getName());
        if (setupSession == null) return;
        setupSession.getSetupListeners().forEach(listener -> listener.onHangingBreak(setupSession, event));
    }

    @EventHandler
    public void onHangingBreak(EntityDamageByEntityEvent event) {
        //if (event.isCancelled()) return;
        SetupSession setupSession = SetupManager.getINSTANCE().getSession(event.getEntity().getWorld().getName());
        if (setupSession == null) return;
        setupSession.getSetupListeners().forEach(listener -> listener.onEntityDamageByEntity(setupSession, event));
    }

    public void unRegister() {
        PlayerInteractEvent.getHandlerList().unregister(this);
        PlayerInteractEntityEvent.getHandlerList().unregister(this);
        PlayerInteractAtEntityEvent.getHandlerList().unregister(this);
        PlayerDropItemEvent.getHandlerList().unregister(this);
        EntityPickupItemEvent.getHandlerList().unregister(this);
        BlockBreakEvent.getHandlerList().unregister(this);
        BlockPlaceEvent.getHandlerList().unregister(this);
        HangingPlaceEvent.getHandlerList().unregister(this);
        HangingBreakByEntityEvent.getHandlerList().unregister(this);
        HangingBreakEvent.getHandlerList().unregister(this);
    }
}
