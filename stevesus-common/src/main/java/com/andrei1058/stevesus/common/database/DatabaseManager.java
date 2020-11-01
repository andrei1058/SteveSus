package com.andrei1058.stevesus.common.database;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.SettingsManagerBuilder;
import com.andrei1058.dbi.DatabaseAdapter;
import com.andrei1058.dbi.adapter.HikariAdapter;
import com.andrei1058.dbi.adapter.SQLiteAdapter;
import com.andrei1058.stevesus.common.api.database.DatabaseService;
import com.andrei1058.stevesus.common.database.adapter.NoDatabase;
import com.andrei1058.stevesus.common.database.config.DatabaseConfig;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.SQLException;

public class DatabaseManager implements DatabaseService {

    private static DatabaseManager INSTANCE;

    private DatabaseAdapter databaseAdapter;
    private final SettingsManager databaseConfig;
    private final File databaseFolder;

    private DatabaseManager(Plugin plugin, String databaseFolderPath) {
        databaseAdapter = new NoDatabase();

        File dbFolder = plugin.getDataFolder();
        if (!databaseFolderPath.isEmpty()) {
            File newPath = new File(databaseFolderPath);
            if (dbFolder.isDirectory()) {
                dbFolder = newPath;
                plugin.getLogger().info("Set database configuration path to: " + databaseFolderPath);
            } else {
                plugin.getLogger().warning("Tried to set database configuration path to: " + databaseFolderPath + " but it does not seem like a directory.");
            }
        }
        this.databaseFolder = dbFolder;
        if (!databaseFolder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            databaseFolder.mkdir();
        }
        databaseConfig = SettingsManagerBuilder.withYamlFile(new File(getDatabaseConfigurationPath(), "database.yml")).configurationData(DatabaseConfig.class).useDefaultMigrationService().create();
    }

    public static void onLoad(Plugin plugin, String databaseFolderPath) {
        if (INSTANCE == null) {
            INSTANCE = new DatabaseManager(plugin, databaseFolderPath);

            if (INSTANCE.databaseConfig.getProperty(DatabaseConfig.DATABASE_ENABLED)) {
                try {
                    DatabaseManager.getINSTANCE().setDatabaseAdapter(new HikariAdapter(plugin.getName() + "JdbcPool",
                            INSTANCE.databaseConfig.getProperty(DatabaseConfig.DATABASE_POOL_SIZE).orElse(10),
                            INSTANCE.databaseConfig.getProperty(DatabaseConfig.DATABASE_MAX_LIFETIME).orElse(1800),
                            INSTANCE.databaseConfig.getProperty(DatabaseConfig.DATABASE_HOST),
                            INSTANCE.databaseConfig.getProperty(DatabaseConfig.DATABASE_PORT),
                            INSTANCE.databaseConfig.getProperty(DatabaseConfig.DATABASE_NAME),
                            INSTANCE.databaseConfig.getProperty(DatabaseConfig.DATABASE_USER),
                            INSTANCE.databaseConfig.getProperty(DatabaseConfig.DATABASE_PASS),
                            INSTANCE.databaseConfig.getProperty(DatabaseConfig.DATABASE_VERIFY_CERTIFICATE).orElse(true),
                            INSTANCE.databaseConfig.getProperty(DatabaseConfig.DATABASE_SSL)
                    ));
                } catch (SQLException e) {
                    plugin.getLogger().severe("Cannot connect to database!");
                    e.printStackTrace();
                    plugin.getLogger().info("Fallback on SQLite adapter.");
                    try {
                        if (DatabaseManager.getINSTANCE().setDatabaseAdapter(new SQLiteAdapter(getINSTANCE().getSQLiteFile().toURI().getPath()))) {
                            DatabaseManager.getINSTANCE().setDatabaseAdapter(new NoDatabase());
                        }
                    } catch (SQLException exception) {
                        exception.printStackTrace();
                        plugin.getLogger().info("Using no database integration.");
                        DatabaseManager.getINSTANCE().setDatabaseAdapter(new NoDatabase());
                    }
                }
            } else {
                plugin.getLogger().info("Using SQLite adapter.");
                try {
                    DatabaseManager.getINSTANCE().setDatabaseAdapter(new SQLiteAdapter(getINSTANCE().getSQLiteFile().toURI().getPath()));
                } catch (SQLException exception) {
                    exception.printStackTrace();
                    plugin.getLogger().info("Using no database integration.");
                    DatabaseManager.getINSTANCE().setDatabaseAdapter(new NoDatabase());
                }
            }

        }
    }

    public static void onDisable() {
        if (INSTANCE == null) return;
        if (INSTANCE.databaseAdapter != null) {
            INSTANCE.databaseAdapter.disable();
        }
    }

    public static DatabaseManager getINSTANCE() {
        return INSTANCE;
    }

    @Override
    public DatabaseAdapter getDatabase() {
        return databaseAdapter;
    }

    @Override
    public boolean setDatabaseAdapter(@Nullable DatabaseAdapter adapter) {
        if (adapter == null) {
            INSTANCE.databaseAdapter = new NoDatabase();
            return true;
        }
        DatabaseAdapter old = INSTANCE.databaseAdapter;
        INSTANCE.databaseAdapter = adapter;
        old.disable();
        return true;
    }

    @SuppressWarnings("unused")
    public SettingsManager getDatabaseConfig() {
        return databaseConfig;
    }

    @Override
    public File getSQLiteFile() {
        return new File(getDatabaseConfigurationPath(), "localDatabase.db");
    }

    @Override
    public File getDatabaseConfigurationPath() {
        return databaseFolder;
    }
}
