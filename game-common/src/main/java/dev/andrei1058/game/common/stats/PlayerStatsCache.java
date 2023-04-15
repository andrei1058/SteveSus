package dev.andrei1058.game.common.stats;

import com.andrei1058.dbi.DatabaseAdapter;
import com.andrei1058.dbi.column.Column;
import com.andrei1058.dbi.column.ColumnValue;
import com.andrei1058.dbi.column.datavalue.SimpleValue;
import com.andrei1058.dbi.operator.EqualsOperator;
import dev.andrei1058.game.common.CommonManager;
import dev.andrei1058.game.common.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlayerStatsCache {

    private final UUID uuid;
    private Date firstPlay;
    private Date lastPlay;
    private int gamesPlayed;
    private int gamesAbandoned;
    private int gamesWon;
    private int gamesLost;
    private int kills;
    private int sabotages;
    private int fixedSabotages;
    private int tasks;

    public PlayerStatsCache(UUID player) {
        this.uuid = player;

        Bukkit.getScheduler().runTaskAsynchronously(CommonManager.getINSTANCE().getPlugin(), () -> {
            UUID result = DatabaseManager.getINSTANCE().getDatabase().select(PlayerStatsTable.PRIMARY_KEY, StatsManager.getINSTANCE().getStatsTable(), new EqualsOperator<>(PlayerStatsTable.PRIMARY_KEY, player));
            if (result == null) {
                List<ColumnValue<?>> values = new ArrayList<>();
                values.add(new SimpleValue<>(PlayerStatsTable.PRIMARY_KEY, player));
                DatabaseManager.getINSTANCE().getDatabase().insert(StatsManager.getINSTANCE().getStatsTable(), values, DatabaseAdapter.InsertFallback.IGNORE);
            }
        });
    }

    public UUID getUuid() {
        return uuid;
    }

    @Nullable
    public Date getFirstPlay() {
        return firstPlay;
    }

    public void setFirstPlay(@Nullable Date firstPlay) {
        this.firstPlay = firstPlay;
    }

    @Nullable
    public Date getLastPlay() {
        return lastPlay;
    }

    public void setLastPlay(@Nullable Date lastPlay) {
        this.lastPlay = lastPlay;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getGamesAbandoned() {
        return gamesAbandoned;
    }

    public void setGamesAbandoned(int gamesAbandoned) {
        this.gamesAbandoned = gamesAbandoned;
    }

    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
        this.gamesPlayed = gamesWon + gamesLost;
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public void setGamesLost(int gamesLost) {
        this.gamesLost = gamesLost;
        this.gamesPlayed = gamesWon + gamesLost;
    }

    public int getKills() {
        return kills;
    }

    public int getSabotages() {
        return sabotages;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public void setSabotages(int sabotages) {
        this.sabotages = sabotages;
    }

    public void setTasks(int tasks) {
        this.tasks = tasks;
    }

    public int getTasks() {
        return tasks;
    }

    public int getFixedSabotages() {
        return fixedSabotages;
    }

    public void setFixedSabotages(int fixedSabotages) {
        this.fixedSabotages = fixedSabotages;
    }

    public int getGamesLost() {
        return gamesLost;
    }

    /**
     * If the player abandoned will increment only abandon column.
     */
    public void saveStats(boolean abandoned) {

        HashMap<Column<?>, ColumnValue<?>> toSave = new HashMap<>();

        if (abandoned) {
            setGamesAbandoned(getGamesAbandoned() + 1);
        } else {
            toSave.put(PlayerStatsTable.FIRST_PLAY, new SimpleValue<>(PlayerStatsTable.FIRST_PLAY, getFirstPlay() == null ? getLastPlay() : getFirstPlay()));
            toSave.put(PlayerStatsTable.LAST_PLAY, new SimpleValue<>(PlayerStatsTable.LAST_PLAY, getLastPlay()));

            toSave.put(PlayerStatsTable.GAMES_WON, new SimpleValue<>(PlayerStatsTable.GAMES_WON, getGamesWon()));
            toSave.put(PlayerStatsTable.GAMES_LOST, new SimpleValue<>(PlayerStatsTable.GAMES_WON, getGamesLost()));

            toSave.put(PlayerStatsTable.KILLS, new SimpleValue<>(PlayerStatsTable.KILLS, getKills()));
            toSave.put(PlayerStatsTable.SABOTAGES, new SimpleValue<>(PlayerStatsTable.SABOTAGES, getSabotages()));
            toSave.put(PlayerStatsTable.FIXED_SABOTAGES, new SimpleValue<>(PlayerStatsTable.FIXED_SABOTAGES, getFixedSabotages()));
            toSave.put(PlayerStatsTable.TASKS, new SimpleValue<>(PlayerStatsTable.TASKS, getTasks()));
        }
        toSave.put(PlayerStatsTable.GAMES_ABANDONED, new SimpleValue<>(PlayerStatsTable.GAMES_ABANDONED, getGamesAbandoned()));

        //todo other stats for new game

        Bukkit.getScheduler().runTaskAsynchronously(CommonManager.getINSTANCE().getPlugin(), () -> DatabaseManager.getINSTANCE().getDatabase().set(StatsManager.getINSTANCE().getStatsTable(),
                toSave, new EqualsOperator<>(PlayerStatsTable.PRIMARY_KEY, getUuid())));
    }
}
