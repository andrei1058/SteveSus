package dev.andrei1058.game.arena.gametask.scan;

import com.andrei1058.hologramapi.Hologram;
import com.andrei1058.hologramapi.HologramPage;
import com.andrei1058.hologramapi.content.LineTextContent;
import dev.andrei1058.game.SteveSus;
import dev.andrei1058.game.api.arena.Arena;
import dev.andrei1058.game.api.arena.GameListener;
import dev.andrei1058.game.api.arena.task.GameTask;
import dev.andrei1058.game.api.arena.task.TaskProvider;
import dev.andrei1058.game.api.arena.team.Team;
import dev.andrei1058.game.api.event.PlayerTaskDoneEvent;
import dev.andrei1058.game.api.locale.Message;
import dev.andrei1058.game.common.api.arena.GameState;
import dev.andrei1058.game.language.LanguageManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.*;

public class SubmitScanTask extends GameTask {

    private final double capsuleRadius;
    private int scanDuration;
    private final Hologram taskHologram;
    private final List<Location> scanParticles;
    private final Location scanCapsuleLocation;
    // one scan at a time.
    private UUID currentScan = null;
    private int currentScanTask = -1;

    public SubmitScanTask(double radius, int scanDuration, Location capsuleLocation, Arena arena, String localName) {
        super(localName);
        this.scanCapsuleLocation = capsuleLocation.clone();
        this.scanCapsuleLocation.add(0, 1, 0);
        this.capsuleRadius = radius;
        this.scanDuration = scanDuration;
        this.taskHologram = new Hologram(capsuleLocation.clone().add(0, 1.8, 0), 2);
        HologramPage page = taskHologram.getPage(0);
        assert page != null;
        page.setLineContent(0, new LineTextContent(s -> LanguageManager.getINSTANCE().getMsg(s, Message.GAME_TASK_NAME_PATH_.toString() + getHandler().getIdentifier())));
        page.setLineContent(1, new LineTextContent(s -> LanguageManager.getINSTANCE().getMsg(s, Message.GAME_TASK_DESCRIPTION_PATH_.toString() + getHandler().getIdentifier())));

        scanParticles = new ArrayList<>(SubmitScanProvider.getInstance().getCircle(capsuleLocation.add(0, 0.3, 0), radius, 15));

        arena.registerGameListener(new ScanListener());
    }

    // player, player current stage. max stage == finished
    private final LinkedHashMap<UUID, Integer> assignedPlayers = new LinkedHashMap<>();

    @Override
    public TaskProvider getHandler() {
        return SubmitScanProvider.getInstance();
    }

    @Override
    public void onInterrupt(Player player, Arena arena) {
        if (isDoingTask(player)) {
            cancelScan(false, player, arena);
        }
    }

    @Override
    public int getCurrentStage(Player player) {
        return assignedPlayers.getOrDefault(player.getUniqueId(), 0);
    }

    @Override
    public int getCurrentStage(UUID player) {
        return assignedPlayers.getOrDefault(player, 0);
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
        assignedPlayers.put(player.getUniqueId(), 0);
    }

    @Override
    public Set<UUID> getAssignedPlayers() {
        return Collections.unmodifiableSet(assignedPlayers.keySet());
    }

    @Override
    public boolean hasTask(Player player) {
        return assignedPlayers.containsKey(player.getUniqueId());
    }

    @Override
    public boolean isDoingTask(Player player) {
        return currentScan != null && currentScan.equals(player.getUniqueId());
    }

    @Override
    public void enableIndicators() {
        taskHologram.show();
    }

    @Override
    public void disableIndicators() {
        taskHologram.hide();
    }


