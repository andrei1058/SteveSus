package com.andrei1058.amongusmc.common.selector;

import com.andrei1058.amongusmc.common.CommonManager;
import com.andrei1058.amoungusmc.common.api.gui.BaseGUI;
import com.andrei1058.amoungusmc.common.api.locale.CommonLocale;
import com.andrei1058.amongusmc.common.gui.listener.GUIListeners;
import com.andrei1058.amongusmc.common.selector.command.SelectorCommand;
import com.andrei1058.amongusmc.common.selector.config.SelectorConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;

public class SelectorManager {

    public static final String NBT_P_CMD_KEY = "pl-cmd-1058";
    public static final String NBT_C_CMD_KEY = "co-cmd-1058";

    private static SelectorManager INSTANCE;
    private SelectorConfig selectorConfig;
    private final File selectorDirectory;

    // a gui per language, gui name, gui instance
    private final HashMap<CommonLocale, HashMap<String, ArenaGUI>> arenaGUIs = new HashMap<>();

    private SelectorManager(Plugin plugin, File selectorDirectory) {
        this.selectorDirectory = selectorDirectory;
        // register gui listeners
        Bukkit.getPluginManager().registerEvents(new GUIListeners(), plugin);
    }

    public void openToPlayer(Player player, CommonLocale lang, String guiName) {
        HashMap<String, ArenaGUI> selectors = arenaGUIs.get(lang);
        if (selectors == null) return;
        ArenaGUI selector = selectors.get(guiName);
        if (selector == null) return;
        selector.open(player);
    }

    public SelectorConfig getSelectorConfig() {
        return selectorConfig;
    }

    /**
     * To be used when an arena changes
     */
    public void refreshArenaSelector() {
        arenaGUIs.forEach((lang, map) -> map.forEach((name, gui) -> gui.refresh()));
    }

    public static SelectorManager getINSTANCE() {
        return INSTANCE;
    }

    // this is initialized in common manager
    public static void init(Plugin plugin, String selectorDirectory) {
        if (INSTANCE == null) {

            File selectorDir = plugin.getDataFolder();
            // change directory eventually
            if (!selectorDirectory.isEmpty()) {
                File newPath = new File(selectorDirectory);
                if (newPath.isDirectory()) {
                    selectorDir = newPath;
                    plugin.getLogger().info("Set selector configuration path to: " + selectorDirectory);
                } else {
                    plugin.getLogger().warning("Tried to set selector configuration path to: " + selectorDirectory + " but it does not seem like a directory.");
                }
            }
            INSTANCE = new SelectorManager(plugin, selectorDir);

            // save or load config
            INSTANCE.selectorConfig = new SelectorConfig(plugin, "layout_selector");

            // load selector guis
            for (String guiName : getINSTANCE().getSelectorConfig().getYml().getConfigurationSection("").getKeys(false)) {
                if (guiName == null) continue;
                if (guiName.isEmpty()) continue;
                if (BaseGUI.validatePattern(getINSTANCE().getSelectorConfig().getList(guiName + "." + SelectorConfig.SELECTOR_GENERIC_PATTERN_PATH))) {
                    CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getEnabledCommonLocales().forEach(lang -> {
                        HashMap<String, ArenaGUI> map = getINSTANCE().arenaGUIs.get(lang);
                        if (map == null) {
                            map = new HashMap<>();
                        }
                        map.putIfAbsent(guiName, new ArenaGUI(guiName, getINSTANCE().getSelectorConfig().getList(guiName + "." + SelectorConfig.SELECTOR_GENERIC_PATTERN_PATH), lang));
                        if (getINSTANCE().arenaGUIs.containsKey(lang)) {
                            getINSTANCE().arenaGUIs.replace(lang, map);
                        } else {
                            getINSTANCE().arenaGUIs.put(lang, map);
                        }
                    });
                } else {
                    plugin.getLogger().warning("Could not validate selector pattern: " + guiName);
                }
            }

            // append selector sub command to the main command
            SelectorCommand.append(CommonManager.getINSTANCE().getCommonProvider().getMainCommand());
        }
    }

    public File getSelectorDirectory() {
        return selectorDirectory;
    }
}
