package com.andrei1058.stevesus.common.database.table;

import com.andrei1058.stevesus.common.database.type.LanguageColumn;
import com.andrei1058.dbi.column.Column;
import com.andrei1058.dbi.column.type.UUIDColumn;
import com.andrei1058.dbi.table.Table;

import java.util.LinkedList;

public class LanguageTable implements Table {

    public final UUIDColumn PRIMARY_KEY = new UUIDColumn("player", null);
    public final LanguageColumn LANGUAGE = new LanguageColumn("language", 16);

    LinkedList<Column<?>> columns = new LinkedList<>();

    public LanguageTable(){
        columns.add(LANGUAGE);
    }

    @Override
    public String getName() {
        return "player_language";
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
