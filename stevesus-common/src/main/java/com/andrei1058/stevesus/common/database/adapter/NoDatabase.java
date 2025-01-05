package com.andrei1058.stevesus.common.database.adapter;

import com.andrei1058.stevesus.common.api.database.DatabaseAdapter;
import com.andrei1058.stevesus.common.api.locale.CommonLocale;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NoDatabase implements DatabaseAdapter {

    @Override
    public void createUserLanguageTable() {

    }

    @Override
    public void saveUserLanguage(UUID uuid, CommonLocale locale) {

    }

    @Override
    public @Nullable String getUserLanguage(UUID uuid) {
        return null;
    }

    @Override
    public boolean hasUserStats(UUID uuid) {
        return false;
    }

    @Override
    public void initUserStats(UUID uuid) {

    }

    @Override
    public void saveUserStats(UUID uuid, Map<String, Object> values) {

    }

    @Override
    public HashMap<String, Object> getUserStats(UUID uuid) {
        return null;
    }

    @Override
    public void initUserStatsTable() {

    }

    @Override
    public void disable() {

    }
}
