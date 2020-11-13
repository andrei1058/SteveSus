package com.andrei1058.stevesus.config;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.configurationdata.CommentsConfiguration;
import ch.jalu.configme.properties.*;
import com.andrei1058.stevesus.api.server.ServerType;
import com.andrei1058.stevesus.config.properties.LocationProperty;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.Collections;

public class MainConfig implements SettingsHolder {

    private MainConfig() {
    }

    @Comment({"Server type defines the way you want to run the plugin.",
            "Available types: ",
            "MULTI_ARENA - when you want to run the plugin on a singe server and have a lobby where you can select arenas.",
            "BUNGEE - if you want to run multiple arenas on this server without a lobby. Players will join from a remote server using SteveSus-Connector plugin.",
            "BUNGEE_LEGACY - where the server will run a single arena. Players can join directly and will be added to the game."})
    public static final Property<ServerType> SERVER_TYPE = new EnumProperty<>(ServerType.class, "server-type", ServerType.BUNGEE);

    @Comment({"Server DNS replacement for {server_name} placeholder."})
    public static final Property<String> SERVER_DISPLAY_NAME = new StringProperty("server-display-name", "mc.andrei1058.com");

    @Comment({" ", "This link is opened when players click on certain chat messages. When vip features are required."})
    public static final Property<String> VIP_JOIN_DETAILS = new StringProperty("store-link", "https://andrei1058.com");

    @Comment({" ", "Default/ fallBack server language."})
    public static final Property<String> FALLBACK_LANGUAGE = new StringProperty("default-locale", "en");

    @Comment("Languages folder path.")
    public static final Property<String> LANGUAGES_FOLDER = new StringProperty("custom-path.locales-path", "");
    @Comment("Database folder path. Where's database.yml located?")
    public static final Property<String> DATABASE_PATH = new StringProperty("custom-path.database-path", "");
    @Comment("Templates folder path.")
    public static final Property<String> TEMPLATES_PATH = new StringProperty("custom-path.templates-path", "");
    @Comment("Directory where to get selector.yml from.")
    public static final Property<String> SELECTOR_PATH = new StringProperty("custom-path.selector-path", "");
    @Comment("Sounds configuration path.")
    public static final Property<String> SOUNDS_PATH = new StringProperty("custom-path.sounds-path", "");
    @Comment("Stats GUIs configuration path.")
    public static final Property<String> STATS_PATH = new StringProperty("custom-path.stats-path", "");
    @Comment("Join items configuration path.")
    public static final Property<String> JOIN_ITEMS_PATH = new StringProperty("custom-path.join-items-path", "");
    @Comment("Teleporter configuration path.")
    public static final Property<String> TELEPORTER_PATH = new StringProperty("custom-path.teleporter-path", "");
    @Comment("Abuse prevention configuration path.")
    public static final Property<String> ABUSE_PREVENTION_PATH = new StringProperty("custom-path.abuse-prevention-path", "");
    @Comment("Exclusion vote configuration path.")
    public static final Property<String> EXCLUSION_PATH = new StringProperty("custom-path.exclusion-vote-path", "");


    // COMMON
    @Comment("Set this to true if you want to store player data on a database.")
    public static final Property<Integer> GAME_COUNTDOWN_INITIAL = new IntegerProperty("common-section.game-countdown.start-countdown", 70);
    @Comment("Used when the maximum amount of players is reached. Time shortener.")
    public static final Property<Integer> GAME_COUNTDOWN_SHORTENED = new IntegerProperty("common-section.game-countdown.shortened-countdown", 20);
    @Comment("Should /leave command be registered?")
    public static final Property<Boolean> REGISTER_LEAVE_CMD = new BooleanProperty("common-section.register-leave-cmd", true);
    @Comment({"If not overwritten trough the API, list of servers (separated by comma)", "where to move players when a game ends or when they use a leave cmd."})
    public static final Property<String> DISCONNECT_ADAPTER_INTERNAL = new StringProperty("common-section.internal-disconnect-handler", "hub, lobby");
    @Comment("Allow parties?")
    public static final Property<Boolean> PARTIES_ENABLE = new BooleanProperty("common-section.parties.enable", true);
    @Comment("Should '/x party' sub-command be registered?")
    public static final Property<Boolean> PARTIES_ENABLE_COMMAND = new BooleanProperty("common-section.parties.internal.register-sub-command", true);
    @Comment("How many seconds before removing member from a party if he disconnects? -1/ 0 for always")
    public static final Property<Integer> PARTIES_DISBAND_AFTER = new IntegerProperty("common-section.parties.internal.offline-tolerance", 60);
    @Comment("How many players are allowed in a party?")
    public static final Property<Integer> PARTIES_SIZE_LIMIT = new IntegerProperty("common-section.parties.internal.party-size-limit", 10);
    @Comment("How often should placeholders be refreshed? In server ticks.")
    public static final Property<Integer> SIDEBAR_PLACEHOLDERS_REFRESH_INTERVAL = new IntegerProperty("common-section.sidebar.placeholders-refresh-interval", 20);
    @Comment("How often should scoreboard title be refreshed? In server ticks.")
    public static final Property<Integer> SIDEBAR_TITLE_REFRESH_INTERVAL = new IntegerProperty("common-section.sidebar.title-refresh-interval", 10);


