package com.andrei1058.stevesus.common.api.selector;

import com.andrei1058.stevesus.common.api.arena.GameState;
import com.andrei1058.stevesus.common.api.locale.CommonMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

public enum ArenaHolderConfig {

    SELECTOR_WAITING("waiting", "CONCRETE", "GREEN_GLAZED_TERRACOTTA", (byte) 5, false),
    SELECTOR_STARTING("starting", "CONCRETE", "YELLOW_GLAZED_TERRACOTTA", (byte) 4, false),
    SELECTOR_PLAYING("playing", "CONCRETE", "ORANGE_GLAZED_TERRACOTTA", (byte) 1, false),
    SELECTOR_ENDING("ending", "CONCRETE", "RED_GLAZED_TERRACOTTA", (byte) 14, false);


    public static final String MAIN_PATH = "game-selector-display-item";
    public byte SERVER_VERSION = Byte.parseByte(Bukkit.getServer().getClass().getName().split("\\.")[3].split("_")[1]);

    private final String path;
    private final String material;
    private final short data;
    private final boolean enchanted;

    ArenaHolderConfig(String path, String mat_12, String mat_13, short data, boolean enchanted) {
        this.path = MAIN_PATH + "." + path;
        this.material = SERVER_VERSION >= 13 ? mat_13 : mat_12;
        this.data = data;
        this.enchanted = enchanted;
    }

    /**
     * Export default values to a yml configuration.
     *
     * @param yaml config.
     */
    @SuppressWarnings("unused")
    public void export(YamlConfiguration yaml) {
        yaml.addDefault(materialPath(), material);
        yaml.addDefault(dataPath(), data);
        yaml.addDefault(enchantedPath(), enchanted);
    }

    public String materialPath() {
        return path + ".material";
    }

    public String dataPath() {
        return path + ".data";
    }

    public String enchantedPath() {
        return path + ".enchanted";
    }

    public String getMaterial() {
        return material;
    }

    public short getData() {
        return data;
    }

    public boolean isEnchanted() {
        return enchanted;
    }

    public static ArenaHolderConfig getForState(GameState state) {
        switch (state) {
            case WAITING:
                return SELECTOR_WAITING;
            case STARTING:
                return SELECTOR_STARTING;
            case IN_GAME:
                return SELECTOR_PLAYING;
            default:
                return SELECTOR_ENDING;
        }
    }

    public static CommonMessage getNameForState(GameState state) {
        switch (state) {
            case WAITING:
                return CommonMessage.SELECTOR_DISPLAY_ITEM_WAITING_NAME;
            case STARTING:
                return CommonMessage.SELECTOR_DISPLAY_ITEM_STARTING_NAME;
            case IN_GAME:
                return CommonMessage.SELECTOR_DISPLAY_ITEM_PLAYING_NAME;
            default:
                return CommonMessage.SELECTOR_DISPLAY_ITEM_ENDING_NAME;
        }
    }

    public static CommonMessage getLoreForState(GameState state) {
        switch (state) {
            case WAITING:
                return CommonMessage.SELECTOR_DISPLAY_ITEM_WAITING_LORE;
            case STARTING:
                return CommonMessage.SELECTOR_DISPLAY_ITEM_STARTING_LORE;
            case IN_GAME:
                return CommonMessage.SELECTOR_DISPLAY_ITEM_PLAYING_LORE;
            default:
                return CommonMessage.SELECTOR_DISPLAY_ITEM_ENDING_LORE;
        }
    }
}
