package com.andrei1058.stevesus.common.database.adapter;

import com.andrei1058.stevesus.common.api.database.DatabaseAdapter;
import com.andrei1058.stevesus.common.api.locale.CommonLocale;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SqlLiteAdapter implements DatabaseAdapter {

    private final Connection connection;

    public SqlLiteAdapter(String filePath) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + filePath);
    }

    @Override
    public void createUserLanguageTable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void saveUserLanguage(UUID uuid, CommonLocale isoCode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public @Nullable String getUserLanguage(UUID uuid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasUserStats(UUID uuid) {
        return false;
    }

    @Override
    public void initUserStats(UUID uuid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void saveUserStats(UUID uuid, Map<String, Object> values) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HashMap<String, Object> getUserStats(UUID uuid) {
        return null;
    }

    @Override
    public void initUserStatsTable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void disable() {
        if (connection == null) return;
        try {
            connection.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}
