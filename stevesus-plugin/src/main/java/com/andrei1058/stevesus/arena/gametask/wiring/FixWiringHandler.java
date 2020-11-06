package com.andrei1058.stevesus.arena.gametask.wiring;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.task.GameTask;
import com.andrei1058.stevesus.api.arena.task.TaskHandler;
import com.andrei1058.stevesus.api.arena.task.TaskTriggerType;
import com.andrei1058.stevesus.api.arena.task.TaskType;
import com.andrei1058.stevesus.api.setup.SetupSession;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;

public class FixWiringHandler extends TaskHandler {

    private static FixWiringHandler instance;

    private FixWiringHandler() {
    }

    public static FixWiringHandler getInstance() {
        if (instance == null) {
            instance = new FixWiringHandler();
        }
        return instance;
    }

    @Override
    public String getDefaultDisplayName() {
        return "Fix Wiring";
    }

    @Override
    public String getIdentifier() {
        return "fix_wiring";
    }

    @Override
    public Plugin getProvider() {
        return SteveSus.getInstance();
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.COMMON;
    }

    @Override
    public TaskTriggerType getTriggerType() {
        return TaskTriggerType.INTERACT_ENTITY;
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
        player.getInventory().clear();
        //todo requires "setup interact listener":
        // 1. subscribe from setup manager
        // 2. unregister setup listeners on setup session close
    }

    @Override
    public JSONObject exportAndSave(SetupSession setupSession) {
        return null;
    }

    @Override
    public @Nullable GameTask init(Arena arena, JSONObject configuration) {
        return null;
    }
}
