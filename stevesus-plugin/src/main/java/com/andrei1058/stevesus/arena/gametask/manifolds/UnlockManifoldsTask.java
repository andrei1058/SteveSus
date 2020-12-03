package com.andrei1058.stevesus.arena.gametask.manifolds;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.GameListener;
import com.andrei1058.stevesus.api.arena.task.GameTask;
import com.andrei1058.stevesus.api.arena.task.TaskProvider;
import com.andrei1058.stevesus.api.locale.Locale;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.hook.glowing.GlowingManager;
import com.andrei1058.stevesus.language.LanguageManager;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class UnlockManifoldsTask extends GameTask {

    private final String localName;
    // player uuid, finished boolean
    private final HashMap<UUID, Boolean> assignedPlayers = new HashMap<>();
    private final Arena arena;
    private final Shulker shulkerEntity;

    public UnlockManifoldsTask(String localName, Arena arena, Location location) {
        this.localName = localName;
        this.arena = arena;
        LanguageManager.getINSTANCE().getDefaultLocale().setMsg(Message.GAME_TASK_PATH_ + UnlockManifoldsProvider.getInstance().getIdentifier() + "-" + localName, "&0Count to ten");

        shulkerEntity = location.getWorld().spawn(location, Shulker.class);
        shulkerEntity.setInvulnerable(true);
        shulkerEntity.setAI(false);
        shulkerEntity.setSilent(true);
        shulkerEntity.setColor(DyeColor.BLACK);
        arena.registerGameListener(new GameListener() {
            @Override
            public void onPlayerInteractEntity(Arena arena, Player player, Entity entity) {
                if (entity.equals(shulkerEntity)){
                    tryOpenGUI(player, arena);
                }
            }

            @Override
            public void onEntityPunch(Arena arena, Player player, Entity entity) {
                if (entity.equals(shulkerEntity)){
                    tryOpenGUI(player, arena);
                }
            }
        });
    }

    @Override
    public TaskProvider getHandler() {
        return UnlockManifoldsProvider.getInstance();
    }

    @Override
    public String getLocalName() {
        return localName;
    }

    @Override
    public void onInterrupt(Player player, Arena arena) {
        if (isDoingTask(player)) {
            player.closeInventory();
        }
    }

    @Override
    public int getCurrentStage(Player player) {
        return assignedPlayers.getOrDefault(player.getUniqueId(), false) ? 1 : 0;
    }

    @Override
    public int getCurrentStage(UUID player) {
        return assignedPlayers.getOrDefault(player, false) ? 1 : 0;
    }

    @Override
    public int getTotalStages(Player player) {
        return 1;
    }

    @Override
    public int getTotalStages(UUID player) {
        return 1;
    }

    @Override
    public void assignToPlayer(Player player, Arena arena) {
        assignedPlayers.remove(player.getUniqueId());
        assignedPlayers.put(player.getUniqueId(), false);
        GlowingManager.setGlowingYellow(shulkerEntity, player);
    }

    @Override
    public Set<UUID> getAssignedPlayers() {
        return assignedPlayers.keySet();
    }

    @Override
    public boolean hasTask(Player player) {
        return assignedPlayers.containsKey(player.getUniqueId());
    }

    @Override
    public boolean isDoingTask(Player player) {
        if (player.getOpenInventory() == null){
            return false;
        }
        if (player.getOpenInventory().getTopInventory() == null){
            return false;
        }
        return player.getOpenInventory().getTopInventory().getHolder().getClass().getSimpleName().equals(UnlockGUI.UnlockManifoldsHandler.class.getSimpleName());
    }

    @Override
    public void enableIndicators() {
        for (Player player : arena.getPlayers()){
            if (hasTask(player) && (getCurrentStage(player) != getTotalStages(player))){
                GlowingManager.setGlowingYellow(shulkerEntity, player);
            }
        }
    }

    @Override
    public void disableIndicators() {
        for (Player player : arena.getPlayers()){
            if (hasTask(player)){
                GlowingManager.removeGlowing(shulkerEntity, player);
            }
        }
    }

    public void markDone(Player player) {
        player.closeInventory();
        assignedPlayers.replace(player.getUniqueId(), true);
        arena.refreshTaskMeter();
        arena.getGameEndConditions().tickGameEndConditions(arena);
        GlowingManager.removeGlowing(shulkerEntity, player);
    }

    private void tryOpenGUI(Player player, Arena arena) {
        if (!arena.isTasksAllowedATM()) return;
        if (hasTask(player)) {
            // should prevent called twice
            if (isDoingTask(player)) return;
            if (getCurrentStage(player) != getTotalStages(player)) {
                    SteveSus.newChain().async(() -> {
                        Locale playerLang = LanguageManager.getINSTANCE().getLocale(player);
                        UnlockGUI gui = new UnlockGUI(playerLang, getLocalName(), this);
                        SteveSus.newChain().sync(() -> gui.open(player)).execute();
                    }).execute();
            }
        }
    }
}
