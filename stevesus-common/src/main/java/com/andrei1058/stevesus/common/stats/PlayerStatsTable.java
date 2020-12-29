package com.andrei1058.stevesus.common.stats;

import com.andrei1058.dbi.column.Column;
import com.andrei1058.dbi.column.type.DateColumn;
import com.andrei1058.dbi.column.type.IntegerColumn;
import com.andrei1058.dbi.column.type.UUIDColumn;
import com.andrei1058.dbi.table.Table;

import java.util.LinkedList;

public class PlayerStatsTable implements Table {

    public static final UUIDColumn PRIMARY_KEY = new UUIDColumn("player", null);
    public static final DateColumn FIRST_PLAY = new DateColumn("first_play", null);
    public static final DateColumn LAST_PLAY = new DateColumn("last_play", null);
    public static final IntegerColumn GAMES_ABANDONED = new IntegerColumn("games_abandoned", 0, 7);
    public static final IntegerColumn GAMES_WON = new IntegerColumn("games_won", 0, 7);
    public static final IntegerColumn GAMES_LOST = new IntegerColumn("games_lost", 0, 7);

    public static final IntegerColumn KILLS = new IntegerColumn("kills", 0, 7);
    public static final IntegerColumn SABOTAGES = new IntegerColumn("sabotages", 0, 7);
    public static final IntegerColumn FIXED_SABOTAGES = new IntegerColumn("sabotages_fixed", 0, 7);
    public static final IntegerColumn TASKS = new IntegerColumn("tasks", 0, 7);

    private final LinkedList<Column<?>> columns = new LinkedList<>();

    public PlayerStatsTable() {
        columns.add(FIRST_PLAY);
        columns.add(LAST_PLAY);
        columns.add(GAMES_ABANDONED);
        columns.add(GAMES_WON);
        columns.add(GAMES_LOST);
        columns.add(KILLS);
        columns.add(SABOTAGES);
        columns.add(FIXED_SABOTAGES);
        columns.add(TASKS);
    }

    @Override
    public String getName() {
        return "player_stats";
    }

    @Override
    public Column<?> getPrimaryKey() {
        return PRIMARY_KEY;
    }

    @Override
    public LinkedList<Column<?>> getColumns() {
        return columns;
    }

    @Override
    public boolean isAutoIncrementPK() {
        return false;
    }
}
