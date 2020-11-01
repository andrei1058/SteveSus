package com.andrei1058.stevesus.common.stats;

import com.andrei1058.dbi.column.Column;
import com.andrei1058.dbi.column.ColumnValue;
import com.andrei1058.dbi.column.datavalue.SimpleValue;
import com.andrei1058.dbi.operator.EqualsOperator;
import com.andrei1058.stevesus.common.database.DatabaseManager;
import org.jetbrains.annotations.Nullable;

import java.sql.Date;
import java.util.HashMap;
import java.util.UUID;

public class PlayerStatsCache {

    private final UUID uuid;
    private Date firstPlay;
    private Date lastPlay;
    private int gamesPlayed;
    private int gamesAbandoned;
    private int gamesWon;
    private int gamesLost;

    public PlayerStatsCache(UUID player) {
        this.uuid = player;
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
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public void setGamesLost(int gamesLost) {
        this.gamesLost = gamesLost;
    }

    public int getGamesLost() {
        return gamesLost;
    }

    /**
     * Make sure to call this async.
     * If the player abandoned will increment only abandon column.
     */
    public void saveStats(boolean abandoned) {

        HashMap<Column<?>, ColumnValue<?>> toSave = new HashMap<>();

        if (abandoned) {
            setGamesAbandoned(getGamesAbandoned() + 1);
        } else {
            toSave.put(PlayerStatsTable.FIRST_PLAY, new SimpleValue<>(PlayerStatsTable.FIRST_PLAY, getFirstPlay() == null ? getLastPlay() : getFirstPlay()));
            toSave.put(PlayerStatsTable.LAST_PLAY, new SimpleValue<>(PlayerStatsTable.LAST_PLAY, getLastPlay()));

            toSave.put(PlayerStatsTable.GAMES_PLAYED, new SimpleValue<>(PlayerStatsTable.GAMES_PLAYED, getGamesPlayed()));
            toSave.put(PlayerStatsTable.GAMES_WON, new SimpleValue<>(PlayerStatsTable.GAMES_WON, getGamesWon()));
            toSave.put(PlayerStatsTable.GAMES_LOST, new SimpleValue<>(PlayerStatsTable.GAMES_WON, getGamesLost()));
        }
        toSave.put(PlayerStatsTable.GAMES_ABANDONED, new SimpleValue<>(PlayerStatsTable.GAMES_ABANDONED, getGamesAbandoned()));

        //todo other stats for new game

        DatabaseManager.getINSTANCE().getDatabase().set(StatsManager.getINSTANCE().getStatsTable(),
                toSave, new EqualsOperator<>(PlayerStatsTable.PRIMARY_KEY, getUuid()));
    }
}
