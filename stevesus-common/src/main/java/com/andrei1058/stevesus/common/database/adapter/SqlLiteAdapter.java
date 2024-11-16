package com.andrei1058.stevesus.common.database.adapter;

import com.andrei1058.stevesus.common.api.database.DatabaseAdapter;
import com.andrei1058.stevesus.common.api.locale.CommonLocale;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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
    public void disable() {
        if (connection == null) return;
        try {
            connection.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}
