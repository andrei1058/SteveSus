package com.andrei1058.stevesus.arena.gametask.fuelengines;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.GameListener;
import com.andrei1058.stevesus.api.arena.task.GameTask;
import com.andrei1058.stevesus.api.arena.task.TaskProvider;
import com.andrei1058.stevesus.api.event.PlayerTaskDoneEvent;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.api.server.GameSound;
import com.andrei1058.stevesus.language.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FuelEnginesTask extends GameTask {

    private final LinkedList<FuelStage> availableStages;
    // currentStage%2==0 means stage is done, else he did only the storage part
    private final HashMap<UUID, Integer> currentStage = new HashMap<>();

    public FuelEnginesTask(String localName, LinkedList<FuelStage> availableStages, Arena arena) {
        super(localName);
        this.availableStages = availableStages;
        // hologram lang path
        LanguageManager.getINSTANCE().getDefaultLocale().addDefault(Message.GAME_TASK_PATH_.toString() + getHandler().getIdentifier() + "-" + getLocalName() + "-holo1", "&3&lFuel Station");
        LanguageManager.getINSTANCE().getDefaultLocale().addDefault(Message.GAME_TASK_PATH_.toString() + getHandler().getIdentifier() + "-" + getLocalName() + "-holo2", "&3&lFuel Engine");
        LanguageManager.getINSTANCE().getDefaultLocale().addDefault(Message.GAME_TASK_PATH_.toString() + getHandler().getIdentifier() + "-" + getLocalName() + "-gui-name1", "&0Fuel Station");
        LanguageManager.getINSTANCE().getDefaultLocale().addDefault(Message.GAME_TASK_PATH_.toString() + getHandler().getIdentifier() + "-" + getLocalName() + "-fuel", "&0Fuel");
        LanguageManager.getINSTANCE().getDefaultLocale().addDefault(Message.GAME_TASK_PATH_.toString() + getHandler().getIdentifier() + "-" + getLocalName() + "-button", "&6&lCLICK TO FUEL");
        for (FuelStage fuelStage : availableStages) {
            fuelStage.initHolograms(this);
        }

        arena.registerGameListener(new GameListener() {
            @Override
            public void onPlayerJoin(Arena arena, Player player) {
                for (FuelStage fuelStage : availableStages) {
                    fuelStage.getEngineHologram().hide(player);
                    fuelStage.getStorageHologram().hide(player);
                }
            }

            @Override
            public void onPlayerInteractEntity(Arena arena, Player player, Entity entity) {
                if (entity.getType() != EntityType.MAGMA_CUBE) return;
                if (!hasTask(player)) return;
                FuelStage stage = getCurrent(player);
                if (stage == null) return;
                if (arena.getCamHandler() != null && arena.getCamHandler().isOnCam(player, arena)) return;
                stage.onInteract(player, getCurrentStage(player) % 2 != 0, FuelEnginesTask.this, arena, entity);
            }

            @Override
            public void onEntityPunch(Arena arena, Player player, Entity entity) {
                if (entity.getType() != EntityType.MAGMA_CUBE) return;
                if (!hasTask(player)) return;
                if (arena.getCamHandler() != null && arena.getCamHandler().isOnCam(player, arena)) return;
                FuelStage stage = getCurrent(player);
                if (stage == null) return;
                stage.onInteract(player, getCurrentStage(player) % 2 != 0, FuelEnginesTask.this, arena, entity);

            }
        });
    }

    @Override
    public TaskProvider getHandler() {
        return FuelEnginesTaskProvider.getInstance();
    }

    @Override
    public void onInterrupt(Player player, Arena arena) {
        if (hasTask(player)){
            player.closeInventory();
        }
    }

    @Override
    public int getCurrentStage(UUID player) {
        return currentStage.getOrDefault(player, 0) / 2;
    }

    @Override
    public int getTotalStages(UUID player) {
        return availableStages.size();
    }

    @Override
    public void assignToPlayer(Player player, Arena arena) {
        if (hasTask(player)) return;
        currentStage.put(player.getUniqueId(), 0);
        enableIndicator(player);
    }

    @Override
    public Set<UUID> getAssignedPlayers() {
        return currentStage.keySet();
    }

    @Override
    public boolean hasTask(Player player) {
        return currentStage.containsKey(player.getUniqueId());
    }

    @Override
    public boolean isDoingTask(Player player) {
        return false;
    }

    @Override
    public void enableIndicators() {
        for (UUID uuid : currentStage.keySet()){
            Player p = Bukkit.getPlayer(uuid);
            if (p != null){
                enableIndicator(p);
            }
        }
    }

    @Override
    public void disableIndicators() {
        for (UUID uuid : currentStage.keySet()){
            Player p = Bukkit.getPlayer(uuid);
            if (p != null){
                disableIndicator(p);
            }
        }
    }

    public @Nullable FuelStage getCurrent(Player player) {
        int currentStage = this.currentStage.getOrDefault(player.getUniqueId(), 0);
        int rawStage = currentStage / 2;
        if (availableStages.size() < rawStage) return null;
        return availableStages.get(rawStage);
    }

    private void enableIndicator(Player player) {
        int currentStage = this.currentStage.getOrDefault(player.getUniqueId(), 0);
        boolean halfStage = currentStage % 2 != 0;
        int rawStage = currentStage / 2;
        if (availableStages.size() < rawStage) return;
        FuelStage stage = availableStages.get(rawStage);
        if (halfStage) {
            if (stage.getEngineGlowing() != null) {
                stage.getEngineGlowing().startGlowing(player);
            }
            stage.getEngineHologram().show(player);
        } else {
            if (stage.getStorageGlowing() != null) {
                stage.getStorageGlowing().startGlowing(player);
            }
            stage.getStorageHologram().show(player);
        }
    }

    private void disableIndicator(Player player) {
        int currentStage = this.currentStage.getOrDefault(player.getUniqueId(), 0);
        boolean halfStage = currentStage % 2 != 0;
        int rawStage = currentStage / 2;
        if (availableStages.size() < rawStage) return;
        FuelStage stage = availableStages.get(rawStage);
        if (halfStage) {
            if (stage.getEngineGlowing() != null) {
                stage.getEngineGlowing().stopGlowing(player);
            }
            stage.getEngineHologram().hide(player);
        } else {
            if (stage.getStorageGlowing() != null) {
                stage.getStorageGlowing().stopGlowing(player);
            }
            stage.getStorageHologram().hide(player);
        }
    }

    public void addProgress(Player player, Arena arena) {

        disableIndicator(player);
        int stage = this.currentStage.getOrDefault(player.getUniqueId(), 0) +1;

        currentStage.replace(player.getUniqueId(), stage);

        if (stage == availableStages.size() * 2){
            arena.refreshTaskMeter();
            arena.getGameEndConditions().tickGameEndConditions(arena);
            player.closeInventory();
            PlayerTaskDoneEvent taskDoneEvent = new PlayerTaskDoneEvent(arena, this, player);
            Bukkit.getPluginManager().callEvent(taskDoneEvent);
        }

        enableIndicator(player);
        SteveSus.newChain().delay(15).sync(()-> {
            GameSound.TASK_PROGRESS_DONE.playToPlayer(player);
            player.closeInventory();
        }).execute();
    }
}
