package com.andrei1058.stevesus.arena.gametask.upload;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.GameListener;
import com.andrei1058.stevesus.api.arena.room.GameRoom;
import com.andrei1058.stevesus.api.arena.task.GameTask;
import com.andrei1058.stevesus.api.arena.task.TaskProvider;
import com.andrei1058.stevesus.api.event.PlayerTaskDoneEvent;
import com.andrei1058.stevesus.api.locale.Locale;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.arena.gametask.upload.panel.UploadGUI;
import com.andrei1058.stevesus.arena.gametask.upload.panel.WallPanel;
import com.andrei1058.stevesus.common.api.arena.GameState;
import com.andrei1058.stevesus.hook.glowing.GlowingManager;
import com.andrei1058.stevesus.language.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class UploadTask extends GameTask {

    private int downloadTime;
    private int uploadTime;
    // if null he ended, if download stage 0, if upload stage 1
    private final HashMap<UUID, WallPanel> currentPlayerStage = new HashMap<>();
    // player, taskId
    private final HashMap<UUID, Integer> currentlyDoing = new HashMap<>();
    private final LinkedList<WallPanel> panels = new LinkedList<>();

    public UploadTask(Arena arena, int downloadTime, int uploadTime, LinkedList<Location> downloadFrame, LinkedList<Location> uploadFrame, String localName) {
        super(localName);
        this.downloadTime = downloadTime;
        this.uploadTime = uploadTime;

        downloadFrame.forEach(loc -> panels.add(new WallPanel(arena, loc, WallPanel.PanelType.DOWNLOAD)));
        uploadFrame.forEach(loc -> panels.add(new WallPanel(arena, loc, WallPanel.PanelType.UPLOAD)));
        arena.registerGameListener(new UploadTaskListener());
    }

    public void markPanelFinished(Player player, Arena arena) {
        if (!hasTask(player)) return;
        WallPanel panel = currentPlayerStage.get(player.getUniqueId());
        if (panel == null) {
            // already finished;
            return;
        }
        if (null != panel.getHologram()) {
            panel.getHologram().hideFromPlayer(player);
        }
        GlowingManager.getInstance().removeGlowing(panel.getItemFrame(), player);
        int taskId = currentlyDoing.remove(player.getUniqueId());
        Bukkit.getScheduler().cancelTask(taskId);
        if (panel.getPanelType() == WallPanel.PanelType.DOWNLOAD) {
            List<WallPanel> nextPanel = panels.stream().filter(next -> next.getPanelType() == WallPanel.PanelType.UPLOAD).collect(Collectors.toList());
            if (nextPanel.isEmpty()) {
                SteveSus.getInstance().getLogger().warning("Cannot assign upload at UploadTask on " + arena.getTemplateWorld() + "(" + arena.getGameId() + ") because there are no upload panels assigned.");
                currentPlayerStage.remove(player.getUniqueId());
                currentPlayerStage.put(player.getUniqueId(), null);
                arena.refreshTaskMeter();
                arena.getGameEndConditions().tickGameEndConditions(arena);
                return;
            }
            Collections.shuffle(nextPanel);
            WallPanel newPanel = nextPanel.get(0);
            currentPlayerStage.remove(player.getUniqueId());
            currentPlayerStage.put(player.getUniqueId(), newPanel);
            if (null != newPanel.getHologram()) {
                newPanel.getHologram().showToPlayer(player);
            }
            GlowingManager.setGlowingBlue(newPanel.getItemFrame(), player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            GameRoom room = arena.getRoom(newPanel.getItemFrame().getLocation());
            Locale playerLang = LanguageManager.getINSTANCE().getLocale(player);
            player.sendMessage(playerLang.getMsg(player, UploadTaskProvider.UPLOAD_ROOM_MSG).replace("{room}", (room == null ? playerLang.getMsg(null, Message.GAME_ROOM_NO_NAME) : room.getDisplayName(playerLang))));

        } else {
            // done
            player.playSound(player.getLocation(), Sound.ENTITY_CAT_PURREOW, 1, 1);
            currentPlayerStage.remove(player.getUniqueId());
            currentPlayerStage.put(player.getUniqueId(), null);
            arena.refreshTaskMeter();
            arena.getGameEndConditions().tickGameEndConditions(arena);

            PlayerTaskDoneEvent taskDoneEvent = new PlayerTaskDoneEvent(arena, this, player);
            Bukkit.getPluginManager().callEvent(taskDoneEvent);
        }
    }

    private class UploadTaskListener implements GameListener {
        @Override
        public void onEntityPunch(Arena arena, Player player, Entity entity) {
            tryOpenGUI(player, entity, arena);
        }

        @Override
        public void onPlayerInteractEntity(Arena arena, Player player, Entity entity) {
            tryOpenGUI(player, entity, arena);
        }

        @Override
        public void onGameStateChange(Arena arena, GameState oldState, GameState newState) {
            if (newState == GameState.IN_GAME) {
                for (Player player : arena.getPlayers()) {
                    if (!hasTask(player)) {
                        continue;
                    }
                    for (WallPanel panel : panels) {
                        if (null == panel.getHologram()) {
                            continue;
                        }
                        panel.getHologram().showToPlayer(player);
                    }
                }
            }
        }

        @Override
        public void onPlayerJoin(@NotNull Arena arena, Player player) {
            if (arena.getGameState() == GameState.IN_GAME) {
                for (WallPanel panel : panels) {
                    if (null != panel.getHologram()) {
                        panel.getHologram().hideFromPlayer(player);
                    }
                }
            } else {
                // hide existing glowing
                for (WallPanel panel : panels) {
                    GlowingManager.getInstance().removeGlowing(panel.getItemFrame(), player);
                }
            }
        }

        @Override
        public void onInventoryClose(Arena arena, Player player, Inventory inventory) {
            if (currentlyDoing.containsKey(player.getUniqueId())) {
                int taskId = currentlyDoing.remove(player.getUniqueId());
                Bukkit.getScheduler().cancelTask(taskId);
            }
        }
    }

    @Override
    public TaskProvider getHandler() {
        return UploadTaskProvider.getInstance();
    }

    @Override
    public void onInterrupt(Player player, Arena arena) {

    }

    @Override
    public int getCurrentStage(Player player) {
        return getCurrentStage(player.getUniqueId());
    }

    @Override
    public int getCurrentStage(UUID player) {
        if (!currentPlayerStage.containsKey(player)) {
            return 0;
        }
        WallPanel panel = currentPlayerStage.get(player);
        if (panel == null) {
            return 2;
        }
        if (panel.getPanelType() == WallPanel.PanelType.DOWNLOAD) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public int getTotalStages(Player player) {
        return 2;
    }

    @Override
    public int getTotalStages(UUID player) {
        return 2;
    }

    @Override
    public void assignToPlayer(Player player, Arena arena) {
        if (!hasTask(player)) {
            List<WallPanel> downloads = panels.stream().filter(panel -> panel.getPanelType() == WallPanel.PanelType.DOWNLOAD).collect(Collectors.toList());
            if (downloads.isEmpty()) {
                SteveSus.getInstance().getLogger().warning("Cannot assign task UploadTask on " + arena.getTemplateWorld() + "(" + arena.getGameId() + ") because there are no download panels assigned.");
                return;
            }
            Collections.shuffle(downloads);
            WallPanel panel = downloads.get(0);
            currentPlayerStage.put(player.getUniqueId(), panel);
            GlowingManager.setGlowingBlue(panel.getItemFrame(), player);
            //todo maybe chat where the panel is.
        }
    }

    @Override
    public Set<UUID> getAssignedPlayers() {
        return Collections.unmodifiableSet(currentPlayerStage.keySet());
    }

    @Override
    public boolean hasTask(Player player) {
        return currentPlayerStage.containsKey(player.getUniqueId());
    }

    @Override
    public boolean isDoingTask(Player player) {
        return currentlyDoing.containsKey(player.getUniqueId());
    }

    @Override
    public void enableIndicators() {
        for (Map.Entry<UUID, WallPanel> entry : currentPlayerStage.entrySet()) {
            if (entry.getValue() != null) {
                // show blue indicator
                GlowingManager.setGlowingBlue(entry.getValue().getItemFrame(), Bukkit.getPlayer(entry.getKey()));
            }
        }
    }

    @Override
    public void disableIndicators() {
        for (Map.Entry<UUID, WallPanel> entry : currentPlayerStage.entrySet()) {
            if (entry.getValue() != null) {
                // disable blue indicator
                GlowingManager.getInstance().removeGlowing(entry.getValue().getItemFrame(), Bukkit.getPlayer(entry.getKey()));
            }
        }
    }

    public int getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(int uploadTime) {
        this.uploadTime = uploadTime;
    }

    public int getDownloadTime() {
        return downloadTime;
    }

    public void setDownloadTime(int downloadTime) {
        this.downloadTime = downloadTime;
    }

    public LinkedList<WallPanel> getPanels() {
        return panels;
    }

    private void tryOpenGUI(Player player, Entity entity, Arena arena) {
        if (!arena.isTasksAllowedATM()) return;
        if (hasTask(player)) {
            // should prevent called twice
            if (currentlyDoing.containsKey(player.getUniqueId())) return;
            if (getCurrentStage(player) != getTotalStages(player)) {
                if (arena.getCamHandler() != null && arena.getCamHandler().isOnCam(player, arena)) return;
                WallPanel panel = currentPlayerStage.get(player.getUniqueId());
                if (panel != null && panel.getItemFrame().equals(entity)) {
                    SteveSus.newChain().async(() -> {
                        Locale playerLang = LanguageManager.getINSTANCE().getLocale(player);
                        UploadGUI gui = new UploadGUI(playerLang, panel.getPanelType(), this, panel.getPanelType() == WallPanel.PanelType.DOWNLOAD ? getDownloadTime() : getUploadTime(), player, arena);
                        SteveSus.newChain().sync(() -> {
                            gui.open(player);
                            currentlyDoing.put(player.getUniqueId(), gui.getTaskId());
                        }).execute();
                    }).execute();
                }
            }
        }
    }
}
