package com.andrei1058.stevesus.common.gui;

import com.andrei1058.stevesus.common.CommonManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemUtil {

    public static ItemStack createItem(String material, byte data, int amount, boolean enchanted, List<String> tags) {
        Material mat = Material.matchMaterial(material);
        if (mat == null) {
            mat = Material.BEDROCK;
        }

        ItemStack item = CommonManager.getINSTANCE().getItemSupport().createItem(mat, Math.max(amount, 1), (byte) Math.max(data, 0));
        if (tags.size() > 1) {
            for (int i = 0; i < tags.size(); i += 2) {
                item = CommonManager.getINSTANCE().getItemSupport().addTag(item, tags.get(i), tags.get(i+1));
            }
        }
        ItemMeta meta = item.getItemMeta();

        // air does not have meta
        if (meta == null) return item;
        if (enchanted) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Get material for current server version.
     */
    public static String getMaterial(String mat1_12, String mat1_13) {
        return CommonManager.SERVER_VERSION < 13 ? mat1_12 : mat1_13;
    }
}
