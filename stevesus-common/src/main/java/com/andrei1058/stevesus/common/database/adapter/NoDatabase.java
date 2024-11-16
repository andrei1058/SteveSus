package com.andrei1058.stevesus.common.database.adapter;

import com.andrei1058.stevesus.common.api.database.DatabaseAdapter;
import com.andrei1058.stevesus.common.api.locale.CommonLocale;

import java.util.UUID;

public class NoDatabase implements DatabaseAdapter {

    @Override
    public void createUserLanguageTable() {

    }

    @Override
    public void saveUserLanguage(UUID uuid, CommonLocale locale) {

    }

    @Override
    public void disable() {

    }
}