    // MULTI ARENA
    @Comment("Location where to spawn players when they join the server.")
    public static final Property<Location> MULTI_ARENA_SPAWN_LOC = new TypeBasedProperty<>("multi-arena-section.spawn-point", new Location(null, 0, 0, 0), new LocationProperty());

    // BUNGEE AUTO SCALE
    @Comment("Server's name in bungee config.")
    public static final Property<String> BUNGEE_AUTO_SCALE_PROXIED_NAME = new StringProperty("bungee-section.server-name", "notSet");
    @Comment("List of IP:PORT where to send arenas data. Do not insert the minecraft server port here.")
    public static final ListProperty<String> BUNGEE_AUTO_SCALE_LOBBY_SOCKETS = new StringListProperty("bungee-section.lobby-sockets", Collections.singletonList("localhost:11058"));
    @Comment("How often to check for new started up lobbies in seconds.")
    public static final Property<Integer> BUNGEE_AUTO_SCALE_UPDATE_LOBBIES_INTERVAL = new IntegerProperty("bungee-section.update-lobbies-interval", 10);
    @Comment({"How often to tell remote lobbies that this server is still online and arena data is up to date. In seconds"})
    public static final Property<Integer> BUNGEE_AUTO_SCALE_PING_LOBBIES_INTERVAL = new IntegerProperty("bungee-section.ping-lobbies-interval", 5);

    // BUNGEE LEGACY
    @Comment({"How many arena re-starts before re-starting the server itself?", "Hint: -1 to disable or 1 to always restart."})
    public static final Property<Integer> BUNGEE_LEGACY_GAMES_BEFORE_RESTART = new IntegerProperty("bungee-legacy-section.games-before-restart", 1);
    @Comment("When the countdown above reaches 0 it will execute this command.")
    public static final Property<String> BUNGEE_LEGACY_RESTART_COMMAND = new StringProperty("bungee-legacy-section.restart-command", "restart");

    // ALLOWED COMMANDS IN GAME
    @Comment("Commands allowed when game status is waiting or starting.")
    public static final ListProperty<String> ALLOWED_CMD_PRE_GAME = new StringListProperty("whitelisted-commands.pre-game", Arrays.asList("ss", "lang"));
    @Comment("Commands allowed during the game.")
    public static final ListProperty<String> ALLOWED_CMD_IN_GAME = new StringListProperty("whitelisted-commands.in-game", Arrays.asList("ss", "lang", "party"));

    @Override
    public void registerComments(CommentsConfiguration conf) {
        conf.setComment("",
                "Plugin made with love and M0nst3r Energy in Romania by andrei1058",
                "Website: https://andrei1058.com",
                "Discord: https://discord.gg/XdJfN2X", " ", " ");

        conf.setComment("common-section", " ", " ", "This configuration section is used by every server type.");
        conf.setComment("common-section.game-countdown", "Game countdowns in seconds.", "Game countdowns can be configured per arena as well!", "Just add game-countdown.start-countdown for example in the arena file.");

        // multi arena configuration section header
        conf.setComment("multi-arena-section", " ", " ", "This configuration section contains settings used only when your server is running in MULTI_ARENA mode.");

        // bungee legacy configuration section header
        conf.setComment("bungee-legacy-section", " ", " ", "This configuration section contains settings used only when your server is running in BUNGEE_LEGACY mode.");

        // bungee auto scale configuration section header
        conf.setComment("bungee-section", " ", " ", "This configuration section contains settings used only when your server is running in BUNGEE mode.");

        // custom paths
        conf.setComment("custom-path", " ", "Useful if you want to make your servers use the same files.", "This is compatible as well with the Connector plugin. Leave empty for default.");
        // parties
        conf.setComment("common-section.parties", " ", "Parties configuration.");
        conf.setComment("common-section.parties.internal", "Default parties implementation configuration.");

        conf.setComment("whitelisted-commands", " ", "List of commands that are allowed to be used in pre-game and in-game.", "You can allow a root command like 'party' or with sub-commands: 'party leave'.");
    }
}
