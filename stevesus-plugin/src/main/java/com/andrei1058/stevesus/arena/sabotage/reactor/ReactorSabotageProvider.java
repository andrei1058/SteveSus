package com.andrei1058.stevesus.arena.sabotage.reactor;

import com.andrei1058.stevesus.SteveSus;
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

public class ReactorSabotageProvider extends SabotageProvider {

    public static String NAME_PATH;
    public static String GUI_WAITING;
    public static String GUI_NORMAL;
    public static String FIXED_SUBTITLE;

    private static ReactorSabotageProvider instance;

    private ReactorSabotageProvider() {
    }

    public static ReactorSabotageProvider getInstance() {
        return instance == null ? instance = new ReactorSabotageProvider() : instance;
    }

    @Override
    public void onRegister() {
        LocaleManager localeManager = SteveSusAPI.getInstance().getLocaleHandler();
        NAME_PATH = Message.SABOTAGE_PATH_.toString() + getUniqueIdentifier() + "-boss-bar";
        GUI_WAITING = Message.SABOTAGE_PATH_.toString() + getUniqueIdentifier() + "-gui-waiting";
        GUI_NORMAL = Message.SABOTAGE_PATH_.toString() + getUniqueIdentifier() + "-gui-normal";
        FIXED_SUBTITLE = Message.SABOTAGE_PATH_.toString() + getUniqueIdentifier() + "-to-subtitle";
        localeManager.getDefaultLocale().addDefault(NAME_PATH, "&cFix Reactor Meltdown");
        localeManager.getDefaultLocale().addDefault(Message.DEFEAT_REASON_PATH_.toString() + getUniqueIdentifier(), "&cYou couldn't fix reactor meltdown in time!");
        localeManager.getDefaultLocale().addDefault(GUI_WAITING, "&0Waiting for second user");
        localeManager.getDefaultLocale().addDefault(GUI_NORMAL, "&0Reactor Normal");
        localeManager.getDefaultLocale().addDefault(FIXED_SUBTITLE, "&aReactor Meltdown Fixed!");
    }

    @Override
    public Plugin getOwner() {
        return SteveSus.getInstance();
    }

    @Override
    public @NotNull String getUniqueIdentifier() {
        return "reactor_meltdown";
    }

    @Override
    public @Nullable SabotageBase onArenaInit(Arena arena, JsonObject configuration) {
        if (arena.hasLoadedSabotage(getUniqueIdentifier())) return null;
        if (!(configuration.has("loc1") && configuration.has("loc2") && configuration.has("deadLine"))) return null;
        OrphanLocationProperty importer = new OrphanLocationProperty();
        Location loc1 = importer.convert(configuration.get("loc1").getAsString(), null);
        if (loc1 == null) return null;
        loc1.setWorld(arena.getWorld());
        Location loc2 = importer.convert(configuration.get("loc2").getAsString(), null);
        if (loc2 == null) return null;
        loc2.setWorld(arena.getWorld());
        int deadLine = configuration.get("deadLine").getAsInt();
        return new ReactorSabotage(arena, deadLine,loc1, loc2);
    }

    @Override
    public void onSetupSessionClose(SetupSession setupSession) {

    }
}
