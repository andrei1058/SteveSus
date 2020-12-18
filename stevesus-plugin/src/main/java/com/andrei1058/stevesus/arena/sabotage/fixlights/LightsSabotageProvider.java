package com.andrei1058.stevesus.arena.sabotage.fixlights;

import com.andrei1058.stevesus.api.SteveSusAPI;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.sabotage.SabotageBase;
import com.andrei1058.stevesus.api.arena.sabotage.SabotageProvider;
import com.andrei1058.stevesus.api.locale.LocaleManager;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.api.setup.SetupSession;
import com.andrei1058.stevesus.config.properties.OrphanLocationProperty;
import com.google.gson.JsonObject;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LightsSabotageProvider extends SabotageProvider {

    public static String NAME_PATH;
    public static String FIXED_SUBTITLE;
    private static LightsSabotageProvider instance;

    private LightsSabotageProvider() {
    }

    public static LightsSabotageProvider getInstance() {
        return instance == null ? instance = new LightsSabotageProvider() : instance;
    }

    @Override
    public void onRegister() {
        LocaleManager localeManager = SteveSusAPI.getInstance().getLocaleHandler();
        NAME_PATH = Message.SABOTAGE_PATH_.toString() + getUniqueIdentifier() + "-boss-bar";
        FIXED_SUBTITLE = Message.SABOTAGE_PATH_.toString() + getUniqueIdentifier() + "-to-subtitle";
        localeManager.getDefaultLocale().addDefault(FIXED_SUBTITLE, "&aLights Fixed!");
        localeManager.getDefaultLocale().addDefault(NAME_PATH, "&cFix Lights");
    }

    @Override
    public Plugin getOwner() {
        return SteveSusAPI.getInstance();
    }

    @Override
    public @NotNull String getUniqueIdentifier() {
        return "lights";
    }

    @Override
    public @Nullable SabotageBase onArenaInit(Arena arena, JsonObject configuration) {
        if (arena.hasLoadedSabotage(getUniqueIdentifier())) return null;
        // check required data
        if (!(configuration.has("location"))) return null;

        // load location
        OrphanLocationProperty importer = new OrphanLocationProperty();
        Location loc = importer.convert(configuration.get("location").getAsString(), null);
        if (loc == null) return null;
        loc.setWorld(arena.getWorld());
        return new LightsSabotage(arena, loc);
    }

    @Override
    public void onSetupSessionClose(SetupSession setupSession) {

    }
}
