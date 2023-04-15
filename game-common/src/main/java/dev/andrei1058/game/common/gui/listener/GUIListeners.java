package dev.andrei1058.game.common.gui.listener;

import dev.andrei1058.game.common.api.gui.CustomHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;

public class GUIListeners implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.isCancelled()) return;
        if (e.getCurrentItem() == null) return;
        if (e.getWhoClicked() == null) return;
        if (e.getClickedInventory().getHolder() == null) return;
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getClickedInventory().getHolder() instanceof CustomHolder) {
            e.setCancelled(true);
            ((CustomHolder) e.getClickedInventory().getHolder()).onClick((Player) e.getWhoClicked(), e.getCurrentItem(), e.getClick());
            ((CustomHolder) e.getClickedInventory().getHolder()).onClick((Player) e.getWhoClicked(), e.getCurrentItem(), e.getClick(), e.getSlot());
        }
    }

    @EventHandler
    public void onItemMove(InventoryMoveItemEvent e) {
        if (e.isCancelled()) return;
        if ((e.getDestination().getHolder() instanceof CustomHolder
                || e.getSource().getHolder() instanceof CustomHolder)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.isCancelled()) return;
        if (e.getInventory().getHolder() instanceof CustomHolder) {
            e.setCancelled(true);
        }
    }
}
