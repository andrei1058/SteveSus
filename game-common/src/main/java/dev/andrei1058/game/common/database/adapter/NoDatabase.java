package dev.andrei1058.game.common.database.adapter;

import com.andrei1058.dbi.DatabaseAdapter;
import com.andrei1058.dbi.column.Column;
import com.andrei1058.dbi.column.ColumnValue;
import com.andrei1058.dbi.operator.Operator;
import com.andrei1058.dbi.table.Table;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NoDatabase implements DatabaseAdapter {
    @Override
    public <T> T select(Column<T> column, Table table, Operator<?> operator) {
        return column.getDefaultValue();
    }

    @Override
    public <T> List<T> select(Column<T> column, Table table, Operator<?> operator, int i, int i1) {
        return new ArrayList<>();
    }

    @Override
    public HashMap<Column<?>, ?> selectRow(Table table, Operator<?> operator) {
        return new HashMap<>();
    }

    @Override
    public List<List<ColumnValue<?>>> selectRows(List<Column<?>> list, Table table, Operator<?> operator) {
        return new ArrayList<>();
    }

    @Override
    public List<List<ColumnValue<?>>> selectRows(List<Column<?>> list, Table table, Operator<?> operator, int i, int i1) {
        return null;
    }

    @Override
    public void insert(Table table, List<ColumnValue<?>> list, @Nullable InsertFallback insertFallback) {

    }

    @Override
    public void createTable(Table table, boolean b) {

    }

    @Override
    public void set(Table table, Column<?> column, ColumnValue<?> columnValue, Operator<?> operator) {

    }

    @Override
    public void set(Table table, HashMap<Column<?>, ColumnValue<?>> hashMap, Operator<?> operator) {

    }

    @Override
    public <T> T getLastId(Column<T> column) {
        return null;
    }

    @Override
    public void disable() {

    }
}
