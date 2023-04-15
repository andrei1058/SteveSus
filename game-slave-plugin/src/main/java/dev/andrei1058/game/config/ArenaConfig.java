package dev.andrei1058.game.config;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.configurationdata.CommentsConfiguration;
import ch.jalu.configme.properties.*;
import dev.andrei1058.game.api.arena.ArenaTime;
import dev.andrei1058.game.common.api.selector.ArenaHolderConfig;
import dev.andrei1058.game.config.properties.OrphanLocationProperty;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collections;

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

    @Comment({" ", "Which exclusion layout to use in this arena?", "Leave empty for default."})
    public static final Property<String> EXCLUSION_LAYOUT = new StringProperty("exclusion-layout", "");

    @Comment({" ", "Will spawn players in sequence if you set more than one location."})
    public static final ListProperty<Location> WAITING_LOBBY_LOCATIONS = new ListProperty<>("waiting-lobby.spawn-locations", new OrphanLocationProperty(), new ArrayList<>());
    @Comment({" ", "Will spawn players in sequence if you set more than one location."})
    public static final ListProperty<Location> START_LOCATIONS = new ListProperty<>("game-start.spawn-locations", new OrphanLocationProperty(), new ArrayList<>());

    @Comment({" ", "Will spawn players in sequence if you set more than one location."})
    public static final ListProperty<Location> SPECTATE_LOCATIONS = new ListProperty<>("spectator-system.spawn-locations", new OrphanLocationProperty(), new ArrayList<>());

    @Comment({" ", "Will spawn players in sequence when the game starts or when an emergency meeting is requested."})
    public static final ListProperty<Location> MEETING_LOCATIONS = new ListProperty<>("emergency-meeting-locations", new OrphanLocationProperty(), new ArrayList<>());

    @Comment({" ", "Location where to spawn meeting button."})
    public static final OptionalProperty<Location> MEETING_BUTTON_LOC = new OptionalProperty<>(new TypeBasedProperty<>("meeting-button-location", new Location(null, 0, 0, 0), new OrphanLocationProperty()));

    @Comment({
            " ",
            "Imposter vents configuration. List of strings.",
            "Syntax: vent_name;vent_connection1,conn2;x,y,z,yaw,pitch;material,data"
    })
    public static final StringListProperty VENTS = new StringListProperty("imposter-vents", Collections.emptyList());

    @Comment({
            " ",
            "Player tasks configuration. List of strings.",
            "Syntax: localName,provider,taskName,taskData."
    })
    public static final StringListProperty TASKS = new StringListProperty("crew-tasks", Collections.emptyList());

    @Comment({
            " ",
            "Map rooms.",
            "Syntax: name;pos1;pos2."
    })
    public static final StringListProperty ROOMS = new StringListProperty("rooms", Collections.emptyList());

    @Comment({
            " ",
            "List of Sabotages.",
            "Syntax: provider;sabotageName;sabotageConfig."
    })
    public static final StringListProperty SABOTAGES = new StringListProperty("sabotages", Collections.emptyList());

    @Comment("Security Cams. Syntax: identifier;location")
    public static final StringListProperty SECURITY_CAMS = new StringListProperty("security.cams", Collections.emptyList());
    @Comment("Security Monitors. Syntax: location")
    public static final StringListProperty SECURITY_MONITORS = new StringListProperty("security.monitors", Collections.emptyList());

    @Comment("Amount of common tasks by default. -1 for all available.")
    public static final IntegerProperty DEFAULT_GAME_OPTION_TASKS_COMMON = new IntegerProperty("live-settings.common-task.default", 1);
    @Comment("Min amount of common tasks.")
    public static final IntegerProperty DEFAULT_GAME_OPTION_TASKS_COMMON_MIN = new IntegerProperty("live-settings.common-task.min", 1);
    @Comment("Max amount of common tasks. -1 for max available.")
    public static final IntegerProperty DEFAULT_GAME_OPTION_TASKS_COMMON_MAX = new IntegerProperty("live-settings.common-task.max", -1);

    @Comment("Amount of short tasks.")
    public static final IntegerProperty DEFAULT_GAME_OPTION_TASKS_SHORT = new IntegerProperty("live-settings.short-task.default", 1);
    @Comment("Min amount of short tasks. -1 for all available.")
    public static final IntegerProperty DEFAULT_GAME_OPTION_TASKS_SHORT_MIN = new IntegerProperty("live-settings.short-task.min", 1);
    @Comment("Max amount of short tasks. -1 for max available.")
    public static final IntegerProperty DEFAULT_GAME_OPTION_TASKS_SHORT_MAX = new IntegerProperty("live-settings.short-task.max", -1);

    @Comment("Amount of long tasks.")
    public static final IntegerProperty DEFAULT_GAME_OPTION_TASKS_LONG = new IntegerProperty("live-settings.long-task.default", 2);
    @Comment("Min amount of long tasks. -1 for all available.")
    public static final IntegerProperty DEFAULT_GAME_OPTION_TASKS_LONG_MIN = new IntegerProperty("live-settings.long-task.min", 0);
    @Comment("Max amount of long tasks. -1 for all available.")
    public static final IntegerProperty DEFAULT_GAME_OPTION_TASKS_LONG_MAX = new IntegerProperty("live-settings.long-task.max", -1);

    @Comment("How many meetings can a player start by default.")
    public static final IntegerProperty DEFAULT_GAME_OPTION_MEETING_PER_PLAYER = new IntegerProperty("live-settings.meetings-per-player.default", 2);
    @Comment("Min meetings a player can start.")
    public static final IntegerProperty DEFAULT_GAME_OPTION_MEETING_PER_PLAYER_MIN = new IntegerProperty("live-settings.meetings-per-player.min", 0);
    @Comment("Max meetings a player can start.")
    public static final IntegerProperty DEFAULT_GAME_OPTION_MEETING_PER_PLAYER_MAX = new IntegerProperty("live-settings.meetings-per-player.max", 10);


    @Comment("Talking time.")
    public static final IntegerProperty DEFAULT_GAME_OPTION_MEETING_TALK_TIME = new IntegerProperty("live-settings.meeting-talk-timer.default", 45);
    @Comment("Min Talking time.")
    public static final IntegerProperty DEFAULT_GAME_OPTION_MEETING_TALK_TIME_MIN = new IntegerProperty("live-settings.meeting-talk-timer.min", 5);
    @Comment("Max Talking time.")
    public static final IntegerProperty DEFAULT_GAME_OPTION_MEETING_TALK_TIME_MAX = new IntegerProperty("live-settings.meeting-talk-timer.max", 180);

    @Comment("Default voting time.")
    public static final IntegerProperty DEFAULT_GAME_OPTION_MEETING_VOTE_TIME = new IntegerProperty("live-settings.meeting-voting-timer.default", 25);
    @Comment("Min voting time.")
    public static final IntegerProperty DEFAULT_GAME_OPTION_MEETING_VOTE_TIME_MIN = new IntegerProperty("live-settings.meeting-voting-timer.min", 2);
    @Comment("Max voting time.")
    public static final IntegerProperty DEFAULT_GAME_OPTION_MEETING_VOTE_TIME_MAX = new IntegerProperty("live-settings.meeting-voting-timer.max", 180);

    @Comment("Should visual effects be enabled for visual tasks?")
    public static final BooleanProperty DEFAULT_GAME_OPTION_TASKS_VISUAL_ENABLED = new BooleanProperty("live-settings.enable-visual-tasks", true);

    @Comment("Cooldown before using emergency button again after a meeting. In seconds.")
    public static final IntegerProperty DEFAULT_GAME_OPTION_MEETING_COOL_DOWN = new IntegerProperty("live-settings.meeting-button-cooldown.default", 15);
    @Comment("Min cooldown before using emergency button again after a meeting. In seconds.")
    public static final IntegerProperty DEFAULT_GAME_OPTION_MEETING_COOL_DOWN_MIN = new IntegerProperty("live-settings.meeting-button-cooldown.min", 5);
    @Comment("Max cooldown before using emergency button again after a meeting. In seconds.")
    public static final IntegerProperty DEFAULT_GAME_OPTION_MEETING_COOL_DOWN_MAX = new IntegerProperty("live-settings.meeting-button-cooldown.max", 160);

    @Comment("Cooldown before using sabotage again. In seconds.")
    public static final IntegerProperty LIVE_OPTION_SABOTAGE_COOL_DOWN_DEFAULT = new IntegerProperty("live-settings.sabotage-cooldown.default", 45);
    @Comment({"Min cooldown before using sabotage again. In seconds.", "This is also applied when the game starts."})
    public static final IntegerProperty LIVE_OPTION_SABOTAGE_COOL_DOWN_MIN = new IntegerProperty("live-settings.sabotage-cooldown.min", 10);
    @Comment("Max cooldown before using sabotage again. In seconds.")
    public static final IntegerProperty LIVE_OPTION_SABOTAGE_COOL_DOWN_MAX = new IntegerProperty("live-settings.sabotage-cooldown.max", 60);

    @Comment("Cooldown before killing again. In seconds.")
    public static final IntegerProperty LIVE_OPTION_KILL_COOL_DOWN_DEFAULT = new IntegerProperty("live-settings.kill-cooldown.default", 45);
    @Comment("Min cooldown before killing again. In seconds.")
    public static final IntegerProperty LIVE_OPTION_KILL_COOL_DOWN_MIN = new IntegerProperty("live-settings.kill-cooldown.min", 10);
    @Comment("Max cooldown before killing again. In seconds.")
    public static final IntegerProperty LIVE_OPTION_KILL_COOL_DOWN_MAX = new IntegerProperty("live-settings.kill-cooldown.max", 180);


    @Comment("Ignore color limit? If players amount is greater than color limit some players will have the same color.")
    public static final BooleanProperty DEFAULT_GAME_OPTION_IGNORE_COLOR_LIMIT = new BooleanProperty("ignore-color-limit", true);

    @Comment({
            " ",
            "Set gameplay time. Available choices: DAY, NIGHT."
    })
    public static final Property<ArenaTime> MAP_TIME = new EnumProperty<>(ArenaTime.class, "time-of-the-day", ArenaTime.NIGHT);


    @Comment("Initial game countdown for this arena.")
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
        conf.setComment("default-game-option", " ", "Default game options.", "These values can suffer modification from addons.");
    }
}
