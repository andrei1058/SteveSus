package com.andrei1058.stevesus.common.stats;

import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.api.locale.CommonLocale;
import com.andrei1058.stevesus.common.api.locale.CommonMessage;
import com.andrei1058.stevesus.common.database.DatabaseManager;
import com.andrei1058.stevesus.common.stats.command.StatsCommand;
import com.andrei1058.stevesus.common.stats.config.StatsConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;

public class StatsManager {

    private static StatsManager INSTANCE;
    private static final HashMap<UUID, PlayerStatsCache> playerStats = new HashMap<>();
    private StatsConfig statsGUIConfig;

    private StatsManager() {
    }

    /**
     * Initialize stats manager.
     *
     * @param statsFileDirectory directory where to get stats_viewer.yml from.
     */
    public static void init(File statsFileDirectory) {
        if (INSTANCE == null) {
            INSTANCE = new StatsManager();
            DatabaseManager.getINSTANCE().getDatabase().initUserStatsTable();
            INSTANCE.statsGUIConfig = new StatsConfig(CommonManager.getINSTANCE().getPlugin(), statsFileDirectory, "layout_stats");
            StatsCommand.append(CommonManager.getINSTANCE().getCommonProvider().getMainCommand());
        }
    }

    /**
     * Make sure to call this async.
     */
    public void fetchStats(UUID player) {
        HashMap<String, Object> result = DatabaseManager.getINSTANCE().getDatabase().getUserStats(player);

        PlayerStatsCache cache = new PlayerStatsCache(player);
        this.mapToCache(result, cache);

        cache.setGamesPlayed(cache.getGamesWon() + cache.getGamesLost());
        //todo other stats for new game

        playerStats.put(player, cache);
    }

    private void mapToCache(@Nullable HashMap<String, Object> result, PlayerStatsCache cache) {
        if (null == result || result.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : result.entrySet()) {
            if (entry.getKey().equals(StatsList.FIRST_PLAY)) {
                if (entry.getValue() == null) {
                    cache.setFirstPlay(null);
                } else {
                    cache.setFirstPlay((Date) entry.getValue());
                    // todo test
//                    cache.setFirstPlay((Date) entry.getKey().castResult(entry.getValue()));
                }
            } else if (entry.getKey().equals(StatsList.LAST_PLAY)) {
                if (entry.getValue() == null) {
                    cache.setLastPlay(null);
                } else {
                    // todo test
                    cache.setLastPlay((Date) entry.getValue());
//                    cache.setLastPlay((Date) entry.getKey().castResult(entry.getValue()));
                }
            } else if (entry.getKey().equals(StatsList.GAMES_ABANDONED)) {
                // todo
                cache.setGamesAbandoned((Integer) entry.getValue());
//                cache.setGamesAbandoned((Integer) entry.getKey().castResult(entry.getValue()));
            } else if (entry.getKey().equals(StatsList.GAMES_WON)) {
                // todo
                cache.setGamesWon((Integer) entry.getValue());
//                cache.setGamesWon((Integer) entry.getKey().castResult(entry.getValue()));
            } else if (entry.getKey().equals(StatsList.GAMES_LOST)) {
                // todo
                cache.setGamesLost((Integer) entry.getValue());
//                cache.setGamesLost((Integer) entry.getKey().castResult(entry.getValue()));
            } else if (entry.getKey().equals(StatsList.KILLS)) {
                // todo
                cache.setKills((Integer) entry.getValue());
//                cache.setKills((int) entry.getKey().castResult(entry.getValue()));
            } else if (entry.getKey().equals(StatsList.SABOTAGES)) {
                // todo
                cache.setSabotages((Integer) entry.getValue());
//                cache.setSabotages((int) entry.getKey().castResult(entry.getValue()));
            } else if (entry.getKey().equals(StatsList.FIXED_SABOTAGES)) {
                // todo
                cache.setFixedSabotages((Integer) entry.getValue());
//                cache.setFixedSabotages((int) entry.getKey().castResult(entry.getValue()));
            } else if (entry.getKey().equals(StatsList.TASKS)) {
                // todo
                cache.setTasks((Integer) entry.getValue());
//                cache.setTasks((int) entry.getKey().castResult(entry.getValue()));
            }
        }

    }

    /**
     * Clear player stats cache.
     */
    public void clear(UUID player) {
        playerStats.remove(player);
    }

    /**
     * Get player stats cache.
     */
    public PlayerStatsCache getPlayerStats(UUID player) {
        return playerStats.get(player);
    }

    /**
     * Stats Manager Instance.
     */
    public static StatsManager getINSTANCE() {
        return INSTANCE;
    }

    /**
     * Get stats GUIs configuration.
     */
    public StatsConfig getStatsGUIConfig() {
        return statsGUIConfig;
    }

    /**
     * Open a stats GUI to a player.
     *
     * @param player  player receiver.
     * @param guiName yml path.
     */
    public static void openToPlayer(Player player, String guiName) {
        if (INSTANCE.getStatsGUIConfig().getYml().get(guiName) == null) return;
        // load async
        Bukkit.getScheduler().runTaskAsynchronously(CommonManager.getINSTANCE().getPlugin(), () -> {
            List<String> pattern = StatsManager.getINSTANCE().getStatsGUIConfig().getList(guiName + "." + StatsConfig.STATS_GENERIC_PATTERN_PATH);
            StatsGUI statsGUI = new StatsGUI(guiName, pattern, player, CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getLocale(player));
            // open sync
            Bukkit.getScheduler().runTask(CommonManager.getINSTANCE().getPlugin(), () -> statsGUI.open(player));
        });
    }

    /**
     * Parse stats placeholders.
     * Recommended to be used async.
     */
    public String replaceStats(@NotNull Player player, @NotNull String rawString) {
        PlayerStatsCache cache = getPlayerStats(player.getUniqueId());
        if (cache == null) return rawString;
        CommonLocale playerLocale = CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getLocale(player);

        SimpleDateFormat dateFormat = new SimpleDateFormat(playerLocale.getRawMsg(CommonMessage.DATE_FORMAT.toString()));
        dateFormat.setTimeZone(TimeZone.getTimeZone(playerLocale.getRawMsg(CommonMessage.TIME_ZONE.toString())));
        String first_play = cache.getFirstPlay() == null ? playerLocale.getMsg(null, CommonMessage.DATE_NONE) : dateFormat.format(cache.getFirstPlay());
        String last_play = cache.getLastPlay() == null ? playerLocale.getMsg(null, CommonMessage.DATE_NONE) : dateFormat.format(cache.getLastPlay());
        return rawString.replace("{first_play}", first_play).replace("{last_play}", last_play).replace("{games_played}", String.valueOf(cache.getGamesPlayed()))
                .replace("{games_lost}", String.valueOf(cache.getGamesLost())).replace("{games_won}", String.valueOf(cache.getGamesWon()))
                .replace("{games_abandoned}", String.valueOf(cache.getGamesAbandoned())).replace("{kills}", String.valueOf(cache.getKills()))
                .replace("{sabotages}", String.valueOf(cache.getSabotages())).replace("{sabotages_fixed}", String.valueOf(cache.getFixedSabotages()))
                .replace("{tasks}", String.valueOf(cache.getTasks()));
    }
}
