package com.andrei1058.stevesus.common.api.locale;

import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Arrays;
import java.util.Collections;

public enum CommonMessage {

    ENABLE("enable", true),
    NAME("display-name", "&cUnknown"),
    TIME_ZONE("time-zone", "UTC+1"),
    DATE_FORMAT("date-format", "dd MMMM yyyy HH:mm"),
    DATE_NONE("date-never", "Never"),
    PREFIX("prefix", ""),

    // game state messages
    ARENA_STATUS_ENABLING_NAME("arena-state-enabling", "&9In enable queue"),
    ARENA_STATUS_WAITING_NAME("arena-state-waiting", "&aWaiting"),
    ARENA_STATUS_STARTING_NAME("arena-state-starting", "&eStarting"),
    ARENA_STATUS_IN_GAME_NAME("arena-state-in-game", "&4In Game"),
    ARENA_STATUS_ENDING_NAME("arena-state-ending", "&cRestarting"),

    ARENA_SELECTOR_GUI_NAME("arena-selector-gui-name", "&8Game Selector"),

    //
    CMD_JOIN_NOT_FOUND("join-denied-no-games", "{prefix}&cCouldn't find any game!"),
    ARENA_JOIN_DENIED_SPECTATOR("join-denied-spectate", "{prefix}&cSorry, but spectators are not allowed in this arena."),
    ARENA_JOIN_DENIED_GAME_FULL("join-denied-game-full", "{prefix}&cSorry, but this arena is full! \n&aDonors can join full games. &7&o(read more)"),
    ARENA_JOIN_DENIED_NO_PARTY_LEADER("join-denied-not-party-leader", "{prefix}&cSorry, but only party leaders can choose the arena."),

    // Placeholders to be replaced: {name} {template} {on} {max} {status} {spectating} {allowSpectate}
    SELECTOR_DISPLAY_ITEM_WAITING_NAME("arena-display-item-waiting-name", "&f&l{name}"),
    SELECTOR_DISPLAY_ITEM_WAITING_LORE("arena-display-item-waiting-lore", Arrays.asList(" ", "{status} &f{on}&e/&f{max}", "", "&7Click to join!")),
    SELECTOR_DISPLAY_ITEM_STARTING_NAME("arena-display-item-starting-name", "&f&l{name}"),
    SELECTOR_DISPLAY_ITEM_STARTING_LORE("arena-display-item-starting-lore", Arrays.asList(" ", "{status} &f{on}&e/&f{max}", "", "&7Click to join!")),
    SELECTOR_DISPLAY_ITEM_PLAYING_NAME("arena-display-item-playing-name", "&f&l{name} &7(&e{game_tag}&7)"),
    SELECTOR_DISPLAY_ITEM_PLAYING_LORE("arena-display-item-playing-lore", Arrays.asList(" ", "{status} &f{on}&e/&f{max}", "", "&7Click to spectate!")),
    SELECTOR_DISPLAY_ITEM_ENDING_NAME("arena-display-item-ending-name", "&f&l{name}"),
    SELECTOR_DISPLAY_ITEM_ENDING_LORE("arena-display-item-ending-lore", Arrays.asList(" ", "{status} &f{on}&e/&f{max}", "", "&7This arena is restarting!")),

    // Path placeholders: {s} selector name, {r} replacement item.
    SELECTOR_REPLACEMENT_ITEM_NAME_PATH(true, "selector-{s}-replacement-{r}-name", "#"),
    SELECTOR_REPLACEMENT_ITEM_LORE_PATH(true, "selector-{s}-replacement-{r}-lore", Collections.singletonList("")),
    //

