package com.andrei1058.stevesus.api.setup.util;

import com.andrei1058.stevesus.api.glow.GlowColor;
import com.andrei1058.stevesus.api.glow.GlowingBox;
import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.gui.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class SelectTargetBlocks {

    private static int itemIdIndex = 0;

    private final List<Location> setBlock = new ArrayList<>();
    private final List<GlowingBox> glowingBox = new ArrayList<>();
    private final ItemStack addItem;
    private final ItemStack removeItem;

    private Function<Player, Void> addListener;
    private Function<Player, Void> removeListener;

    private int addItemSlot = 3;
    private int removeItemSlot = 4;

    private final int itemId = ++itemIdIndex;

    public SelectTargetBlocks(String addItemName, String removeItemName) {
        addItem = ItemUtil.createItem(ItemUtil.getMaterial("DIAMOND", "DIAMOND"), (byte) 0, 1, false, Arrays.asList("selectT" + itemId, "set"), addItemName, null);
        removeItem = ItemUtil.createItem(ItemUtil.getMaterial("FLINT", "FLINT"), (byte) 0, 1, false, Arrays.asList("selectT" + itemId, "remove"), removeItemName, null);
    }

    public void setAddListener(Function<Player, Void> addListener) {
        this.addListener = addListener;
    }

    public void setRemoveListener(Function<Player, Void> removeListener) {
        this.removeListener = removeListener;
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
        if (player.hasCooldown(itemStack.getType())) return true;
        if (tag.equals("remove")) {
            if (setBlock.isEmpty()) return false;
            player.setCooldown(itemStack.getType(), 20);
            player.sendTitle(" ", ChatColor.RED + "Location removed!", 0, 30, 0);
            int entry = setBlock.size() - 1;
            setBlock.remove(entry);
            GlowingBox glowingBox = this.glowingBox.size() < entry ? null : this.glowingBox.remove(entry);
            if (glowingBox != null) {
                glowingBox.stopGlowing(player);
                glowingBox.getMagmaCube().remove();
            }
            if (removeListener != null) {
                removeListener.apply(player);
            }
            return true;
        } else if (tag.equals("set")) {
            player.setCooldown(itemStack.getType(), 20);
            Block newBlock = player.getTargetBlock(null, 3);
            if (newBlock == null) {
                player.sendTitle(" ", ChatColor.RED + "You need to target a block!", 0, 60, 0);
                return true;
            }
            player.sendTitle(" ", ChatColor.GREEN + "Location added!", 0, 40, 0);
            setBlock.add(newBlock.getLocation());
            GlowingBox glowingBox = new GlowingBox(newBlock.getLocation().clone().add(0.5, 0, 0.5), 2, GlowColor.GREEN);
            glowingBox.startGlowing(player);
            this.glowingBox.add(glowingBox);
            if (addListener != null) {
                addListener.apply(player);
            }
            return true;
        }
        return false;
    }

    /**
     * Get set location.
     */
    public List<Location> getSetBlocks() {
        return setBlock;
    }

    public List<GlowingBox> getGlowingBox() {
        return glowingBox;
    }
}