    /**
     * Start scan for current player.
     *
     * @return false if scan in use.
     */
    private boolean startScan(Player player, Arena arena) {
        if (currentScan != null) return false;
        if (!arena.isTasksAllowedATM()) return false;
        if (arena.getCamHandler() != null && arena.getCamHandler().isOnCam(player, arena)) return false;
        player.teleport(scanCapsuleLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
        arena.setCantMove(player, true);
        Team playerTeam = arena.getPlayerTeam(player);
        currentScan = player.getUniqueId();
        final int fraction = 4;
        final int[] currentSecond = {scanDuration * fraction}; // its * multiplied because task is running every half tick
        final double maxY = 2.5;
        final double[] currentY = {0.2};
        final boolean[] upDirection = {true};
        final double spaceUnit = 0.25;
        final double[] currentPitch = {0.9};
        currentScanTask = Bukkit.getScheduler().runTaskTimer(SteveSus.getInstance(), () -> {
            if (currentSecond[0] <= 0) {
                cancelScan(true, player, arena);
            } else {
                currentSecond[0]--;
                player.sendTitle(" ", LanguageManager.getINSTANCE().getMsg(player, SubmitScanProvider.MSG_SCANNING_SUBTITLE).replace("{time}", String.valueOf(currentSecond[0] / fraction + 1)), 0, 10, 0);
                if (upDirection[0]) {
                    if (currentY[0] >= maxY) {
                        upDirection[0] = false;
                        currentY[0] -= spaceUnit;
                        currentPitch[0] -= 0.1;
                    } else {
                        currentY[0] += spaceUnit;
                        currentPitch[0] += 0.1;
                    }
                } else {
                    if (currentY[0] <= 0) {
                        upDirection[0] = true;
                        currentY[0] += spaceUnit;
                        currentPitch[0] += 0.1;
                    } else {
                        currentY[0] -= spaceUnit;
                        currentPitch[0] -= 0.1;
                    }
                }
                player.playSound(scanCapsuleLocation, Sound.BLOCK_NOTE_BASS, (float) 1, (float) currentPitch[0]);
                if (arena.getLiveSettings().isVisualTasksEnabled() && getHandler().isVisual() && (playerTeam == null || !playerTeam.getIdentifier().endsWith("-ghost"))) {
                    scanParticles.forEach(loc -> loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc.clone().add(0, currentY[0], 0), 1));
                } else {
                    scanParticles.forEach(loc -> player.spawnParticle(Particle.VILLAGER_HAPPY, loc.clone().add(0, currentY[0], 0), 1));
                }
            }
        }, 0L, 20 / fraction).getTaskId();
        return true;
    }

    private void cancelScan(boolean done, Player player, Arena arena) {
        if (currentScan == null) return;
        arena.setCantMove(player, false);
        if (currentScanTask != -1) {
            Bukkit.getScheduler().cancelTask(currentScanTask);
        }
        if (done) {
            taskHologram.hide(player);
            assignedPlayers.replace(currentScan, 1);
            player.sendTitle(" ", LanguageManager.getINSTANCE().getMsg(player, SubmitScanProvider.MSG_SCANNING_DONE), 0, 10, 0);
            player.playSound(player.getLocation(), Sound.ENTITY_CAT_PURREOW, 1, 1);
            arena.refreshTaskMeter();
            arena.getGameEndConditions().tickGameEndConditions(arena);
            PlayerTaskDoneEvent taskDoneEvent = new PlayerTaskDoneEvent(arena, this, player);
            Bukkit.getPluginManager().callEvent(taskDoneEvent);
        }
        currentScan = null;
        currentScanTask = -1;
    }

    @SuppressWarnings("unused")
    public void setScanDuration(int scanDuration) {
        this.scanDuration = scanDuration;
    }

    private class ScanListener implements GameListener {

        @Override
        public void onPlayerToggleSneakEvent(Arena arena, Player player, boolean isSneaking) {
            if (isSneaking) {
                if (!hasTask(player)) return;
                if (getCurrentStage(player) == getTotalStages(player)) return;
                if (player.getLocation().distance(scanCapsuleLocation) > capsuleRadius) return;
                // check location
                if (!startScan(player, arena)) {
                    player.sendTitle(" ", LanguageManager.getINSTANCE().getMsg(player, SubmitScanProvider.MSG_CANNOT_SCAN), 0, 35, 0);
                    if (getHandler().isVisual() && arena.getLiveSettings().isVisualTasksEnabled()) {
                        player.playEffect(EntityEffect.VILLAGER_ANGRY);
                    }
                }
            }
        }

        @Override
        public void onGameStateChange(Arena arena, GameState oldState, GameState newState) {
            if (newState == GameState.IN_GAME) {
                for (Player player : arena.getPlayers()) {
                    if (hasTask(player)) {
                        taskHologram.show(player);
                    }
                }
            }
        }

        @Override
        public void onPlayerJoin(Arena arena, Player player) {
            taskHologram.hide(player);
        }
    }
}
