package com.andrei1058.stevesus.arena.gametask.primeshields;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.task.GameTask;
import com.andrei1058.stevesus.api.arena.task.TaskProvider;
import com.andrei1058.stevesus.api.arena.task.TaskType;
import com.andrei1058.stevesus.api.setup.SetupSession;
import com.andrei1058.stevesus.arena.gametask.startreactor.StartReactorTask;
import com.andrei1058.stevesus.config.properties.OrphanLocationProperty;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public class PrimeShieldsTaskProvider extends TaskProvider {

    private static PrimeShieldsTaskProvider instance;

    public static PrimeShieldsTaskProvider getInstance() {
        return instance == null ? instance = new PrimeShieldsTaskProvider() : instance;
    }

    @Override
    public String getDefaultDisplayName() {
        return "&8Prime Shields";
    }

    @Override
    public String getDefaultDescription() {
        return "Click the red hexagons.";
    }

    @Override
    public String getIdentifier() {
        return "prime_shields";
    }

    @Override
    public Plugin getProvider() {
        return SteveSus.getInstance();
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.SHORT;
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
        return new PrimeShieldsTask(localName, location, arena);
    }
}
