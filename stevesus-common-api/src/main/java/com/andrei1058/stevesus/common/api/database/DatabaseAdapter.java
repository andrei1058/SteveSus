package com.andrei1058.stevesus.common.api.database;

import com.andrei1058.stevesus.common.api.locale.CommonLocale;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public interface DatabaseAdapter {

    /**
     * Create user language table if not exists.
     */
    void createUserLanguageTable();

    /**
     * Save user-selected language.
     *
     * @param uuid   player.
     * @param locale language.
     */
    void saveUserLanguage(UUID uuid, CommonLocale locale);

    /**
     * Fetch user locale preference.
     *
     * @param uuid player.
     * @return iso code language.
     */
    @Nullable String getUserLanguage(UUID uuid);

    /**
     * Check if database contains stats for given user.
     *
     * @param uuid player.
     * @return true if has data.
     */
    boolean hasUserStats(UUID uuid);

    /**
     * Initializer user stats to database.
     *
     * @param uuid player.
     */
    void initUserStats(UUID uuid);

    /**
     * Save-update user stats.
     *
     * @param uuid   player.
     * @param values statistic-id is the key, the number/date/string is the value.
     */
    void saveUserStats(UUID uuid, Map<String, Object> values);

    /**
     * Get user stats.
     *
     * @param uuid player.
     * @return statistic-id is the key, the number/date/string is the value.
     */
    HashMap<String, Object> getUserStats(UUID uuid);

    /**
     * Init user stats table if not exists.
     */
    void initUserStatsTable();

    /**
     * Disable database adapter in case it is replaced with another source.
     */
    void disable();
}
