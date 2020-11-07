package com.andrei1058.stevesus.server.multiarena;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Inventory backups are used in MULTI_ARENA mode to restore players inventories when they go back to lobby.
 * This might help support other plugins of "join items" type that are given when a player joins a multi
 * arena server but if he stats playing an arena such a plugin wouldn't know to give the items back when he
 * finishes the game so that's it.
 */
public class InventoryBackup {

    // list of inventories
    private static final LinkedList<InventoryBackup> inventories = new LinkedList<>();

    // inventory holder
    private final UUID owner;
    // inv items
    private final LinkedHashMap<ItemStack, Integer> items = new LinkedHashMap<>();
    // active potions in lobby
    private final List<PotionEffect> potions = new ArrayList<>();
    // armor in lobby
    private ItemStack[] armor;
    // gm in lobby
    private GameMode gamemode;
    // if was flying in lobby
    private boolean allowFlight, flying;

    /**
     * A backup should be created before switching worlds.
     */
    private InventoryBackup(Player player) {
        this.owner = player.getUniqueId();

        int x = 0;
        for (ItemStack i : player.getInventory()) {
            if (i != null) {
                if (i.getType() != Material.AIR) {
                    this.items.put(i, x);
                }
            }
            x++;
        }
        this.potions.addAll(player.getActivePotionEffects());
        this.armor = player.getInventory().getArmorContents();
        this.gamemode = player.getGameMode();
        this.allowFlight = player.getAllowFlight();
        this.flying = player.isFlying();
    }

    /**
     * Restore contents.
     */
    private void restore(Player player) {
        // clear current inventory
        player.getInventory().clear();

        // restore lobby items
        if (!items.isEmpty()) {
            for (Map.Entry<ItemStack, Integer> entry : items.entrySet()) {
                player.getInventory().setItem(entry.getValue(), entry.getKey());
            }
            player.updateInventory();
            items.clear();
        }
        // restore potion effects from lobby
        if (!potions.isEmpty()) {
            for (PotionEffect pe : potions) {
                player.addPotionEffect(pe);
            }
            potions.clear();
        }
        // restore armor
        player.getInventory().setArmorContents(armor);
        // restore lobby game mode
        player.setGameMode(gamemode);
        // restore flight state
        player.setAllowFlight(allowFlight);
        player.setFlying(flying);
    }

    /**
     * Will create an inventory backup.
     *
     * @param player target player.
     */
    public static void createInventoryBackup(Player player) {
        // delete old backup
        if (hasBackup(player.getUniqueId())) {
            dropOnQuit(player.getUniqueId());
        }
        inventories.add(new InventoryBackup(player));
    }

    /**
     * Will restore a player's inventory if backed up before.
     * This won't clear the inventory, you should do it yourself.
     *
     * @param player target.
     */
    public static void restoreInventory(Player player) {
        InventoryBackup backup = getBackup(player.getUniqueId());
        if (backup != null){
            backup.restore(player);
        }
    }

    /**
     * This is used as alternative of {@link #restoreInventory} if the player left the server
     * directly from an arena.
     */
    public static void dropOnQuit(UUID player) {
        inventories.removeIf(backup -> backup.owner.equals(player));
    }

    /**
     * Check if a player has a backup.
     */
    public static boolean hasBackup(UUID uuid) {
        return inventories.stream().anyMatch(backup -> backup.owner.equals(uuid));
    }

    /**
     * Get a player backup.
     */
    @Nullable
    public static InventoryBackup getBackup(UUID player) {
        return inventories.stream().filter(backup -> backup.owner.equals(player)).findFirst().orElse(null);
    }
}
