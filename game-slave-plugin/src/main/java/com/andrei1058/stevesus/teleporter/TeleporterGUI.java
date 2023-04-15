package com.andrei1058.stevesus.teleporter;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.locale.Locale;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.arena.ArenaManager;
import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.api.gui.BaseGUI;
import com.andrei1058.stevesus.common.api.gui.CustomHolder;
import com.andrei1058.stevesus.common.api.gui.slot.StaticSlot;
import com.andrei1058.stevesus.common.gui.ItemUtil;
import com.andrei1058.stevesus.common.selector.SelectorManager;
import com.andrei1058.stevesus.teleporter.config.TeleporterConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class TeleporterGUI extends BaseGUI {

    private static final String NBT_TELEPORTER_TARGET_UUID = "autt-1058-a";


    public TeleporterGUI(String guiName, List<String> pattern, Player player, Locale lang, Arena arena) {
        super(pattern, lang, new TeleporterSelectorHolder(), (lang.hasPath(Message.TELEPORTER_GUI_NAME + "-" + guiName) ?
                lang.getMsg(player,Message.TELEPORTER_GUI_NAME + "-" + guiName) : lang.getMsg(player, Message.TELEPORTER_GUI_NAME))
                .replace("{spectator}", player.getDisplayName()).replace("{spectator_raw}", player.getName()));

        // load content
        YamlConfiguration yml = TeleporterManager.getInstance().getTeleporterConfig().getYml();
        String path = guiName + "." + TeleporterConfig.TELEPORTER_GENERIC_REPLACE_PATH;

        for (String replacementString : yml.getConfigurationSection(path).getKeys(false)) {
            if (replacementString.toCharArray().length > 1) {
                CommonManager.getINSTANCE().getPlugin().getLogger().warning("Invalid char '" + replacementString + "' on statsGUI replacements: " + guiName);
                continue;
            }

            if (yml.getBoolean(path + "." + replacementString + ".teleporter")) {
                final int[] currentPlayer = {-1};
                List<Integer> slots = this.getReplacementSlots(replacementString.charAt(0));
                slots.forEach(slot -> this.getInventory().setItem(slot, getItemStack(yml, path, arena, replacementString, ++currentPlayer[0], player, guiName)));
            } else {
                withReplacement(replacementString.charAt(0), new StaticSlot(getItemStack(yml, path, arena, replacementString, 0, player, guiName)));
            }
        }
    }

    private ItemStack getItemStack(YamlConfiguration yml, String path, Arena arena, String replacementString, int currentPlayer, Player player, String guiName) {
        Player targetPlayer = null;

        // if current item should point to a player
        if (yml.getBoolean(path + "." + replacementString + ".teleporter")) {
            if (arena.getPlayers().size() > currentPlayer) {
                targetPlayer = arena.getPlayers().get(currentPlayer);
            } else {
                return new ItemStack(Material.AIR);
            }
        }

        // apply commands on click
        List<String> tags = new ArrayList<>();
        String cmdP = yml.getString(path + "." + replacementString + ".commands.as-player");
        if (targetPlayer != null) {
            tags.add(NBT_TELEPORTER_TARGET_UUID);
            tags.add(targetPlayer.getUniqueId().toString());
        }
        if (cmdP != null && !cmdP.isEmpty()) {
            tags.add(SelectorManager.NBT_P_CMD_KEY);
            String command = cmdP.replace("{player}", player.getDisplayName()).replace("{player_raw}", player.getName()).replace("{player_uuid}", player.getUniqueId().toString());
            if (targetPlayer != null) {
                command = command.replace("{target}", targetPlayer.getDisplayName()).replace("{target_raw}", targetPlayer.getName()).replace("{target_uuid}", targetPlayer.getUniqueId().toString());
            }
            tags.add(command);
        }
        String cmdC = yml.getString(path + "." + replacementString + ".commands.as-console");
        if (cmdC != null && !cmdC.isEmpty()) {
            tags.add(SelectorManager.NBT_C_CMD_KEY);
            String command = cmdC.replace("{player}", player.getDisplayName()).replace("{player_raw}", player.getName()).replace("{player_uuid}", player.getUniqueId().toString());
            if (targetPlayer != null) {
                command = command.replace("{target}", targetPlayer.getDisplayName()).replace("{target_raw}", targetPlayer.getName()).replace("{target_uuid}", targetPlayer.getUniqueId().toString());
            }
            tags.add(command);
        }

        String namePath = Message.TELEPORTER_REPLACEMENT_ITEM_NAME_PATH.toString().replace("{s}", guiName).replace("{r}", replacementString);
        String lorePath = Message.TELEPORTER_REPLACEMENT_ITEM_LORE_PATH.toString().replace("{s}", guiName).replace("{r}", replacementString);
        if (!CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getDefaultLocale().hasPath(namePath)) {
            // create path on default language
            CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getDefaultLocale().setMsg(namePath, "&7" + replacementString);
        }
        if (!CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getDefaultLocale().hasPath(lorePath)) {
            // create path on default language
            CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getDefaultLocale().setList(lorePath, new ArrayList<>());
        }
        ItemStack item = ItemUtil.createItem(yml.getString(path + "." + replacementString + ".item.material"),
                (byte) yml.getInt(path + "." + replacementString + ".item.data"), yml.getInt(path + "." + replacementString + ".item.amount"),
                yml.getBoolean(path + "." + replacementString + ".item.enchanted"), tags);

        // apply skin if head
        if (CommonManager.getINSTANCE().getItemSupport().isPlayerHead(item)) {
            // apply holder skin
            if (targetPlayer == null) {
                item = CommonManager.getINSTANCE().getItemSupport().applyPlayerSkinOnHead(player, item);
            } else {
                // apply target skin
                item = CommonManager.getINSTANCE().getItemSupport().applyPlayerSkinOnHead(targetPlayer, item);
            }
        }

        ItemMeta meta = item.getItemMeta();

        // air does not have meta
        if (meta != null) {
            String displayName = getLang().getMsg(player, namePath).replace("{player}", player.getDisplayName()).replace("{player_raw}", player.getName()).replace("{player_uuid}", player.getUniqueId().toString());

            // if target item
            if (targetPlayer != null) {
                displayName = displayName.replace("{target}", targetPlayer.getDisplayName()).replace("{target_raw}", targetPlayer.getName()).replace("{target_uuid}", targetPlayer.getUniqueId().toString());
            }
            meta.setDisplayName(displayName);
            List<String> newLore = new LinkedList<>();

            if (targetPlayer == null){
                meta.setLore(getLang().getMsgList(player, lorePath, new String[]{"{player}", player.getDisplayName(), "{player_raw}", player.getName(), "{player_uuid}", player.getUniqueId().toString()}));
            } else {
                meta.setLore(getLang().getMsgList(targetPlayer, lorePath, new String[]{"{player}", player.getDisplayName(), "{player_raw}", player.getName(), "{player_uuid}", player.getUniqueId().toString(),
                "{target}", targetPlayer.getDisplayName(), "{target_raw}", targetPlayer.getName(), "{target_uuid}", targetPlayer.getUniqueId().toString()}));
            }

            item.setItemMeta(meta);
        }
        return item;
    }

    public static class TeleporterSelectorHolder implements CustomHolder {

        @Override
        public Inventory getInventory() {
            return null;
        }

        @Override
        public BaseGUI getGui() {
            return null;
        }

        @Override
        public void setGui(BaseGUI gui) {
        }

        @Override
        public void onClick(Player player, ItemStack itemStack, ClickType clickType) {
            String tag = CommonManager.getINSTANCE().getItemSupport().getTag(itemStack, SelectorManager.NBT_P_CMD_KEY);
            if (tag != null && !tag.isEmpty()) {
                for (String cmd : tag.split("\\n")) {
                    Bukkit.dispatchCommand(player, cmd);
                }
                if (player.getOpenInventory() != null) {
                    player.closeInventory();
                }
            }
            tag = CommonManager.getINSTANCE().getItemSupport().getTag(itemStack, SelectorManager.NBT_C_CMD_KEY);
            if (tag != null && !tag.isEmpty()) {
                for (String cmd : tag.split("\\n")) {
                    // {player} is the one who clicks, {target} is the target's head.
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }
                if (player.getOpenInventory() != null) {
                    player.closeInventory();
                }
            }
            tag = CommonManager.getINSTANCE().getItemSupport().getTag(itemStack, NBT_TELEPORTER_TARGET_UUID);
            if (tag != null && !tag.isEmpty()) {
                Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer(player);
                if (arena != null) {
                    player.closeInventory();
                    UUID targetUUID;
                    try {
                        targetUUID = UUID.fromString(tag);
                    } catch (IllegalArgumentException ex) {
                        return;
                    }
                    Player target = Bukkit.getPlayer(targetUUID);
                    if (target == null) {
                        return;
                    }
                    Arena targetArena = ArenaManager.getINSTANCE().getArenaByPlayer(target);
                    // in case player stayed with open GUI a long time and target left
                    if (targetArena == null || !targetArena.equals(arena)) {
                        return;
                    }

                    player.teleport(target, PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
            }
        }

        @Override
        public boolean isStatic() {
            return true;
        }
    }
}
