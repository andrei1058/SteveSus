package com.andrei1058.stevesus.api.setup.util;

import com.andrei1058.stevesus.api.SteveSusAPI;
import com.andrei1058.stevesus.api.glow.GlowColor;
import com.andrei1058.stevesus.api.glow.GlowingBox;
import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.gui.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class SelectTargetBlock {

    private static int itemIdIndex = 0;

    private Location setBlock;
    private GlowingBox glowingBox;
    private final ItemStack addItem;
    private final ItemStack removeItem;

    private int addItemSlot = 3;
    private int removeItemSlot = 4;

    private final int itemId = ++itemIdIndex;

    public SelectTargetBlock(String addItemName, String removeItemName) {
        addItem = ItemUtil.createItem(ItemUtil.getMaterial("DIAMOND", "DIAMOND"), (byte) 2, 1, false, Arrays.asList("selectT" + itemId, "set"), addItemName, null);
        removeItem = ItemUtil.createItem(ItemUtil.getMaterial("FLINT", "FLINT"), (byte) 4, 1, false, Arrays.asList("selectT" + itemId, "remove"), removeItemName, null);
    }

    /**
     * Give setup items to the player.
     */
    public void giveItems(Player player) {
        player.getInventory().setItem(addItemSlot, addItem);
        player.getInventory().setItem(removeItemSlot, removeItem);
    }

    /**
     * Change item position in inv.
     */
    public void setAddItemSlot(int addItemSlot) {
        this.addItemSlot = addItemSlot;
    }

    /**
     * Change item position in inv.
     */
    public void setRemoveItemSlot(int removeItemSlot) {
        this.removeItemSlot = removeItemSlot;
    }

    /**
     * On item interact.
     * Handle set or remove.
     *
     * @return true if interact event must be cancelled.
     */
    public boolean onItemInteract(ItemStack itemStack, Player player) {
        if (itemStack == null) return false;
        if (itemStack.getType() == Material.AIR) return false;
        String tag = CommonManager.getINSTANCE().getItemSupport().getTag(itemStack, "selectT" + itemId);
        if (tag == null) return false;
        if (player.hasCooldown(itemStack.getType())) return false;
        if (tag.equals("remove")) {
            player.setCooldown(itemStack.getType(), 20);
            player.sendTitle(" ", ChatColor.RED + "Location un-set!", 0, 30, 0);
            setBlock = null;
            if (glowingBox != null) {
                glowingBox.stopGlowing(player);
                glowingBox.getMagmaCube().remove();
            }
            return true;
        } else if (tag.equals("set")) {
            player.setCooldown(itemStack.getType(), 20);
            Block newBlock = player.getTargetBlock(null, 3);
            if (newBlock == null) {
                player.sendTitle(" ", ChatColor.RED + "You need to target a block!", 0, 60, 0);
                return true;
            }
            if (setBlock != null) {
                player.sendTitle(" ", ChatColor.GREEN + "Location replaced!", 0, 40, 0);
                if (glowingBox != null) {
                    glowingBox.stopGlowing(player);
                    glowingBox.getMagmaCube().remove();
                }
            } else {
                player.sendTitle(" ", ChatColor.GREEN + "Location set!", 0, 40, 0);
            }
            setBlock = newBlock.getLocation();
            glowingBox = new GlowingBox(setBlock.clone().add(0.5, 0, 0.5), 2, GlowColor.GREEN);
            glowingBox.startGlowing(player);
            return true;
        }
        return false;
    }

    /**
     * Get set location.
     */
    @Nullable
    public Location getSetBlock() {
        return setBlock;
    }
}
