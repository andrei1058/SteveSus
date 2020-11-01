package com.andrei1058.stevesus.common.stats;

import com.andrei1058.dbi.column.Column;
import com.andrei1058.dbi.operator.EqualsOperator;
import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.api.locale.CommonLocale;
import com.andrei1058.stevesus.common.api.locale.CommonMessage;
import com.andrei1058.stevesus.common.database.DatabaseManager;
import com.andrei1058.stevesus.common.stats.command.StatsCommand;
import com.andrei1058.stevesus.common.stats.config.StatsConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;

public class StatsManager {

    private static StatsManager INSTANCE;
    private final PlayerStatsTable statsTable = new PlayerStatsTable();
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
            DatabaseManager.getINSTANCE().getDatabase().createTable(INSTANCE.statsTable, false);
            INSTANCE.statsGUIConfig = new StatsConfig(CommonManager.getINSTANCE().getPlugin(), statsFileDirectory, "layout_stats");
            StatsCommand.append(CommonManager.getINSTANCE().getCommonProvider().getMainCommand());
        }
    }

    /**
     * Make sure to call this async.
     */
    public void fetchStats(UUID player) {
        HashMap<Column<?>, ?> result = DatabaseManager.getINSTANCE().getDatabase().selectRow(statsTable, new EqualsOperator<>(PlayerStatsTable.PRIMARY_KEY, player));

        PlayerStatsCache cache = new PlayerStatsCache(player);

        for (Map.Entry<Column<?>, ?> entry : result.entrySet()) {
            if (entry.getKey().equals(PlayerStatsTable.FIRST_PLAY)) {
                if (entry.getValue() == null) {
                    cache.setFirstPlay(null);
                } else {
                    cache.setFirstPlay((Date) entry.getKey().castResult(entry.getValue()));
                }
            } else if (entry.getKey().equals(PlayerStatsTable.LAST_PLAY)) {
                if (entry.getValue() == null) {
                    cache.setLastPlay(null);
                } else {
                    cache.setLastPlay((Date) entry.getKey().castResult(entry.getValue()));
                }
            } else if (entry.getKey().equals(PlayerStatsTable.GAMES_PLAYED)) {
                cache.setGamesPlayed((Integer) entry.getKey().castResult(entry.getValue()));
            } else if (entry.getKey().equals(PlayerStatsTable.GAMES_ABANDONED)) {
                cache.setGamesAbandoned((Integer) entry.getKey().castResult(entry.getValue()));
            } else if (entry.getKey().equals(PlayerStatsTable.GAMES_WON)) {
                cache.setGamesWon((Integer) entry.getKey().castResult(entry.getValue()));
            } else if (entry.getKey().equals(PlayerStatsTable.GAMES_LOST)) {
                cache.setGamesLost((Integer) entry.getKey().castResult(entry.getValue()));
            }
        }
        //todo other stats for new game

        playerStats.put(player, cache);
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
     * Get database stats table.
     */
    public PlayerStatsTable getStatsTable() {
        return statsTable;
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
                .replace("{games_abandoned}", String.valueOf(cache.getGamesAbandoned()));
    }
}
