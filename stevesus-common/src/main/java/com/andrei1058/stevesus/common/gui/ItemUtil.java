package com.andrei1058.stevesus.common.gui;

import com.andrei1058.stevesus.common.CommonManager;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemUtil {

    public static ItemStack createItem(String material, byte data, int amount, boolean enchanted, @Nullable List<String> tags) {
        Material mat = Material.matchMaterial(material);
        if (mat == null) {
            mat = Material.BEDROCK;
        }

        ItemStack item = CommonManager.getINSTANCE().getItemSupport().createItem(mat, Math.max(amount, 1), (byte) Math.max(data, 0));
        if (tags != null && tags.size() > 1) {
            for (int i = 0; i < tags.size(); i += 2) {
                item = CommonManager.getINSTANCE().getItemSupport().addTag(item, tags.get(i), tags.get(i + 1));
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
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createItem(String material, byte data, int amount, boolean enchanted, @Nullable List<String> tags, String displayName, @Nullable List<String> lore) {
        Material mat = Material.matchMaterial(material);
        if (mat == null) {
            mat = Material.BEDROCK;
        }
        return createItem(mat, data, amount, enchanted, tags, displayName, lore);
    }

    public static ItemStack createItem(Material mat, byte data, int amount, boolean enchanted, @Nullable List<String> tags, String displayName, @Nullable List<String> lore) {

        ItemStack item = CommonManager.getINSTANCE().getItemSupport().createItem(mat, Math.max(amount, 1), (byte) Math.max(data, 0));
        if (tags != null && tags.size() > 1) {
            for (int i = 0; i < tags.size(); i += 2) {
                item = CommonManager.getINSTANCE().getItemSupport().addTag(item, tags.get(i), tags.get(i + 1));
            }
        }
        ItemMeta meta = item.getItemMeta();

        // air does not have meta
        if (meta == null) return item;
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        if (lore != null && !lore.isEmpty()) {
            List<String> newLore = new ArrayList<>();
            lore.forEach(string -> newLore.add(ChatColor.translateAlternateColorCodes('&', string)));
            meta.setLore(newLore);
        }
        if (enchanted) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Get material for current server version.
     */
    public static String getMaterial(String mat1_12, String mat1_13) {
        return CommonManager.SERVER_VERSION < 13 ? mat1_12 : mat1_13;
    }

    /**
     * Create a skull with custom skin.
     *
     * @param url  skin url from mojang.
     * @param name skull name.
     */
    public static ItemStack createSkullWithSkin(String url, String name) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);

        if (url.isEmpty()) return head;

        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        headMeta.setDisplayName(name);
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        byte[] encodedData = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        Field profileField;
        try {
            profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
            e1.printStackTrace();
        }
        head.setItemMeta(headMeta);
        return head;
    }

}