    CMD_PERMISSION_DENIED("cmd-permission-denied", "{prefix}&cPermission denied!"),
    CMD_MAIN_DESCRIPTION("cmd-main-description", "&8- &eMain Among Us Command."),
    CMD_SELECTOR_DESC("cmd-gui-description", "&8- &eOpen game selector GUI."),
    CMD_JOIN_DESC("cmd-join-description", "&f[name] &7Leave empty for random join."),
    CMD_LANG_DESC("cmd-lang-description", "&8- &eChose your preferred language."),
    // Message placeholders: {code} - language iso, {name} - language display name.
    CMD_LANG_SET("cmd-lang-set", "&7Language successfully switched to: &a{name}&7({code})."),
    // Message placeholders: {cmd} - main cmd short name.
    CMD_LANG_USAGE_HEADER("cmd-lang-usage-header", "{prefix}&7Chose your desired language like this &a/{cmd} lang codeHere&7.\n{prefix}&7Available languages:"),
    // Message placeholders: {code} - language iso, {name} - language display name.
    CMD_LANG_USAGE_OPTION("cmd-lang-usage-option", "&a- &7{name}, iso code: &a{code}"),
    CMD_PARTY_USAGE_HEADER("cmd-party-usage-header", "{prefix}&b&lParty commands:"),
    // Msg placeholders: {name} - party cmd name
    CMD_PARTY_DISPLAY_NAME("cmd-party-display-name", "&3/&f{name} "),
    CMD_PARTY_DESC("cmd-party-description", "&8- &ePlay with your friends."),
    CMD_PARTY_INV_DESC("cmd-party-invite-description", "[player] &7Invite a friend to your party."),
    // Msg placeholders: {root} - root cmd name, {cmd} - sub cmd name, {name} - actual party cmd name
    CMD_PARTY_INV_USAGE("cmd-party-invite-usage", "{prefix}&a/{root} {cmd} {name} [player1] [player2] &7Invite a friend to play together."),
    // Msg placeholders: {player} - target player name
    CMD_PARTY_INV_SENT("cmd-party-invite-sent", "{prefix}&7Invite sent to &a{player}&7."),
    // Msg placeholders: {player} - target player name
    CMD_PARTY_INV_FAILED("cmd-party-invite-failed", "{prefix}&7Could not invite &c{player}&7."),
    CMD_PARTY_INV_FAILED2("cmd-party-invite-failed2", "{prefix}&7Cannot invite more players. Party size limit reached!"),
    // Msg placeholders: {player} - requester display name, {name} - requester raw name
    CMD_PARTY_INV_RECEIVED("cmd-party-invite-received", "{prefix}&b{player} &7invited you to a party. &oClick to accept!"),
    // Msg placeholders: {player} - requester raw name
    CMD_PARTY_ACC_DESC("cmd-party-accept-description", "[player] &7Accept a party request."),
    CMD_PARTY_ACC_FAILED("cmd-party-accept-failed", "{prefix}&7There's no active request from &b{player}&7."),
    // Msg placeholders: {player} - requester raw name
    CMD_PARTY_ACC_SUCC("cmd-party-accept-success", "{prefix}&7Joined &b{player}&7's party."),
    // Msg placeholders: {player} - joined display name, {name} - joined raw name
    CMD_PARTY_ACC_BROADCAST("cmd-party-accept-broadcast", "{prefix}&b{player}&7 joined your party!"),
    // Msg placeholders: {root} - root cmd name, {cmd} - sub cmd name, {name} - actual party cmd name
    CMD_PARTY_ACC_USAGE("cmd-party-accept-usage", "{prefix}&7Usage: &b/{root} {cmd} {name} [name]"),

