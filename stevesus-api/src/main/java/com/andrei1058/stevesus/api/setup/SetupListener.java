package com.andrei1058.stevesus.api.setup;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * A setup listener is used when you require to listen some actions
 * like player interact when someone is doing a map setup.
 * <p>
 * You should use this listener and not raw bukkit events because this is unregistered
 * when a setup session is closed.
 */
public interface SetupListener {

    void onPlayerInteract(PlayerInteractEvent event);

    void onPlayerInteractEntity(PlayerInteractEntityEvent event);

    void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event);

    void onPlayerDropItem(PlayerDropItemEvent event);

    /**
     * Player check already made internally.
     */
    void onPlayerPickupItem(EntityPickupItemEvent event);

    void onBlockBreak(BlockBreakEvent event);

    void onBlockPlace(BlockPlaceEvent event);

    /**
     * When an item frame or painting is placed etc.
     */
    void onHangingPlace(HangingPlaceEvent event);

    /**
     * When an item frame or painting is broken by an entity.
     */
    void onHangingBreakByEntity(HangingBreakByEntityEvent event);

    /**
     * When an item frame or painting is broken by natural events
     * like when the block behind got broken.
     */
    void onHangingBreak(HangingBreakEvent event);

    /**
     * Useful to protect items in item frames.
     */
    void onEntityDamageByEntity(EntityDamageByEntityEvent event);
}
