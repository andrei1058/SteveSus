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

    default void onPlayerInteract(SetupSession setupSession, PlayerInteractEvent event) {

    }

    default void onPlayerInteractEntity(SetupSession setupSession, PlayerInteractEntityEvent event) {

    }

    default void onPlayerInteractAtEntity(SetupSession setupSession, PlayerInteractAtEntityEvent event) {

    }

    default void onPlayerDropItem(SetupSession setupSession, PlayerDropItemEvent event) {

    }

    /**
     * Player check already made internally.
     */
    default void onPlayerPickupItem(SetupSession setupSession, EntityPickupItemEvent event) {

    }

    default void onBlockBreak(SetupSession setupSession, BlockBreakEvent event) {

    }

    default void onBlockPlace(SetupSession setupSession, BlockPlaceEvent event) {

    }

    /**
     * When an item frame or painting is placed etc.
     */
    default void onHangingPlace(SetupSession setupSession, HangingPlaceEvent event) {

    }

    /**
     * When an item frame or painting is broken by an entity.
     */
    default void onHangingBreakByEntity(SetupSession setupSession, HangingBreakByEntityEvent event) {

    }

    /**
     * When an item frame or painting is broken by natural events
     * like when the block behind got broken.
     */
    default void onHangingBreak(SetupSession setupSession, HangingBreakEvent event) {

    }

    /**
     * Useful to protect items in item frames.
     */
    default void onEntityDamageByEntity(SetupSession setupSession, EntityDamageByEntityEvent event) {

    }

    default void onSetupPerClose(SetupSession setupSession) {

    }
}
