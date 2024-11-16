package com.andrei1058.stevesus.common.database.adapter;

import com.andrei1058.stevesus.common.api.database.DatabaseAdapter;
import com.andrei1058.stevesus.common.api.locale.CommonLocale;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class HikariAdapter implements DatabaseAdapter {

    private final HikariDataSource dataSource;
    private final Connection connection;


    public HikariAdapter(
            String poolName,
            int poolSize,
            int maxLifetime,
            String host,
            int port,
            String databaseName,
            String user,
            String password,
            boolean verifyCertificate,
            boolean useSSL
    ) throws SQLException {
// connect
        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setPoolName(poolName);

        hikariConfig.setMaximumPoolSize(poolSize);
        hikariConfig.setMaxLifetime(maxLifetime * 1000L);

        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + databaseName);

        hikariConfig.setUsername(user);
        hikariConfig.setPassword(password);

        hikariConfig.addDataSourceProperty("useSSL", useSSL);
        if (!verifyCertificate) {
            hikariConfig.addDataSourceProperty("verifyServerCertificate", String.valueOf(false));
        }

        hikariConfig.addDataSourceProperty("characterEncoding", "utf8");
        hikariConfig.addDataSourceProperty("encoding", "UTF-8");
        hikariConfig.addDataSourceProperty("useUnicode", "true");

        hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
        hikariConfig.addDataSourceProperty("jdbcCompliantTruncation", "false");

        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "275");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        // Recover if connection gets interrupted
        hikariConfig.addDataSourceProperty("socketTimeout", String.valueOf(TimeUnit.SECONDS.toMillis(30)));

        dataSource = new HikariDataSource(hikariConfig);

        connection = dataSource.getConnection();
    }

    @Override
    public void createUserLanguageTable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void saveUserLanguage(UUID uuid, CommonLocale locale) {
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
