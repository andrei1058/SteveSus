package com.andrei1058.stevesus.api.locale;

import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Arrays;
import java.util.Collections;

public enum Message {

    // bungee mode related
    ARENA_JOIN_DENIED_NO_PROXY("join-denied-no-proxy", "{prefix}&cSorry but you must join an arena using BedWarsProxy." +
            "\n&eIf you want to setup an arena make sure to give yourself the bw.setup permission so you can join the server directly!"),

    ARENA_JOIN_ANNOUNCE("join-announce", "{prefix}&a{player} &7has joined (&a{on}&7/&a{max}&7)!"),
    // {arena}
    ARENA_JOIN_SPECTATOR("join-spectator", "{prefix}&7You are now spectating &a{arena}&7.\n{prefix}&7You can leave the arena at any time doing &a/leave&7."),

    // vip join feature related
    VIP_JOIN_KICKED("vip-join-feature-kicked", "{prefix}&cSorry, but you were kicked out because a donor joined the arena.\n&cPlease consider donating for more features. &7&o(click)"),
    VIP_JOIN_DENIED("vip-join-feature-denied", "{prefix}&cWe apologise but this arena is full.\n&cWe know you're a donor but actually this arena is full of staff or/ and donors."),

    COUNTDOWN_START_CANCELLED("countdown-cancelled-chat", "{prefix}&cThere aren't enough players! Countdown stopped!"),
    COUNTDOWN_START_CANCELLED_TITLE("countdown-cancelled-title", "&cWaiting for players.."),

    CMD_LEAVE_DESC("cmd-leave-description", "&7Leave this game/ server."),
    CMD_FORCE_START_DESC("cmd-start-description", "&7Force start a game."),
    CMD_FORCE_START_FAILED("cmd-start-failed", "&7Cannot force start this game. Minimum requirements are not reached."),
    CMD_TELEPORTER_DESC("cmd-teleporter-description", "&7Spectate a player."),

    LEAVE_ANNOUNCE("leave-announce", "{prefix}&7{player} &ehas left!"),

    CHAT_FORMAT_LOBBY_WORLD("chat-format-lobby-world", "{vault_prefix}{player}{vault_suffix}&8>&r {message}"),
    CHAT_FORMAT_WAITING("chat-format-waiting", "{vault_prefix}{player}{vault_suffix}&8>&r {message}"),
    CHAT_FORMAT_STARTING("chat-format-starting", "{vault_prefix}{player}{vault_suffix}&8>&r {message}"),
    CHAT_FORMAT_IN_GAME("chat-format-in-game", "{vault_prefix}{player}{vault_suffix}&8>&r {message}"),
    CHAT_FORMAT_ENDING("chat-format-ending", "{vault_prefix}{player}{vault_suffix}&8>&r {message}"),

    TITLE_COUNTDOWN_STOPPED("count-down-interrupted-title", " "),
    SUBTITLE_COUNTDOWN_STOPPED("count-down-interrupted-subtitle", "&cWaiting for more players..."),
    //ACTION_MESSAGE_STATE_WAITING("action-msg-game-state-waiting", "&cWaiting for more players..."),
    // placeholders: {countdown}
    ACTION_MESSAGE_STATE_STARTING("action-msg-game-state-starting", "&aStarting in &b&l{countdown} &aseconds!"),
    ACTION_MESSAGE_STATE_ENDING("action-msg-game-state-ending", "&bThanks for playing on &f&l{server_name}&b!"),

    // {c} category name, {i} item name
    JOIN_ITEM_NAME_PATH(true, "join-item-{c}-{i}-name", " "),
    // {c} category name, {i} item name
    JOIN_ITEM_LORE_PATH(true, "join-item-{c}-{i}-lore", " "),
    COUNT_DOWN_TITLE_PATH(true, "count-down-title-", " "),
    COUNT_DOWN_SUBTITLE_PATH(true, "count-down-subtitle-", " "),

