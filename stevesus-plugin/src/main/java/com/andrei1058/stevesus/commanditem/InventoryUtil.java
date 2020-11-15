package com.andrei1058.stevesus.commanditem;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InventoryUtil {

    /**
     * Clear inventory, remove effects, levels, xp and so on.
     */
    public static void wipePlayer(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setExp(0);
        player.setLevel(0);
        player.setHealthScale(20);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.getInventory().clear();
        player.setAllowFlight(false);
        player.setFlying(false);
        player.getActivePotionEffects().forEach(potion -> player.removePotionEffect(potion.getType()));
    }

    public static void clearStorageContents(Player player){
        for (ItemStack itemStack : player.getInventory().getStorageContents()){
            if (itemStack == null) continue;
            player.getInventory().remove(itemStack);
        }
    }
}
