package com.andrei1058.stevesus.arena.listener.spectator;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.arena.ArenaManager;
import com.andrei1058.stevesus.commanditem.JoinItemsManager;
import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.api.gui.CustomHolder;
import com.andrei1058.stevesus.teleporter.TeleporterGUI;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

/**
 * All in one spectator listeners.
 * Prevent building, interacting etc.
 */
public class SpectatorListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.isCancelled()) return;
        Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer(e.getPlayer());
        if (arena == null) return;

        // disable spectator interact
        if (arena.isSpectator(e.getPlayer())) {

            // allow command items only
            if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                ItemStack itemStack = CommonManager.getINSTANCE().getItemSupport().getInHand(e.getPlayer());
                if (itemStack != null) {
                    if (itemStack.getType() != Material.AIR) {
                        if (CommonManager.getINSTANCE().getItemSupport().hasTag(itemStack, JoinItemsManager.INTERACT_NBT_TAG_PLAYER_CMDS)
                                || CommonManager.getINSTANCE().getItemSupport().hasTag(itemStack, JoinItemsManager.INTERACT_NBT_TAG_CONSOLE_CMDS)) {
                            return;
                        }
                    }
                }
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpectatorClick(InventoryClickEvent e) {
        if (e.getWhoClicked().getGameMode() == GameMode.SPECTATOR) {
            // cancel usage of gm3 tools
            e.setCancelled(true);

        } else if (e.getClickedInventory() != null && !(e.getClickedInventory().getHolder() instanceof TeleporterGUI.TeleporterSelectorHolder)) {
            // cancel usage of inventory if not custom
            if (e.getWhoClicked() instanceof Player && ArenaManager.getINSTANCE().isSpectating((Player) e.getWhoClicked())) {
                e.setCancelled(true);
            }
        }
    }

    // first person spectate
    @EventHandler
    public void onEntityInteract(PlayerInteractAtEntityEvent e) {
        if (e.isCancelled()) return;
        if (e.getRightClicked().getType() != EntityType.PLAYER) return;

        Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer(e.getPlayer());
        if (arena == null) return;
        e.setCancelled(true);
        arena.startFirstPersonSpectate(e.getPlayer(), (Player) e.getRightClicked());
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        if (e.isCancelled()) return;
        Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer(e.getPlayer());
        if (arena == null) return;
        if (arena.isSpectator(e.getPlayer()) && arena.isFirstPersonSpectate(e.getPlayer())) {
            arena.stopFirstPersonSpectate(e.getPlayer());
        }
    }

    @EventHandler
    public void onSpectatorInteract(PlayerInteractEntityEvent e) {
        Arena a = ArenaManager.getINSTANCE().getArenaByPlayer(e.getPlayer());
        if (a == null) return;
        if (a.isPlayer(e.getPlayer())) return;
        e.setCancelled(true);
    }

    // stop first person
    @EventHandler
    public void onTargetDeath(PlayerDeathEvent e) {
        Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer(e.getEntity());
        if (arena == null) return;
        arena.getSpectators().forEach(spectator -> {
            if (spectator.getSpectatorTarget() != null && spectator.getSpectatorTarget().equals(e.getEntity())) {
                arena.stopFirstPersonSpectate(spectator);
            }
        });
    }

    // Disable hits from spectators
    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        Arena arena = ArenaManager.getINSTANCE().getArenaByWorld(e.getEntity().getWorld().getName());
        if (arena == null) return;

        Player damager = null;
        if (e.getDamager() instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) e.getDamager()).getShooter();
            if (shooter instanceof Player) {
                damager = (Player) shooter;
            }
        } else if (e.getDamager() instanceof Player) {
            damager = (Player) e.getDamager();
        } else if (e.getDamager() instanceof TNTPrimed) {
            TNTPrimed tnt = (TNTPrimed) e.getDamager();
            if (tnt.getSource() instanceof Player) {
                damager = (Player) tnt.getSource();
            }
        }
        if (damager == null) return;
        if (arena.isSpectator(damager)) {
            e.setCancelled(true);
        }
    }
}