    // Path placeholders: {s} selector name, {r} replacement item.
    TELEPORTER_REPLACEMENT_ITEM_NAME_PATH(true, "teleporterGUI-{s}-replacement-{r}-name", "#"),
    TELEPORTER_REPLACEMENT_ITEM_LORE_PATH(true, "teleporterGUI-{s}-replacement-{r}-lore", Collections.singletonList("")),
    // {player} player display name, {name} raw name, {spectator} the current spectator display name, {spectator_raw} the current spectator raw name.
    TELEPORTER_GUI_NAME("statsGUI-gui-name", "&8Teleport to a player"),
    // Placeholders: {target}, {target_raw}
    TITLE_SPECTATE_FIRST_PERSON_START("title-spectate-first-person-start", "&aSpectating &7{target}"),
    // Placeholders: {target}, {target_raw}
    SUBTITLE_SPECTATE_FIRST_PERSON_START("subtitle-spectate-first-person-start", "&cSNEAK to exit"),
    // Placeholders: {target}, {target_raw}
    TITLE_SPECTATE_FIRST_PERSON_STOP("title-spectate-first-person-stop", "&eExiting Spectator mode"),
    // Placeholders: {target}, {target_raw}
    SUBTITLE_SPECTATE_FIRST_PERSON_STOP("subtitle-spectate-first-person-stop", " "),

    SCOREBOARD_SIDEBAR_LOBBY("scoreboard-sidebar-lobby", Arrays.asList("&f&lSteve Sus\n&f&lSteve Sus\n&b&lS&f&lteve Sus\n&b&lSt&f&leve Sus\n&b&lSte&f&lve Sus\n" +
                    "&b&lStev&f&le Sus\n&b&lSteve&f&l Sus\n&b&lSteve S&f&lus\n&b&lSteve Su&f&ls\n&b&lSteve Sus\n&b&lSteve Sus\n&b&lSteve Sus\n&f&lSteve Sus\n&b&lSteve Sus\n&f&lSteve Sus\n&b&lSteve Sus",
            "", "&7{date}", "", "&7{player}", "", "&8Game Stats:", "&7Wins: &f{games_won}", "&7Abandons: &f{games_abandoned}", "&7Losts: &f{games_lost}", "", "&b{server_name}")),

    SCOREBOARD_SIDEBAR_WAITING("scoreboard-sidebar-waiting", Arrays.asList("&f&lSteve Sus\n&f&lSteve Sus\n&b&lS&f&lteve Sus\n&b&lSt&f&leve Sus\n&b&lSte&f&lve Sus\n" +
                    "&b&lStev&f&le Sus\n&b&lSteve&f&l Sus\n&b&lSteve S&f&lus\n&b&lSteve Su&f&ls\n&b&lSteve Sus\n&b&lSteve Sus\n&b&lSteve Sus\n&f&lSteve Sus\n&b&lSteve Sus\n&f&lSteve Sus\n&b&lSteve Sus",
            "&7{date}", "&8{tag}", "", "&fMap: &b{name}", "&fPlayers: &b{on}/{max}", "", "Waiting...", "", "", "&b{server_name}")),
    SCOREBOARD_SIDEBAR_STARTING("scoreboard-sidebar-starting", Arrays.asList("&f&lSteve Sus\n&f&lSteve Sus\n&b&lS&f&lteve Sus\n&b&lSt&f&leve Sus\n&b&lSte&f&lve Sus\n" +
                    "&b&lStev&f&le Sus\n&b&lSteve&f&l Sus\n&b&lSteve S&f&lus\n&b&lSteve Su&f&ls\n&b&lSteve Sus\n&b&lSteve Sus\n&b&lSteve Sus\n&f&lSteve Sus\n&b&lSteve Sus\n&f&lSteve Sus\n&b&lSteve Sus",
            "&7{date}", "&8{tag}", "", "&fMap: &b{name}", "&fPlayers: &b{on}/{max}", "", "Starting in &b{countdown}s", "", "", "&b{server_name}")),
    SCOREBOARD_SIDEBAR_IN_GAME("scoreboard-sidebar-playing", Arrays.asList("&f&lSteve Sus\n&f&lSteve Sus\n&b&lS&f&lteve Sus\n&b&lSt&f&leve Sus\n&b&lSte&f&lve Sus\n" +
                    "&b&lStev&f&le Sus\n&b&lSteve&f&l Sus\n&b&lSteve S&f&lus\n&b&lSteve Su&f&ls\n&b&lSteve Sus\n&b&lSteve Sus\n&b&lSteve Sus\n&f&lSteve Sus\n&b&lSteve Sus\n&f&lSteve Sus\n&b&lSteve Sus",
            "", "", "", "", "", "", "", "", "", "", "&b{server_name}")),
    SCOREBOARD_SIDEBAR_ENDING("scoreboard-sidebar-ending", Arrays.asList("&f&lSteve Sus\n&f&lSteve Sus\n&b&lS&f&lteve Sus\n&b&lSt&f&leve Sus\n&b&lSte&f&lve Sus\n" +
                    "&b&lStev&f&le Sus\n&b&lSteve&f&l Sus\n&b&lSteve S&f&lus\n&b&lSteve Su&f&ls\n&b&lSteve Sus\n&b&lSteve Sus\n&b&lSteve Sus\n&f&lSteve Sus\n&b&lSteve Sus\n&f&lSteve Sus\n&b&lSteve Sus",
            "&7{date}", "&8{tag}", "", "&7Game Ended", "&fMap: &b{name}", "&7Spectators: &f{spectating}", "", "", "", "&b{server_name}")),
    SCOREBOARD_SIDEBAR_SPECTATOR("scoreboard-sidebar-spectating", Arrays.asList("&f&lSteve Sus\n&f&lSteve Sus\n&b&lS&f&lteve Sus\n&b&lSt&f&leve Sus\n&b&lSte&f&lve Sus\n" +
                    "&b&lStev&f&le Sus\n&b&lSteve&f&l Sus\n&b&lSteve S&f&lus\n&b&lSteve Su&f&ls\n&b&lSteve Sus\n&b&lSteve Sus\n&b&lSteve Sus\n&f&lSteve Sus\n&b&lSteve Sus\n&f&lSteve Sus\n&b&lSteve Sus",
            "&7{date}", "&8{tag}", "", "&fMap: &b{name}", "", "", "", "&7Spectators: &f{spectating}", "", "&b{server_name}")),