    // Msg placeholders: {player} - player display name, {name} - player raw name
    CMD_PARTY_KICK_BROADCAST("cmd-party-kick-broadcast", "{prefix}&b{player}&7 was kicked from your party!"),
    // Msg placeholders: {root} - root cmd name, {cmd} - sub cmd name, {name} - actual party cmd name
    CMD_PARTY_KICK_USAGE("cmd-party-kick-usage", "{prefix}&7Usage: &b/{root} {cmd} {name} [name]"),
    CMD_PARTY_KICK_DESC("cmd-party-kick-description", "[player] &7Kick a player from your party."),
    // Msg placeholders: {player} - player display name, {name} - player raw name
    CMD_PARTY_KICK_FAILED("cmd-party-kick-failed", "{prefix}&7Tried to kick &b{player}&7 but he's not in your party."),
    CMD_PARTY_DISBAND_BROADCAST("cmd-party-disband-broadcast", "{prefix}&bYour party has been disbanded!"),
    CMD_PARTY_LEAVE_DESC("cmd-party-leave-description", "&7Leave or disband your current party."),
    CMD_PARTY_LEAVE_SUCC("cmd-party-leave-success", "{prefix}&7You're no longer in a party!"),
    CMD_PARTY_MEMBERS_DESC("cmd-party-members-description", "&7Display members in your party."),
    // Msg placeholders: {members} - list of members formatted
    CMD_PARTY_MEMBERS_MSG("cmd-party-members-list", " \n{prefix}&b&lParty members: "),
    // Msg placeholders: {player} - display name, {name} - raw name
    CMD_PARTY_MEMBERS_FORMAT_ONLINE("cmd-party-members-format-online", "&7{player} &a(online)&b,"),
    // Msg placeholders: {player} - display name, {name} - raw name
    CMD_PARTY_MEMBERS_FORMAT_OFFLINE("cmd-party-members-format-online", "&7{name} &c(offline)&b,"),
    // Msg placeholders: {player} - display name, {name} - raw name
    CMD_PARTY_MEMBERS_HOVER("cmd-party-members-hover", "&7Click to kick &b{player}&7."),
    CMD_PARTY_TRANSFER_DESC("cmd-party-transfer-description", "[player] &7Transfer party ownership."),
    // Msg placeholders: {root} - root cmd name, {cmd} - sub cmd name, {name} - actual party cmd name
    CMD_PARTY_TRANSFER_USAGE("cmd-party-transfer-usage", "{prefix}&7Usage: &b/{root} {cmd} {name} [name]"),
    // Msg placeholders: {player} - display name, {name} - raw name
    CMD_PARTY_TRANSFER_FAILED("cmd-party-transfer-failed", "{prefix}&b{player} &7is not a party member!"),
    // Msg placeholders: {player} - display name, {name} - raw name
    CMD_PARTY_TRANSFER_BROADCAST("cmd-party-transfer-broadcast", "{prefix}&b{player} &7is the new party leader!"),

    // Path placeholders: {s} selector name, {r} replacement item.
    STATS_REPLACEMENT_ITEM_NAME_PATH(true, "statsGUI-{s}-replacement-{r}-name", "#"),
    STATS_REPLACEMENT_ITEM_LORE_PATH(true, "statsGUI-{s}-replacement-{r}-lore", Collections.singletonList("")),
    // {player} player display name, {name} raw name
    STATS_GUI_NAME("statsGUI-gui-name", "&8{player} Stats"),
    CMD_STATS_DESC("cmd-stats-description", "&8- &eView your statistics."),

    // {arena}
    ARENA_JOIN_VIA_PARTY("join-via-party", "{prefix}&7Joining &a{arena} &7via party.");

    private final String path;
    private final Object defaultMsg;
    private final boolean manual;

    /**
     * Create a common message used in the main mini-game and connector plugin.
     *
     * @param manual     true if this path requires manual saving to yml.
     *                   If message path has placeholders like this: my-path-{name}-lore.
     * @param path       message path.
     * @param defaultMsg default message for path.
     */
    CommonMessage(boolean manual, String path, Object defaultMsg) {
        this.path = "cm-" + path;
        this.defaultMsg = defaultMsg;
        this.manual = manual;
    }

    /**
     * Create a common message used in the main mini-game and connector plugin.
     *
     * @param path       message path.
     * @param defaultMsg default message for path.
     */
    CommonMessage(String path, Object defaultMsg) {
        this.path = "cm-" + path;
        this.defaultMsg = defaultMsg;
        this.manual = false;
    }

    /**
     * Check if this message needs manual saving.
     *
     * @return false if is saved by {@link #saveDefaults(YamlConfiguration)}.
     */
    public boolean isManual() {
        return manual;
    }

    @Override
    public String toString() {
        return path;
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
        for (CommonMessage message : values()) {
            if (!message.isManual()) {
                yml.addDefault(message.path, message.getDefaultMsg());
            }
        }
    }
}
