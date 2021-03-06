package com.andrei1058.stevesus.common.stats.config;

import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.selector.SelectorManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class StatsConfig {

    public static final String STATS_GENERIC_PATTERN_PATH = "pattern";
    public static final String STATS_GENERIC_REPLACE_PATH = "replacements";

    private YamlConfiguration yml;
    private File config;
    private boolean firstTime = false;

    /**
     * Create a new configuration file.
     *
     * @param plugin config owner.
     * @param name   config name. Do not include .yml in it.
     */
    public StatsConfig(Plugin plugin, File directory, String name) {
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                plugin.getLogger().log(Level.SEVERE, "Could not create " + SelectorManager.getINSTANCE().getSelectorDirectory().getPath());
                return;
            }
        }

        config = new File(directory, name + ".yml");
        if (!config.exists()) {
            firstTime = true;
            plugin.getLogger().log(Level.INFO, "Creating " + config.getPath());
            try {
                if (!config.createNewFile()) {
                    plugin.getLogger().log(Level.SEVERE, "Could not create " + config.getPath());
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        yml = YamlConfiguration.loadConfiguration(config);
        yml.options().copyDefaults(true);

        yml.options().header("Plugin by andrei1058.\n" +
                "Replacements example:\n" +
                "'x':\n" +
                "  commands: \n" +
                "    as-player: myStoreLink, au stats someOtherGUI, etc\n" +
                "    as-console: openDonations {player}\n" +
                "  item:\n" +
                "    material: DIAMOND\n" +
                "    data:0 (yes, data. I'm supporting 1.12.)\n" +
                "    enchanted: false\n" +
                "    amount: 1");

        yml.addDefault("main." + STATS_GENERIC_PATTERN_PATH, Arrays.asList("#########", "#*-##)))#", "#))##abc#", "#########", "##defg###","##))))###"));

        yml.addDefault("main." + STATS_GENERIC_REPLACE_PATH + ".*." + "commands.as-player", "");
        yml.addDefault("main." + STATS_GENERIC_REPLACE_PATH + ".*." + "commands.as-console", "");
        yml.addDefault("main." + STATS_GENERIC_REPLACE_PATH + ".*." + "item.material", CommonManager.SERVER_VERSION < 13 ? "BOOK_AND_QUILL" : "WRITABLE_BOOK");
        yml.addDefault("main." + STATS_GENERIC_REPLACE_PATH + ".*." + "item.data", 0);
        yml.addDefault("main." + STATS_GENERIC_REPLACE_PATH + ".*." + "item.enchanted", false);
        yml.addDefault("main." + STATS_GENERIC_REPLACE_PATH + ".*." + "item.amount", 1);

        yml.addDefault("main." + STATS_GENERIC_REPLACE_PATH + ".-." + "commands.as-player", "");
        yml.addDefault("main." + STATS_GENERIC_REPLACE_PATH + ".-." + "commands.as-console", "");
        yml.addDefault("main." + STATS_GENERIC_REPLACE_PATH + ".-." + "item.material", CommonManager.SERVER_VERSION < 13 ? "WATCH" : "CLOCK");
        yml.addDefault("main." + STATS_GENERIC_REPLACE_PATH + ".-." + "item.data", 0);
        yml.addDefault("main." + STATS_GENERIC_REPLACE_PATH + ".-." + "item.enchanted", false);
        yml.addDefault("main." + STATS_GENERIC_REPLACE_PATH + ".-." + "item.amount", 1);

        yml.addDefault("main." + STATS_GENERIC_REPLACE_PATH + ".)." + "item.material", CommonManager.SERVER_VERSION < 13 ? "STAINED_GLASS_PANE" : "GRAY_STAINED_GLASS_PANE");
        yml.addDefault("main." + STATS_GENERIC_REPLACE_PATH + ".)." + "item.data", 7);
        yml.addDefault("main." + STATS_GENERIC_REPLACE_PATH + ".#." + "item.material", "AIR");

        yml.addDefault("main." + STATS_GENERIC_REPLACE_PATH + ".a." + "item.material", CommonManager.SERVER_VERSION < 13 ? "BANNER" : "WHITE_BANNER");
        yml.addDefault("main." + STATS_GENERIC_REPLACE_PATH + ".a." + "item.data", 15);

        yml.addDefault("main." + STATS_GENERIC_REPLACE_PATH + ".b." + "item.material", CommonManager.SERVER_VERSION < 13 ? "BANNER" : "LIME_BANNER");
        yml.addDefault("main." + STATS_GENERIC_REPLACE_PATH + ".b." + "item.data", 10);

        yml.addDefault("main." + STATS_GENERIC_REPLACE_PATH + ".c." + "item.material", CommonManager.SERVER_VERSION < 13 ? "BANNER" : "RED_BANNER");
        yml.addDefault("main." + STATS_GENERIC_REPLACE_PATH + ".c." + "item.data", 1);

        yml.addDefault("main." + STATS_GENERIC_REPLACE_PATH + ".d." + "item.material", CommonManager.SERVER_VERSION < 13 ? "REDSTONE_TORCH_ON" : "REDSTONE_TORCH");
        yml.addDefault("main." + STATS_GENERIC_REPLACE_PATH + ".e." + "item.material", "FLINT");
        yml.addDefault("main." + STATS_GENERIC_REPLACE_PATH + ".f." + "item.material", "SHIELD");
        yml.addDefault("main." + STATS_GENERIC_REPLACE_PATH + ".g." + "item.material", "PAPER");
        save();
    }

    /**
     * Reload configuration.
     */
    @SuppressWarnings("unused")
    public void reload() {
        yml = YamlConfiguration.loadConfiguration(config);
    }

    /**
     * Set data to config
     */
    public void set(String path, Object value) {
        yml.set(path, value);
        save();
    }

    /**
     * Get yml instance
     */
    public YamlConfiguration getYml() {
        return yml;
    }

    /**
     * Save config changes to file
     */
    public void save() {
        try {
            yml.save(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get list of strings at given path
     *
     * @return a list of string with colors translated
     */
    public List<String> getList(String path) {
        return yml.getStringList(path).stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList());
    }

    /**
     * Get boolean at given path
     */
    public boolean getBoolean(String path) {
        return yml.getBoolean(path);
    }

    /**
     * Get Integer at given path
     */
    @SuppressWarnings("unused")
    public int getInt(String path) {
        return yml.getInt(path);
    }


    /**
     * Get string at given path
     */
    public String getString(String path) {
        return yml.getString(path);
    }

    /**
     * Check if the config file was created for the first time
     * Can be used to add default values
     */
    @SuppressWarnings("unused")
    public boolean isFirstTime() {
        return firstTime;
    }
}