    PREVENTION_GAME_TOO_SHORT("prevention-game-too-short", " \n \n {prefix}&cUnfortunately your play-time on &f{map} &cwas too short and no stats will be applied!\n ")
    ;

    private final String path;
    private final boolean manual;
    private final Object defaultMsg;

    /**
     * Create a local message used in the mini-game plugin.
     *
     * @param manual     true if this path requires manual saving to yml.
     *                   If message path has placeholders like this: my-path-{name}-lore.
     * @param path       message path.
     * @param defaultMsg default message for path.
     */
    Message(boolean manual, String path, Object defaultMsg) {
        this.path = "gm-" + path;
        this.manual = manual;
        this.defaultMsg = defaultMsg;
    }

    /**
     * Create a local message used in the mini-game plugin.
     *
     * @param path       message path.
     * @param defaultMsg default message for path.
     */
    Message(String path, Object defaultMsg) {
        this.path = "gm-" + path;
        this.manual = false;
        this.defaultMsg = defaultMsg;
    }

    /**
     * Check if this message needs manual saving.
     *
     * @return false if is saved by {@link #saveDefaults(YamlConfiguration)}.
     */
    public boolean isManual() {
        return manual;
    }

    /**
     * Get default message value.
     */
    public Object getDefaultMsg() {
        return defaultMsg;
    }

    /**
     * Save this message to a yml file.
     *
     * @param yml              language file where to save.
     * @param pathReplacements placeholders to be replaced in message path.
     * @param value            message value.
     */
    public void addDefault(YamlConfiguration yml, String[] pathReplacements, Object value) {
        String path = this.toString();
        for (int i = 0; i < pathReplacements.length; i += 2) {
            path = path.replace(pathReplacements[i], pathReplacements[i + 1]);
        }
        yml.addDefault(path, value);
    }

    /**
     * Save messages that are not {@link #isManual()} to the given yml.
     *
     * @param yml language file where to save.
     */
    public static void saveDefaults(YamlConfiguration yml) {
        for (Message message : values()) {
            if (!message.isManual()) {
                yml.addDefault(message.path, message.getDefaultMsg());
            }
        }
    }

    @Override
    public String toString() {
        return path;
    }

}
