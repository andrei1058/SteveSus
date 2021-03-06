package com.andrei1058.stevesus.common.selector;

import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.api.arena.DisplayableArena;
import com.andrei1058.stevesus.common.api.arena.GameState;
import com.andrei1058.stevesus.common.api.gui.BaseGUI;
import com.andrei1058.stevesus.common.api.gui.CustomHolder;
import com.andrei1058.stevesus.common.api.gui.slot.StaticSlot;
import com.andrei1058.stevesus.common.api.locale.CommonLocale;
import com.andrei1058.stevesus.common.api.locale.CommonMessage;
import com.andrei1058.stevesus.common.gui.ItemUtil;
import com.andrei1058.stevesus.common.selector.config.SelectorConfig;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArenaGUI extends BaseGUI {

    private final String guiName;

    /**
     * Create a GUI from the given pattern.
     * <p>
     * #########
     * ####x####
     * #########
     * <p>
     * This will create a GUI from the given pattern.
     */
    public ArenaGUI(String guiName, List<String> pattern, CommonLocale lang) {
        super(pattern, lang, new ArenaSelectorHolder(), lang.hasPath(CommonMessage.ARENA_SELECTOR_GUI_NAME + "-" + guiName) ?
                lang.getMsg(null, CommonMessage.ARENA_SELECTOR_GUI_NAME + "-" + guiName) : lang.getMsg(null, CommonMessage.ARENA_SELECTOR_GUI_NAME));
        this.guiName = guiName;

        YamlConfiguration yml = SelectorManager.getINSTANCE().getSelectorConfig().getYml();
        String path = guiName + "." + SelectorConfig.SELECTOR_GENERIC_REPLACE_PATH;
        for (String replacementString : yml.getConfigurationSection(path).getKeys(false)) {
            if (replacementString.toCharArray().length > 1) {
                CommonManager.getINSTANCE().getPlugin().getLogger().warning("Invalid char '" + replacementString + "' on selector replacements: " + guiName);
                continue;
            }
            char character = replacementString.charAt(0);
            String type = yml.getString(path + "." + character + ".type");
            if (type == null) {
                CommonManager.getINSTANCE().getPlugin().getLogger().warning("Could not retrieve replacement type for '" + character + "' on selector: " + guiName);
            } else {
                if (type.equalsIgnoreCase("arena") || type.equalsIgnoreCase("game") || type.equalsIgnoreCase("template")) {
                    withReplacement(character, new ArenaSlot(yml.getString(path + "." + character + ".filter-template"), yml.getString(path + "." + character + ".filter-status")));

                } else if (type.equalsIgnoreCase("cmd") || type.equalsIgnoreCase("command") || type.equalsIgnoreCase("item")) {
                    List<String> tags = new ArrayList<>();
                    if (!type.equalsIgnoreCase("item")) {
                        String cmdP = yml.getString(path + "." + character + ".commands.as-player");
                        if (cmdP != null && !cmdP.isEmpty()) {
                            tags.add(SelectorManager.NBT_P_CMD_KEY);
                            tags.add(cmdP);
                        }
                        String cmdC = yml.getString(path + "." + character + ".commands.as-console");
                        if (cmdC != null && !cmdC.isEmpty()) {
                            tags.add(SelectorManager.NBT_C_CMD_KEY);
                            tags.add(cmdC);
                        }
                    }
                    String namePath = CommonMessage.SELECTOR_REPLACEMENT_ITEM_NAME_PATH.toString().replace("{s}", getGuiName()).replace("{r}", String.valueOf(character));
                    String lorePath = CommonMessage.SELECTOR_REPLACEMENT_ITEM_LORE_PATH.toString().replace("{s}", getGuiName()).replace("{r}", String.valueOf(character));
                    if (!CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getDefaultLocale().hasPath(namePath)) {
                        // create path on default language
                        CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getDefaultLocale().setMsg(namePath, "&7" + character);
                    }
                    if (!CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getDefaultLocale().hasPath(lorePath)) {
                        // create path on default language
                        CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getDefaultLocale().setList(lorePath, new ArrayList<>());
                    }
                    ItemStack item = ItemUtil.createItem(yml.getString(path + "." + character + ".item.material"),
                            (byte) yml.getInt(path + "." + character + ".item.data"), yml.getInt(path + "." + character + ".item.amount"),
                            yml.getBoolean(path + "." + character + ".item.enchanted"), tags);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(getLang().getMsg(null, namePath));
                        meta.setLore(getLang().getMsgList(null, lorePath));
                        item.setItemMeta(meta);
                    }
                    withReplacement(character, new StaticSlot(item));
                }
            }
        }
    }

    @Override
    public void refresh() {
        getRefreshableSlots().forEach((key, value) -> {
            AtomicInteger entry = new AtomicInteger(-1);
            if (key instanceof ArenaSlot) {
                ArenaSlot as = (ArenaSlot) key;
                Stream<DisplayableArena> toFilter = CommonManager.getINSTANCE().getCommonProvider().getArenas().stream();
                if (!as.getStatusFilter().isEmpty()) {
                    toFilter = toFilter.filter(arena -> as.getStatusFilter().contains(arena.getGameState()));
                }
                if (!as.getTemplatesFilter().isEmpty()) {
                    toFilter = toFilter.filter(arena -> as.getTemplatesFilter().contains(arena.getTemplateWorld()));
                }
                List<DisplayableArena> filtered = toFilter.collect(Collectors.toList());
                value.forEach(slot -> {
                    entry.getAndIncrement();
                    ItemStack displayItem = null;
                    if (filtered.size() > entry.get()) {
                        displayItem = filtered.get(entry.get()).getDisplayItem(getLang());
                    }
                    getInventory().setItem(slot, displayItem == null ? BaseGUI.AIR : displayItem);
                });
            } else {
                value.forEach(slot -> {
                    ItemStack displayItem = key.getSlotItem(entry.incrementAndGet(), getLang(), null);
                    getInventory().setItem(slot, displayItem == null ? BaseGUI.AIR : displayItem);
                });
            }
        });
    }

    public String getGuiName() {
        return guiName;
    }

    public static class ArenaSelectorHolder implements CustomHolder {

        private BaseGUI gui;

        @Override
        public Inventory getInventory() {
            return null;
        }

        @Override
        public BaseGUI getGui() {
            return gui;
        }

        @Override
        public void setGui(BaseGUI gui) {
            this.gui = gui;
        }

        @Override
        public void onClick(Player player, ItemStack itemStack, ClickType clickType) {
            String tag = CommonManager.getINSTANCE().getItemSupport().getTag(itemStack, CommonManager.getINSTANCE().getCommonProvider().getDisplayableArenaNBTTagKey());
            if (tag != null) {
                if (player.getOpenInventory() != null) {
                    player.closeInventory();
                }
                DisplayableArena arena = CommonManager.getINSTANCE().getCommonProvider().getFromTag(tag);
                if (arena != null) {
                    if (arena.getGameState() == GameState.WAITING || arena.getGameState() == GameState.STARTING) {
                        arena.joinPlayer(player, false);
                    } else {
                        arena.joinSpectator(player, null);
                    }
                    return;
                }
            }
            tag = CommonManager.getINSTANCE().getItemSupport().getTag(itemStack, SelectorManager.NBT_P_CMD_KEY);
            if (tag != null && !tag.isEmpty()) {
                if (player.getOpenInventory() != null) {
                    player.closeInventory();
                }
                for (String cmd : tag.split("\\n")) {
                    Bukkit.dispatchCommand(player, cmd);
                }
            }
            tag = CommonManager.getINSTANCE().getItemSupport().getTag(itemStack, SelectorManager.NBT_C_CMD_KEY);
            if (tag != null && !tag.isEmpty()) {
                if (player.getOpenInventory() != null) {
                    player.closeInventory();
                }
                for (String cmd : tag.split("\\n")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", player.getName()));
                }
            }
        }

        @Override
        public boolean isStatic() {
            return true;
        }
    }
}
