package com.andrei1058.stevesus.arena.gametask.fuelengines;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.task.GameTask;
import com.andrei1058.stevesus.api.arena.task.TaskProvider;
import com.andrei1058.stevesus.api.arena.task.TaskType;
import com.andrei1058.stevesus.api.setup.SetupSession;
import com.andrei1058.stevesus.config.properties.OrphanLocationProperty;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;

public class FuelEnginesTaskProvider extends TaskProvider {

    private static FuelEnginesTaskProvider instance;

    public static FuelEnginesTaskProvider getInstance() {
        if (instance == null) {
            instance = new FuelEnginesTaskProvider();
        }
        return instance;
    }

    @Override
    public String getDefaultDisplayName() {
        return "&3Fuel Engines";
    }

    @Override
    public String getDefaultDescription() {
        return "Fuel engines by using the gas can.";
    }

    @Override
    public String getIdentifier() {
        return "fuel_engines";
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
        if (configuration.isJsonNull()) return null;
        if (!configuration.has("candidates")) return null;
        if (!configuration.get("candidates").isJsonArray()) {
            return null;
        }
        JsonArray array = configuration.get("candidates").getAsJsonArray();
        LinkedList<FuelStage> taskCandidates = new LinkedList<>();
        for (JsonElement element : array) {
            JsonObject candidate = element.getAsJsonObject();
            if (candidate.isJsonNull()) continue;
            JsonElement storageElement = candidate.get("storage");
            if (storageElement.isJsonNull()) continue;
            JsonElement engineElement = candidate.get("engine");
            if (engineElement.isJsonNull()) continue;
            OrphanLocationProperty convertor = new OrphanLocationProperty();
            Location storage = convertor.convert(storageElement.getAsString(), null);
            if (storage == null) continue;
            storage.setWorld(arena.getWorld());
            Location engine = convertor.convert(engineElement.getAsString(), null);
            if (engine == null) continue;
            engine.setWorld(arena.getWorld());
            FuelStage stage = new FuelStage(storage, engine);
            taskCandidates.add(stage);
        }
        if (taskCandidates.isEmpty()) return null;
        return new FuelEnginesTask(localName, taskCandidates, arena);
    }
}
