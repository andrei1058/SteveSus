package dev.andrei1058.game.arena.gametask.upload;

import dev.andrei1058.game.SteveSus;
import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.arena.GameListener;
import dev.andrei1058.game.api.arena.room.GameRoom;
import dev.andrei1058.game.api.arena.task.GameTask;
import dev.andrei1058.game.api.arena.task.TaskProvider;
import dev.andrei1058.game.api.event.PlayerTaskDoneEvent;
import dev.andrei1058.game.api.locale.Locale;
import dev.andrei1058.game.api.locale.Message;
import dev.andrei1058.game.arena.gametask.upload.panel.UploadGUI;
import dev.andrei1058.game.arena.gametask.upload.panel.WallPanel;
import dev.andrei1058.game.common.api.arena.GameState;
import dev.andrei1058.game.hook.glowing.GlowingManager;
import dev.andrei1058.game.language.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

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

    public UploadTask(GameArena gameArena, int downloadTime, int uploadTime, LinkedList<Location> downloadFrame, LinkedList<Location> uploadFrame, String localName) {
        super(localName);
        this.downloadTime = downloadTime;
        this.uploadTime = uploadTime;

        downloadFrame.forEach(loc -> panels.add(new WallPanel(gameArena, loc, WallPanel.PanelType.DOWNLOAD)));
        uploadFrame.forEach(loc -> panels.add(new WallPanel(gameArena, loc, WallPanel.PanelType.UPLOAD)));
        gameArena.registerGameListener(new UploadTaskListener());
    }

    public void markPanelFinished(Player player, GameArena gameArena) {
        if (!hasTask(player)) return;
        WallPanel panel = currentPlayerStage.get(player.getUniqueId());
        if (panel == null) {
            // already finished;
            return;
        }
        panel.getHologram().hide(player);
        GlowingManager.getInstance().removeGlowing(panel.getItemFrame(), player);
        int taskId = currentlyDoing.remove(player.getUniqueId());
        Bukkit.getScheduler().cancelTask(taskId);
        if (panel.getPanelType() == WallPanel.PanelType.DOWNLOAD) {
            List<WallPanel> nextPanel = panels.stream().filter(next -> next.getPanelType() == WallPanel.PanelType.UPLOAD).collect(Collectors.toList());
            if (nextPanel.isEmpty()) {
                SteveSus.getInstance().getLogger().warning("Cannot assign upload at UploadTask on " + gameArena.getTemplateWorld() + "(" + gameArena.getGameId() + ") because there are no upload panels assigned.");
                currentPlayerStage.remove(player.getUniqueId());
                currentPlayerStage.put(player.getUniqueId(), null);
                gameArena.refreshTaskMeter();
                gameArena.getGameEndConditions().tickGameEndConditions(gameArena);
                return;
            }
            Collections.shuffle(nextPanel);
            WallPanel newPanel = nextPanel.get(0);
            currentPlayerStage.remove(player.getUniqueId());
            currentPlayerStage.put(player.getUniqueId(), newPanel);
            newPanel.getHologram().show(player);
            GlowingManager.setGlowingBlue(newPanel.getItemFrame(), player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            GameRoom room = gameArena.getRoom(newPanel.getItemFrame().getLocation());
            Locale playerLang = LanguageManager.getINSTANCE().getLocale(player);
            player.sendMessage(playerLang.getMsg(player, UploadTaskProvider.UPLOAD_ROOM_MSG).replace("{room}", (room == null ? playerLang.getMsg(null, Message.GAME_ROOM_NO_NAME) : room.getDisplayName(playerLang))));

        } else {
            // done
            player.playSound(player.getLocation(), Sound.ENTITY_CAT_PURREOW, 1, 1);
            currentPlayerStage.remove(player.getUniqueId());
            currentPlayerStage.put(player.getUniqueId(), null);
            gameArena.refreshTaskMeter();
            gameArena.getGameEndConditions().tickGameEndConditions(gameArena);

            PlayerTaskDoneEvent taskDoneEvent = new PlayerTaskDoneEvent(gameArena, this, player);
            Bukkit.getPluginManager().callEvent(taskDoneEvent);
        }
    }

    private class UploadTaskListener implements GameListener {
        @Override
        public void onEntityPunch(GameArena gameArena, Player player, Entity entity) {
            tryOpenGUI(player, entity, gameArena);
        }

        @Override
        public void onPlayerInteractEntity(GameArena gameArena, Player player, Entity entity) {
            tryOpenGUI(player, entity, gameArena);
        }

        @Override
        public void onGameStateChange(GameArena gameArena, GameState oldState, GameState newState) {
            if (newState == GameState.IN_GAME) {
                panels.forEach(panel -> {
                    gameArena.getPlayers().forEach(player -> {
                        if (!GlowingManager.isGlowing(panel.getItemFrame(), player)) {
                            if (panel.getHologram() != null) {
                                panel.getHologram().hide(player);
                            }
                        } else {
                            if (panel.getHologram() != null) {
                                panel.getHologram().show(player);
                            }
                        }
                    });
                    if (panel.getHologram() != null) {
                        panel.getHologram().show();
                    }
                });
            }
        }

        @Override
        public void onPlayerJoin(GameArena gameArena, Player player) {
            if (gameArena.getGameState() == GameState.IN_GAME) {
                for (WallPanel panel : panels) {
                    panel.getHologram().hide(player);
                }
            } else {
                // hide existing glowing
                for (WallPanel panel : panels) {
                    GlowingManager.getInstance().removeGlowing(panel.getItemFrame(), player);
                }
            }
        }

        @Override
        public void onInventoryClose(GameArena gameArena, Player player, Inventory inventory) {
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
    public void onInterrupt(Player player, GameArena gameArena) {

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
    public void assignToPlayer(Player player, GameArena gameArena) {
        if (!hasTask(player)) {
            List<WallPanel> downloads = panels.stream().filter(panel -> panel.getPanelType() == WallPanel.PanelType.DOWNLOAD).collect(Collectors.toList());
            if (downloads.isEmpty()) {
                SteveSus.getInstance().getLogger().warning("Cannot assign task UploadTask on " + gameArena.getTemplateWorld() + "(" + gameArena.getGameId() + ") because there are no download panels assigned.");
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

    private void tryOpenGUI(Player player, Entity entity, GameArena gameArena) {
        if (!gameArena.isTasksAllowedATM()) return;
        if (hasTask(player)) {
            // should prevent called twice
            if (currentlyDoing.containsKey(player.getUniqueId())) return;
            if (getCurrentStage(player) != getTotalStages(player)) {
                if (gameArena.getCamHandler() != null && gameArena.getCamHandler().isOnCam(player, gameArena)) return;
                WallPanel panel = currentPlayerStage.get(player.getUniqueId());
                if (panel != null && panel.getItemFrame().equals(entity)) {
                    SteveSus.newChain().async(() -> {
                        Locale playerLang = LanguageManager.getINSTANCE().getLocale(player);
                        UploadGUI gui = new UploadGUI(playerLang, panel.getPanelType(), this, panel.getPanelType() == WallPanel.PanelType.DOWNLOAD ? getDownloadTime() : getUploadTime(), player, gameArena);
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
