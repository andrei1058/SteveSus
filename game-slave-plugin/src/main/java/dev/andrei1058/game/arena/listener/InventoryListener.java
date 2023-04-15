package dev.andrei1058.game.arena.listener;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.arena.GameListener;
import dev.andrei1058.game.arena.ArenaManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;

public class InventoryListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        // Make it so they can't toggle their armor
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        final Player player = (Player) event.getPlayer();
        GameArena gameArena = ArenaManager.getINSTANCE().getArenaByPlayer(player);
        if (gameArena == null) return;
        for (GameListener listener : gameArena.getGameListeners()){
            listener.onInventoryClose(gameArena, player, event.getInventory());
        }
    }
}
