package dev.andrei1058.game.common.api.database;

import com.andrei1058.dbi.DatabaseAdapter;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface DatabaseService {

    DatabaseAdapter getDatabase();

    /**
     * Null to restore to server's default.
     */
    boolean setDatabaseAdapter(@Nullable DatabaseAdapter adapter);

    File getSQLiteFile();

    /**
     * Get the folder that contains the database yml configuration file.
     */
    File getDatabaseConfigurationPath();
}
