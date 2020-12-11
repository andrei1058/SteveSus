package com.andrei1058.stevesus.arena.gametask.startreactor;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.task.GameTask;
import com.andrei1058.stevesus.api.arena.task.TaskProvider;
import com.andrei1058.stevesus.api.arena.task.TaskType;
import com.andrei1058.stevesus.api.setup.SetupSession;
import com.andrei1058.stevesus.config.properties.OrphanLocationProperty;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public class StartReactorTaskProvider extends TaskProvider {

    private static StartReactorTaskProvider instance;

    public static StartReactorTaskProvider getInstance() {
        return instance == null ? instance = new StartReactorTaskProvider() : instance;
    }

    @Override
    public String getDefaultDisplayName() {
        return "&cStart Reactor";
    }

    @Override
    public String getDefaultDescription() {
        return "&fReplicate the pattern.";
    }

    @Override
    public String getIdentifier() {
        return "start_reactor";
    }

    @Override
    public Plugin getProvider() {
        return SteveSus.getInstance();
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.LONG;
    }

    @Override
    public boolean isVisual() {
        return false;
    }

    @Override
    public boolean canSetup(Player player, SetupSession setupSession) {
        return true;
    }

    @Override
    public void onSetupRequest(Player player, SetupSession setupSession, String localName) {

    }

    @Override
    public void onSetupLoad(SetupSession setupSession, String localName, JsonObject configData) {

    }

    @Override
    public void onSetupClose(SetupSession setupSession, String localName, JsonObject configData) {

    }

    @Override
    public void onRemove(SetupSession setupSession, String localName, JsonObject configData) {

    }

    @Override
    public @Nullable GameTask onGameInit(Arena arena, JsonObject configuration, String localName) {
        if (!validateElements(configuration, "loc")) return null;
        JsonElement loc = configuration.get("loc");
        if (loc.isJsonNull()) return null;
        Location location = new OrphanLocationProperty().convert(loc.getAsString(), null);
        if (location == null) return null;
        location.setWorld(arena.getWorld());
        return new StartReactorTask(localName, location, arena);
    }
}
