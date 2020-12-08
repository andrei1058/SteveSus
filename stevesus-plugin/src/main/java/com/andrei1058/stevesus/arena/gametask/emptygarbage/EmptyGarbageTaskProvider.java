package com.andrei1058.stevesus.arena.gametask.emptygarbage;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.task.GameTask;
import com.andrei1058.stevesus.api.arena.task.TaskProvider;
import com.andrei1058.stevesus.api.arena.task.TaskType;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.api.setup.SetupSession;
import com.andrei1058.stevesus.config.properties.OrphanLocationProperty;
import com.andrei1058.stevesus.language.LanguageManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EmptyGarbageTaskProvider extends TaskProvider {

    private static EmptyGarbageTaskProvider instance;
    public static String NEXT_PANEL;
    public static String LEVER_NAME;
    public static String LEVER_LORE;
    public static String GARBAGE_GUI_NAME;
    public static String GARBAGE_ITEM_NAME;
    public static String GARBAGE_ITEM_LORE;

    private EmptyGarbageTaskProvider() {
        NEXT_PANEL = Message.GAME_TASK_PATH_.toString() + getIdentifier() + "-panel-next";
        LEVER_NAME = Message.GAME_TASK_PATH_.toString() + getIdentifier() + "-lever-item-name";
        LEVER_LORE = Message.GAME_TASK_PATH_.toString() + getIdentifier() + "-lever-item-lore";
        GARBAGE_GUI_NAME = Message.GAME_TASK_PATH_.toString() + getIdentifier() + "-garbage-gui-name";
        GARBAGE_ITEM_NAME = Message.GAME_TASK_PATH_.toString() + getIdentifier() + "-garbage-item-name";
        GARBAGE_ITEM_LORE = Message.GAME_TASK_PATH_.toString() + getIdentifier() + "-garbage-item-lore";
        LanguageManager.getINSTANCE().getDefaultLocale().addDefault(NEXT_PANEL, "&7Next garbage to be emptied is in: {room}.");
        LanguageManager.getINSTANCE().getDefaultLocale().addDefault(LEVER_NAME, "&6&lLEVER");
        LanguageManager.getINSTANCE().getDefaultLocale().addDefault(LEVER_LORE, Arrays.asList("&fClick to", "&fempty garbage!"));
        LanguageManager.getINSTANCE().getDefaultLocale().addDefault(GARBAGE_ITEM_NAME, "&8Garbage");
        LanguageManager.getINSTANCE().getDefaultLocale().addDefault(GARBAGE_GUI_NAME, "&0Empty Garbage");
        LanguageManager.getINSTANCE().getDefaultLocale().addDefault(GARBAGE_ITEM_LORE, "&7Click the lever.");
    }


    public static EmptyGarbageTaskProvider getInstance() {
        if (instance == null) {
            instance = new EmptyGarbageTaskProvider();
        }
        return instance;
    }


    @Override
    public String getDefaultDisplayName() {
        return "&6Empty Garbage";
    }

    @Override
    public String getDefaultDescription() {
        return "&fClick the lever many items in the GUI.";
    }

    @Override
    public String getIdentifier() {
        return "empty_garbage";
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
        return true;
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
        if (!configuration.has("list")) return null;
        if (!configuration.has("stages")) return null;
        JsonArray array = configuration.get("list").getAsJsonArray();
        if (array.isJsonNull()) {
            return null;
        }
        List<WallLever> wallLevers = new ArrayList<>();
        for (JsonElement element : array) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.isJsonNull()) continue;
            if (!obj.has("location")) continue;
            JsonElement loc = obj.get("location");
            Location location = new OrphanLocationProperty().convert(loc.getAsString(), null);
            if (location == null) continue;
            location.setWorld(arena.getWorld());
            Location drop = null;
            if (obj.has("drop")) {
                drop = new OrphanLocationProperty().convert(obj.get("drop").getAsString(), null);
                if (drop != null) {
                    drop.setWorld(arena.getWorld());
                }
            }
            OrderPriority priority = OrderPriority.NONE;
            if (obj.has("order")) {
                try {
                    priority = OrderPriority.valueOf(obj.get("order").getAsString().toUpperCase());
                } catch (Exception ignored) {
                }
            }
            wallLevers.add(new WallLever(location, drop, priority));
        }
        int stages = configuration.get("stages").getAsInt();
        return new EmptyGarbageTask(localName, arena, wallLevers, stages);
    }
}
