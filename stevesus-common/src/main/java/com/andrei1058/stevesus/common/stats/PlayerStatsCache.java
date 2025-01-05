package com.andrei1058.stevesus.common.stats;

import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.database.DatabaseManager;
import org.bukkit.Bukkit;
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
    private int kills;
    private int sabotages;
    private int fixedSabotages;
    private int tasks;

    public PlayerStatsCache(UUID player) {
        this.uuid = player;

        Bukkit.getScheduler().runTaskAsynchronously(CommonManager.getINSTANCE().getPlugin(), () -> {
            DatabaseManager.getINSTANCE().getDatabase().initUserStats(uuid);
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

        HashMap<String, Object> toSave = new HashMap<>();

        if (abandoned) {
            setGamesAbandoned(getGamesAbandoned() + 1);
        } else {
            toSave.put(StatsList.FIRST_PLAY.toString(), getFirstPlay() == null ? getLastPlay() : getFirstPlay());
            toSave.put(StatsList.LAST_PLAY.toString(), getLastPlay());

            toSave.put(StatsList.GAMES_WON.toString(), getGamesWon());
            toSave.put(StatsList.GAMES_LOST.toString(), getGamesLost());

            toSave.put(StatsList.KILLS.toString(), getKills());
            toSave.put(StatsList.SABOTAGES.toString(), getSabotages());
            toSave.put(StatsList.FIXED_SABOTAGES.toString(), getFixedSabotages());
            toSave.put(StatsList.TASKS.toString(), getTasks());
        }
        toSave.put(StatsList.GAMES_ABANDONED.toString(), getGamesAbandoned());

        //todo other stats for new game

        Bukkit.getScheduler().runTaskAsynchronously(
                CommonManager.getINSTANCE().getPlugin(),
                () -> DatabaseManager.getINSTANCE().getDatabase().saveUserStats(uuid, toSave)
        );
    }
}
