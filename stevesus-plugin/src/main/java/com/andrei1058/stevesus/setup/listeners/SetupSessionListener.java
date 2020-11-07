package com.andrei1058.stevesus.setup.listeners;

import com.andrei1058.stevesus.api.setup.SetupSession;
import com.andrei1058.stevesus.setup.SetupManager;
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
        setupSession.getSetupListeners().forEach(listener -> listener.onPlayerInteract(event));
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event){
        //if (event.isCancelled()) return;
        SetupSession setupSession = SetupManager.getINSTANCE().getSession(event.getPlayer());
        if (setupSession == null) return;
        setupSession.getSetupListeners().forEach(listener -> listener.onPlayerInteractEntity(event));
    }

    @EventHandler
    public void onInteractAtEntity(PlayerInteractAtEntityEvent event){
        //if (event.isCancelled()) return;
        SetupSession setupSession = SetupManager.getINSTANCE().getSession(event.getPlayer());
        if (setupSession == null) return;
        setupSession.getSetupListeners().forEach(listener -> listener.onPlayerInteractAtEntity(event));
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event){
        if (event.isCancelled()) return;
        SetupSession setupSession = SetupManager.getINSTANCE().getSession(event.getPlayer());
        if (setupSession == null) return;
        setupSession.getSetupListeners().forEach(listener -> listener.onPlayerDropItem(event));
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event){
        //if (event.isCancelled()) return;
        if (event.getEntity().getType() != EntityType.PLAYER) return;
        SetupSession setupSession = SetupManager.getINSTANCE().getSession(event.getEntity().getWorld().getName());
        if (setupSession == null) return;
        setupSession.getSetupListeners().forEach(listener -> listener.onPlayerPickupItem(event));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        //if (event.isCancelled()) return;
        SetupSession setupSession = SetupManager.getINSTANCE().getSession(event.getPlayer());
        if (setupSession == null) return;
        setupSession.getSetupListeners().forEach(listener -> listener.onBlockBreak(event));
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        //if (event.isCancelled()) return;
        SetupSession setupSession = SetupManager.getINSTANCE().getSession(event.getPlayer());
        if (setupSession == null) return;
        setupSession.getSetupListeners().forEach(listener -> listener.onBlockPlace(event));
    }

    @EventHandler
    public void onHangingPlace(HangingPlaceEvent event){
        //if (event.isCancelled()) return;
        SetupSession setupSession = SetupManager.getINSTANCE().getSession(event.getPlayer());
        if (setupSession == null) return;
        setupSession.getSetupListeners().forEach(listener -> listener.onHangingPlace(event));
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent event){
        //if (event.isCancelled()) return;
        SetupSession setupSession = SetupManager.getINSTANCE().getSession(event.getEntity().getWorld().getName());
        if (setupSession == null) return;
        setupSession.getSetupListeners().forEach(listener -> listener.onHangingBreakByEntity(event));
    }

    @EventHandler
    public void onHandSwitch(PlayerSwapHandItemsEvent event){
        if (event.isCancelled()) return;
        SetupSession setupSession = SetupManager.getINSTANCE().getSession(event.getPlayer());
        if (setupSession == null) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onHangingBreak(HangingBreakEvent event){
        //if (event.isCancelled()) return;
        SetupSession setupSession = SetupManager.getINSTANCE().getSession(event.getEntity().getWorld().getName());
        if (setupSession == null) return;
        setupSession.getSetupListeners().forEach(listener -> listener.onHangingBreak(event));
    }

    @EventHandler
    public void onHangingBreak(EntityDamageByEntityEvent event){
        //if (event.isCancelled()) return;
        SetupSession setupSession = SetupManager.getINSTANCE().getSession(event.getEntity().getWorld().getName());
        if (setupSession == null) return;
        setupSession.getSetupListeners().forEach(listener -> listener.onEntityDamageByEntity(event));
    }

    public void unRegister(){
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
