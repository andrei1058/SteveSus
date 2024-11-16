package com.andrei1058.stevesus.common.api.database;

import com.andrei1058.stevesus.common.api.locale.CommonLocale;

import java.util.UUID;

public interface DatabaseAdapter {

    /**
     * Create user language table if not exists.
     */
    void createUserLanguageTable();

    /**
     * Save user-selected language.
     * @param uuid player.
     * @param locale language.
     */
    void saveUserLanguage(UUID uuid, CommonLocale locale);

    void disable();
}
