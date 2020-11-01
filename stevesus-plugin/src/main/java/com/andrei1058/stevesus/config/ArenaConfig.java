package com.andrei1058.stevesus.config;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.configurationdata.CommentsConfiguration;
import ch.jalu.configme.properties.*;
import com.andrei1058.stevesus.common.api.selector.ArenaHolderConfig;
import com.andrei1058.stevesus.config.properties.OrphanLocationProperty;
import org.bukkit.Location;

import java.util.ArrayList;

public class ArenaConfig implements SettingsHolder {

    private ArenaConfig() {
    }

    @Comment({"This will override the arena map name in messages.", "Leave this empty to use arena (folder) name as display name."})
    public static final Property<String> DISPLAY_NAME = new StringProperty("display-name", "");

    @Comment("Should this arena be automatically enabled at server start up?")
    public static final Property<Boolean> LOAD_AT_START_UP = new BooleanProperty("load-at-start-up", true);

    @Comment({" ", "How many active copies?", "This is not used in BUNGEE_LEGACY mode."})
    public static final Property<Integer> CLONES_AVAILABLE_AT_ONCE = new IntegerProperty("clones-available-at-once", 2);

    @Comment({" ",
            "Leave this empty if you want to allow all players to join and spectate.",
            "Eliminated players will always be able to spectate the game."})
    public static final Property<String> SPECTATE_PERM = new StringProperty("spectate-permission", "");

    @Comment({" ", "How many players are required before starting the countdown?"})
    public static final Property<Integer> MIN_PLAYERS = new IntegerProperty("min-players", 4);

    @Comment({" ", "How many players are allowed to join this game?"})
    public static final Property<Integer> MAX_PLAYERS = new IntegerProperty("max-players", 10);

    @Comment({" ", "Which teleporter layout to use in this arena?", "Leave empty for default."})
    public static final Property<String> TELEPORTER_LAYOUT = new StringProperty("teleporter-layout", "");

    @Comment({" ", "Will spawn players in sequence if you set more than one location."})
    public static final ListProperty<Location> WAITING_LOBBY_LOCATIONS = new ListProperty<>("waiting-lobby.spawn-locations", new OrphanLocationProperty(), new ArrayList<>());

    @Comment({" ", "Will spawn players in sequence if you set more than one location."})
    public static final ListProperty<Location> SPECTATE_LOCATIONS = new ListProperty<>("spectator-system.spawn-locations", new OrphanLocationProperty(), new ArrayList<>());

    @Comment("Set this to true if you want to store player data on a database.")
    public static final OptionalProperty<Integer> GAME_COUNTDOWN_INITIAL = new OptionalProperty<>(new IntegerProperty(MainConfig.GAME_COUNTDOWN_INITIAL.getPath().split("\\.")[1] + "." + MainConfig.GAME_COUNTDOWN_INITIAL.getPath().split("\\.")[2], 70));
    @Comment("Used when the maximum amount of players is reached. Time shortener.")
    public static final OptionalProperty<Integer> GAME_COUNTDOWN_SHORTENED = new OptionalProperty<>(new IntegerProperty(MainConfig.GAME_COUNTDOWN_SHORTENED.getPath().split("\\.")[1] + "." + MainConfig.GAME_COUNTDOWN_SHORTENED.getPath().split("\\.")[2], 20));


    public static final Property<String> SELECTOR_WAITING_MATERIAL = new StringProperty(ArenaHolderConfig.SELECTOR_WAITING.materialPath(), ArenaHolderConfig.SELECTOR_WAITING.getMaterial());
    public static final Property<Integer> SELECTOR_WAITING_DATA = new IntegerProperty(ArenaHolderConfig.SELECTOR_WAITING.dataPath(), (int) ArenaHolderConfig.SELECTOR_WAITING.getData());
    public static final Property<Boolean> SELECTOR_WAITING_ENCHANT = new BooleanProperty(ArenaHolderConfig.SELECTOR_WAITING.enchantedPath(), ArenaHolderConfig.SELECTOR_WAITING.isEnchanted());

    public static final Property<String> SELECTOR_STARTING_MATERIAL = new StringProperty(ArenaHolderConfig.SELECTOR_STARTING.materialPath(), ArenaHolderConfig.SELECTOR_STARTING.getMaterial());
    public static final Property<Integer> SELECTOR_STARTING_DATA = new IntegerProperty(ArenaHolderConfig.SELECTOR_STARTING.dataPath(), (int) ArenaHolderConfig.SELECTOR_STARTING.getData());
    public static final Property<Boolean> SELECTOR_STARTING_ENCHANT = new BooleanProperty(ArenaHolderConfig.SELECTOR_STARTING.enchantedPath(), ArenaHolderConfig.SELECTOR_STARTING.isEnchanted());

    public static final Property<String> SELECTOR_PLAYING_MATERIAL = new StringProperty(ArenaHolderConfig.SELECTOR_PLAYING.materialPath(), ArenaHolderConfig.SELECTOR_PLAYING.getMaterial());
    public static final Property<Integer> SELECTOR_PLAYING_DATA = new IntegerProperty(ArenaHolderConfig.SELECTOR_PLAYING.dataPath(), (int) ArenaHolderConfig.SELECTOR_PLAYING.getData());
    public static final Property<Boolean> SELECTOR_PLAYING_ENCHANT = new BooleanProperty(ArenaHolderConfig.SELECTOR_PLAYING.enchantedPath(), ArenaHolderConfig.SELECTOR_PLAYING.isEnchanted());

    public static final Property<String> SELECTOR_ENDING_MATERIAL = new StringProperty(ArenaHolderConfig.SELECTOR_ENDING.materialPath(), ArenaHolderConfig.SELECTOR_ENDING.getMaterial());
    public static final Property<Integer> SELECTOR_ENDING_DATA = new IntegerProperty(ArenaHolderConfig.SELECTOR_ENDING.dataPath(), (int) ArenaHolderConfig.SELECTOR_ENDING.getData());
    public static final Property<Boolean> SELECTOR_ENDING_ENCHANT = new BooleanProperty(ArenaHolderConfig.SELECTOR_ENDING.enchantedPath(), ArenaHolderConfig.SELECTOR_ENDING.isEnchanted());


    @Override
    public void registerComments(CommentsConfiguration conf) {
        // header
        conf.setComment("", "Plugin by andrei1058.", " ", " ");
    }
}
