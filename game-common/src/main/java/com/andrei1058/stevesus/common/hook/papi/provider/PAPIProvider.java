package com.andrei1058.stevesus.common.hook.papi.provider;

import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.api.CommonProvider;
import com.andrei1058.stevesus.common.api.arena.DisplayableArena;
import com.andrei1058.stevesus.common.api.locale.CommonLocale;
import com.andrei1058.stevesus.common.stats.PlayerStatsCache;
import com.andrei1058.stevesus.common.stats.StatsManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Placeholders provider for PlaceholderAPI.
 */
public class PAPIProvider extends PlaceholderExpansion {

    private final String identifier;
    private final AdditionalParser additionalParser;

    /**
     * Create a PAPI expansion for this plugin to provide awesome placeholders.
     *
     * @param identifier       first part of the placeholder. Like 'bw1058'.
     * @param additionalParser additional placeholders from other module.
     */
    public PAPIProvider(String identifier, @Nullable AdditionalParser additionalParser) {
        this.identifier = identifier;
        this.additionalParser = additionalParser;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @NotNull String getIdentifier() {
        return identifier;
    }

    @Override
    public @NotNull String getAuthor() {
        return CommonManager.getINSTANCE().getPlugin().getDescription().getAuthors().get(0);
    }

    @Override
    public @NotNull String getVersion() {
        return CommonManager.getINSTANCE().getPlugin().getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        UUID playerUUID = player == null ? null : player.getUniqueId();
        String result = parseCommonPlaceholders(playerUUID, identifier);

        // check module placeholders
        if (result == null && additionalParser != null) {
            return additionalParser.parseModulePlaceholders(player, identifier);
        }
        return result;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String identifier) {

        UUID playerUUID = player == null ? null : player.getUniqueId();
        String result = parseCommonPlaceholders(playerUUID, identifier);

        // check module placeholders
        if (result == null && additionalParser != null) {
            return additionalParser.parseModulePlaceholders(player, identifier);
        }
        return result;
    }

    /**
     * Parse common placeholders that do not require a player instance.
     */
    private String parseCommonPlaceholders(@Nullable UUID playerUUID, String identifier) {
        CommonProvider commonProvider = CommonManager.getINSTANCE().getCommonProvider();

        // global data
        if (identifier.startsWith("global_")) {
            String subRequest = identifier.substring(7);

            String result = null;
            switch (subRequest) {
                case "player_count":
                    result = String.valueOf(commonProvider.getPlayerCount());
                    break;
                case "spectator_count":
                    result = String.valueOf(commonProvider.getSpectatorCount());
                    break;
                case "user_count":
                    result = String.valueOf(commonProvider.getOnlineCount());
                    break;
                case "arena_count":
                    result = String.valueOf(commonProvider.getArenas().size());
                    break;
            }

            return result;
        } else if (identifier.startsWith("game_")) {
            String subRequest = identifier.substring(5);

            if (subRequest.startsWith("count_template_")) {
                if (subRequest.length() < 16) {
                    return "provide template";
                }
                String template = subRequest.substring(15);
                return String.valueOf((int) commonProvider.getArenas().stream().filter(arena -> arena.getTemplateWorld().equals(template)).count());
            } else if (subRequest.startsWith("player_count_")) {
                if (subRequest.length() < 14) {
                    return "provide tag";
                }
                DisplayableArena game = commonProvider.getFromTag(subRequest.substring(13));
                return String.valueOf(game == null ? 0 : game.getCurrentPlayers());
            } else if (subRequest.startsWith("spectator_count_")) {
                if (subRequest.length() < 17) {
                    return "provide tag";
                }
                DisplayableArena game = commonProvider.getFromTag(subRequest.substring(16));
                return String.valueOf(game == null ? 0 : game.getCurrentSpectators());
            } else if (subRequest.startsWith("user_count_")) {
                if (subRequest.length() < 12) {
                    return "provide tag";
                }
                DisplayableArena game = commonProvider.getFromTag(subRequest.substring(11));
                return String.valueOf(game == null ? 0 : game.getCurrentSpectators() + game.getCurrentPlayers());
            } else if (subRequest.startsWith("state_")) {
                if (subRequest.length() < 7) {
                    return "provide tag";
                }
                DisplayableArena game = commonProvider.getFromTag(subRequest.substring(6));
                CommonLocale locale = playerUUID == null ? commonProvider.getCommonLocaleManager().getDefaultLocale() : commonProvider.getCommonLocaleManager().getLocale(playerUUID);
                return game == null ? "Not found" : locale.getMsg(null, game.getGameState().getTranslatePath());
            }
        } else if (identifier.startsWith("stats_")) {
            String subRequest = identifier.substring(6);

            if (playerUUID != null) {
                PlayerStatsCache statsCache = StatsManager.getINSTANCE().getPlayerStats(playerUUID);
                if (statsCache == null){
                    return null;
                }
                switch (subRequest){
                    case "first_play":
                        return commonProvider.getCommonLocaleManager().getLocale(playerUUID).formatDate(statsCache.getFirstPlay());
                    case "last_play":
                        return commonProvider.getCommonLocaleManager().getLocale(playerUUID).formatDate(statsCache.getLastPlay());
                    case "games_played":
                        return String.valueOf(statsCache.getGamesPlayed());
                    case "games_lost":
                        return String.valueOf(statsCache.getGamesLost());
                    case "games_abandoned":
                        return String.valueOf(statsCache.getGamesAbandoned());
                    case "games_won":
                        return String.valueOf(statsCache.getGamesWon());
                    case "tasks":
                        return String.valueOf(statsCache.getTasks());
                    case "sabotages":
                        return String.valueOf(statsCache.getSabotages());
                    case "sabotages_fixed":
                        return String.valueOf(statsCache.getFirstPlay());
                    case "kills":
                        return String.valueOf(statsCache.getKills());
                }
            }
        }
        return null;
    }
}
