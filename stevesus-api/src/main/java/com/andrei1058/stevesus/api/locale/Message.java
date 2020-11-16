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
    EXCLUSION_GUI_NAME("voteGUI-gui-name", "&0Who is the imposter?"),
    // Path placeholders: {s} selector name, {r} replacement item.
    EXCLUSION_REPLACEMENT_ITEM_NAME_PATH(true, "voteGUI-{s}-replacement-{r}-name", "#"),
    EXCLUSION_REPLACEMENT_ITEM_LORE_PATH(true, "voteGUI-{s}-replacement-{r}-lore", Collections.singletonList("")),
    EXCLUSION_GUI_STATUS_DISCONNECTED(false, "voteGUI-player-status-disconnected", "&c&lThis player disconnected"),
    EXCLUSION_GUI_STATUS_REQUESTER(false, "voteGUI-player-status-requester", "&6Meeting requester"),
    EXCLUSION_GUI_STATUS_VOTERS(false, "voteGUI-player-status-voters", "&cVoted by:"),
    EXCLUSION_GUI_STATUS_VOTE_LIST(false, "voteGUI-player-status-vote-FORMAT", "&7 - &o{player}"),
    EXCLUSION_CHAT_ANNOUNCEMENT_ANONYMOUS(false, "vote-announce-anonymous", "&8{player} &7has voted!"),
    EXCLUSION_CHAT_ANNOUNCEMENT_REGULAR(false, "vote-announce-regular", "&8{player} &7voted &c{target}&7."),
    EXCLUSION_CHAT_ANNOUNCEMENT_SKIP(false, "vote-announce-skip", "&8{player} &7has skipped."),

    EXCLUSION_RESULT_CHAT(false, "vote-result-chat", Arrays.asList(" ", " ", "&e&lVoting Results", " ", "{votes}", " ", "{exclusion}", " ")),
    EXCLUSION_RESULT_FORMAT_VOTE(false, "vote-result-chat-format-vote", "&b{player} &8- &e{amount} votes "),
    EXCLUSION_RESULT_FORMAT_EXCLUSION_SKIP(false, "vote-result-chat-format-skipped", "&b&lNo one was ejected!"),
    EXCLUSION_RESULT_FORMAT_EXCLUSION_EJECTED_ANONYMOUS(false, "vote-result-chat-format-ejected-anonymous", "&b{player} &bwas ejected!"),
    EXCLUSION_RESULT_FORMAT_EXCLUSION_EJECTED_INNOCENT(false, "vote-result-chat-format-ejected-anonymous", "&c{player} &cwas ejected! He wasn't an Impostor.."),
    EXCLUSION_RESULT_FORMAT_EXCLUSION_EJECTED_IMPOSTOR(false, "vote-result-chat-format-ejected-impostor", "&a{player} &awas ejected! He wasn an Impostor!!!"),
    EXCLUSION_RESULT_FORMAT_EXCLUSION_EJECTED_SELF(false, "vote-result-chat-format-ejected-self", "&c&lYou've been ejected!"),
    EXCLUSION_RESULT_TITLE_SKIPPED(false, "vote-result-title-skipped", " "),
    EXCLUSION_RESULT_SUBTITLE_SKIPPED(false, "vote-result-subtitle-skipped", "&6No one was ejected!"),
    EXCLUSION_RESULT_TITLE_ANONYMOUS(false, "vote-result-title-anonymous", " "),
    EXCLUSION_RESULT_SUBTITLE_ANONYMOUS(false, "vote-result-subtitle-anonymous", "&b{player} &bwas ejected"),
    EXCLUSION_RESULT_TITLE_INNOCENT(false, "vote-result-title-innocent", "&c{player} &cwas ejected"),
    EXCLUSION_RESULT_SUBTITLE_INNOCENT(false, "vote-result-subtitle-innocent", "&f&lHe was not an Impostor!"),
    EXCLUSION_RESULT_TITLE_IMPOSTOR(false, "vote-result-title-impostor", "&a{player} &awas ejected"),
    EXCLUSION_RESULT_SUBTITLE_IMPOSTOR(false, "vote-result-subtitle-impostor", "&fHe was an Impostor!"),
    EXCLUSION_RESULT_TITLE_SELF(false, "vote-result-title-self", " "),
    EXCLUSION_RESULT_SUBTITLE_SELF(false, "vote-result-subtitle-self", "&c&lYou've been ejected!"),
    MEETING_START_CHAT_MSG_NO_BODY(false, "meeting-start-no-body", Arrays.asList(" ", "&c&lEmergency Meeting", " ", "&e{requester} &erequested an &cemergency &emeeting!", " ")),
    // {reporter}, {dead}, {room}
    MEETING_START_CHAT_MSG_BODY(false, "meeting-start-body", Arrays.asList(" ", " &c&lDead body Found", " ", "&e{reporter} &efound {dead}'s body in &6{room}&e.", " ")),
    GAME_END_IMPOSTORS_WON_CHAT("game-end-impostors-won-chat", Arrays.asList(" ", " ", "&4Impostors won!", "{todo} names here", "")),
    GAME_END_CREW_WON_CHAT("game-end-crew-won-chat", Arrays.asList(" ", " ", "&bCrewmates won!", "{todo} names here", "")),

    GAME_END_IMPOSTORS_WON_TITLE("game-end-impostors-won-title", "&4Impostors won!"),
    GAME_END_IMPOSTORS_WON_SUBTITLE("game-end-impostors-won-subtitle", " "),
    GAME_END_CREW_WON_TITLE("game-end-crew-won-title", "&aCrewmates won!"),
    GAME_END_CREW_WON_SUBTITLE("game-end-crew-won-subtitle", " "),


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
            "&7{date}", "&8{game_tag}", "", "&fMap: &b{name}", "&fPlayers: &b{on}/{max}", "", "Waiting...", "", "", "&b{server_name}")),
    SCOREBOARD_SIDEBAR_STARTING("scoreboard-sidebar-starting", Arrays.asList("&f&lSteve Sus\n&f&lSteve Sus\n&b&lS&f&lteve Sus\n&b&lSt&f&leve Sus\n&b&lSte&f&lve Sus\n" +
                    "&b&lStev&f&le Sus\n&b&lSteve&f&l Sus\n&b&lSteve S&f&lus\n&b&lSteve Su&f&ls\n&b&lSteve Sus\n&b&lSteve Sus\n&b&lSteve Sus\n&f&lSteve Sus\n&b&lSteve Sus\n&f&lSteve Sus\n&b&lSteve Sus",
            "&7{date}", "&8{game_tag}", "", "&fMap: &b{name}", "&fPlayers: &b{on}/{max}", "", "Starting in &b{countdown}s", "", "", "&b{server_name}")),
    SCOREBOARD_SIDEBAR_IN_GAME("scoreboard-sidebar-playing", Arrays.asList("&f&lSteve Sus\n&f&lSteve Sus\n&b&lS&f&lteve Sus\n&b&lSt&f&leve Sus\n&b&lSte&f&lve Sus\n" +
                    "&b&lStev&f&le Sus\n&b&lSteve&f&l Sus\n&b&lSteve S&f&lus\n&b&lSteve Su&f&ls\n&b&lSteve Sus\n&b&lSteve Sus\n&b&lSteve Sus\n&f&lSteve Sus\n&b&lSteve Sus\n&f&lSteve Sus\n&b&lSteve Sus",
            " ", "{task}", "", "{task}", "", "{task}", "", "{task}", "", "{task}", "", "&b{server_name}")),
    SCOREBOARD_SIDEBAR_ENDING("scoreboard-sidebar-ending", Arrays.asList("&f&lSteve Sus\n&f&lSteve Sus\n&b&lS&f&lteve Sus\n&b&lSt&f&leve Sus\n&b&lSte&f&lve Sus\n" +
                    "&b&lStev&f&le Sus\n&b&lSteve&f&l Sus\n&b&lSteve S&f&lus\n&b&lSteve Su&f&ls\n&b&lSteve Sus\n&b&lSteve Sus\n&b&lSteve Sus\n&f&lSteve Sus\n&b&lSteve Sus\n&f&lSteve Sus\n&b&lSteve Sus",
            "&7{date}", "&8{game_tag}", "", "&7Game Ended", "&fMap: &b{name}", "&7Spectators: &f{spectating}", "", "", "", "&b{server_name}")),
    SCOREBOARD_SIDEBAR_SPECTATOR("scoreboard-sidebar-spectating", Arrays.asList("&f&lSteve Sus\n&f&lSteve Sus\n&b&lS&f&lteve Sus\n&b&lSt&f&leve Sus\n&b&lSte&f&lve Sus\n" +
                    "&b&lStev&f&le Sus\n&b&lSteve&f&l Sus\n&b&lSteve S&f&lus\n&b&lSteve Su&f&ls\n&b&lSteve Sus\n&b&lSteve Sus\n&b&lSteve Sus\n&f&lSteve Sus\n&b&lSteve Sus\n&f&lSteve Sus\n&b&lSteve Sus",
            "&7{date}", "&8{game_tag}", "", "&fMap: &b{name}", "", "", "", "&7Spectators: &f{spectating}", "", "&b{server_name}")),

    PREVENTION_GAME_TOO_SHORT("prevention-game-too-short", " \n \n {prefix}&cUnfortunately your play-time on &f{map} &cwas too short and no stats will be applied!\n "),
    TEAM_NAME_PATH_(true, "team-name-", ""),
    GAME_TASK_NAME_PATH_(true, "game-task-name-", ""),
    GAME_TASK_DESCRIPTION_PATH_(true, "game-task-description-", ""),
    GAME_TASK_SCOREBOARD_FORMAT("game-task-scoreboard-format", "&b{task_name}&f ({task_stage}/{task_stages})"),
    GAME_TASK_METER_NAME("game-task-meter-bar", "Total Tasks Completed"),
    EMERGENCY_BUTTON_HOLO1("emergency-button-holo1", "&4&lEmergency Button"),
    EMERGENCY_BUTTON_HOLO2("emergency-button-holo2", "&fClick to start a meeting!"),
    EMERGENCY_BUTTON_STATUS_YOUR_MEETINGS_LEFT("emergency-button-status1", "&eYou have &6&l{amount} &bmeeteings left!"),
    EMERGENCY_BUTTON_STATUS_VOTING_STARTS_IN("emergency-button-status2", "&eVoting starts in &6&l{time}&es!"),
    EMERGENCY_BUTTON_STATUS_VOTING_ENDS_IN("emergency-button-status3", "&cVoting ends in &f&l{time}&cs!"),
    EMERGENCY_DENIED_NO_MEETINGS_LEFT("emergency-button-denied-no-left", "&cYou have &f&l0 &cemergency meetings left!"),
    EMERGENCY_DENIED_COOL_DOWN("emergency-button-denied-cooldown", "&cCrewmates must wait &f&l{time}s &cbefore next emergency."),

    EMERGENCY_MEETING_TITLE("emergency-meeting-title", "&4&lEMERGENCY MEETING"),
    EMERGENCY_MEETING_SUBTITLE("emergency-meeting-subtitle", "Requested by {player}"),
    // {reporter}, {dead}, {room}
    EMERGENCY_MEETING_DEAD_TITLE("emergency-body-found-title", "&4&lDead body reported!"),
    // {reporter}, {dead}, {room}
    EMERGENCY_MEETING_DEAD_SUBTITLE("emergency-body-found-subtitle", "&7{reporter} found {dead}"),
    TALK_ALLOWED_DURING_MEETINGS("chat-denied-no-meeting", "&cYou can talk during meetings only!"),
    COLOR_NAME_PATH_(true, "color-name-", ""),
    DEAD_BODY_HOLO_LINE1("dead-body-holo-line1", "&c&lDead Body"),
    DEAD_BODY_HOLO_LINE2("dead-body-holo-line2", "&cClick to report!");

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
