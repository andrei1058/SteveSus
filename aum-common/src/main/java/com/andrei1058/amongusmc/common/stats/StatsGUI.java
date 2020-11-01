package com.andrei1058.amongusmc.common.stats;

import com.andrei1058.amongusmc.common.CommonManager;
import com.andrei1058.amongusmc.common.gui.ItemUtil;
import com.andrei1058.amongusmc.common.selector.SelectorManager;
import com.andrei1058.amongusmc.common.stats.config.StatsConfig;
import com.andrei1058.amoungusmc.common.api.gui.BaseGUI;
import com.andrei1058.amoungusmc.common.api.gui.CustomHolder;
import com.andrei1058.amoungusmc.common.api.gui.slot.StaticSlot;
import com.andrei1058.amoungusmc.common.api.locale.CommonLocale;
import com.andrei1058.amoungusmc.common.api.locale.CommonMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class StatsGUI extends BaseGUI {

    protected StatsGUI(String guiName, List<String> pattern, Player player, CommonLocale lang) {
        super(pattern, lang, new StatsGUI.StatsSelectorHolder(), (lang.hasPath(CommonMessage.STATS_GUI_NAME + "-" + guiName) ?
                lang.getMsg(player, CommonMessage.STATS_GUI_NAME + "-" + guiName) : lang.getMsg(player, CommonMessage.STATS_GUI_NAME))
                .replace("{player}", player.getDisplayName()).replace("{name}", player.getName()));

        YamlConfiguration yml = StatsManager.getINSTANCE().getStatsGUIConfig().getYml();
        String path = guiName + "." + StatsConfig.STATS_GENERIC_REPLACE_PATH;
        for (String replacementString : yml.getConfigurationSection(path).getKeys(false)) {
            if (replacementString.toCharArray().length > 1) {
                CommonManager.getINSTANCE().getPlugin().getLogger().warning("Invalid char '" + replacementString + "' on statsGUI replacements: " + guiName);
                continue;
            }
            List<String> tags = new ArrayList<>();
            String cmdP = yml.getString(path + "." + replacementString + ".commands.as-player");
            if (cmdP != null && !cmdP.isEmpty()) {
                tags.add(SelectorManager.NBT_P_CMD_KEY);
                tags.add(cmdP);
            }
            String cmdC = yml.getString(path + "." + replacementString + ".commands.as-console");
            if (cmdC != null && !cmdC.isEmpty()) {
                tags.add(SelectorManager.NBT_C_CMD_KEY);
                tags.add(cmdC);
            }
            String namePath = CommonMessage.STATS_REPLACEMENT_ITEM_NAME_PATH.toString().replace("{s}", guiName).replace("{r}", replacementString);
            String lorePath = CommonMessage.STATS_REPLACEMENT_ITEM_LORE_PATH.toString().replace("{s}", guiName).replace("{r}", replacementString);
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
            ItemMeta meta = item.getItemMeta();

            // air does not have meta
            if (meta != null) {
                meta.setDisplayName(StatsManager.getINSTANCE().replaceStats(player, getLang().getMsg(player, namePath)));
                List<String> statsLore = new LinkedList<>();
                getLang().getMsgList(player, lorePath).forEach(string -> statsLore.add(StatsManager.getINSTANCE().replaceStats(player, string)));
                meta.setLore(statsLore);
                item.setItemMeta(meta);
            }
            withReplacement(replacementString.charAt(0), new StaticSlot(item));
        }
    }

    public static class StatsSelectorHolder implements CustomHolder {

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
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", player.getName()));
                }
                if (player.getOpenInventory() != null) {
                    player.closeInventory();
                }
            }
        }

        @Override
        public boolean isStatic() {
            return true;
        }
    }
}
