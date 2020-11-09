package com.andrei1058.stevesus.arena.gametask.scan;

import com.andrei1058.hologramapi.Hologram;
import com.andrei1058.hologramapi.HologramPage;
import com.andrei1058.hologramapi.content.LineTextContent;
import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.task.GameTask;
import com.andrei1058.stevesus.api.arena.task.TaskProvider;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.common.api.arena.GameState;
import com.andrei1058.stevesus.language.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SubmitScan extends GameTask {

    private double radius;
    private int scanDuration;
    private final int preGameTaskId;
    private final Hologram taskHologram;

    public SubmitScan(double radius, int scanDuration, Location capsuleLocation, Arena arena) {
        this.taskHologram = new Hologram(capsuleLocation.clone().add(0, 1.8, 0), 2);
        HologramPage page = taskHologram.getPage(0);
        assert page != null;
        page.setLineContent(0, new LineTextContent(s -> LanguageManager.getINSTANCE().getMsg(s, Message.GAME_TASK_NAME_PATH_.toString() + getHandler().getIdentifier())));
        page.setLineContent(1, new LineTextContent(s -> LanguageManager.getINSTANCE().getMsg(s, Message.GAME_TASK_DESCRIPTION_PATH_.toString() + getHandler().getIdentifier())));

        List<Location> preGameParticles = new ArrayList<>(SubmitScanProvider.getInstance().getCircle(capsuleLocation.add(0, 0.3, 0), radius, 15));
        this.preGameTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(SteveSus.getInstance(), () -> preGameParticles.forEach(loc -> {
            loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 1);
            loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc.clone().add(0, 1.2, 0), 1);
        }), 0L, 10).getTaskId();
    }

    private final LinkedList<Player> assignedPlayers = new LinkedList<>();

    @Override
    public TaskProvider getHandler() {
        return SubmitScanProvider.getInstance();
    }

    @Override
    public void onInterrupt(Player player, Arena arena) {

    }

    @Override
    public void assignToPlayer(Player player, Arena arena) {
        assignedPlayers.remove(player);
        assignedPlayers.add(player);
    }

    @Override
    public void assignToPlayers(List<Player> players, Arena arena) {
        players.forEach(assignedPlayers::remove);
        assignedPlayers.addAll(players);
    }

    @Override
    public List<Player> getAssignedPlayers() {
        return assignedPlayers;
    }

    @Override
    public void onGameStateChange(GameState oldState, GameState newState, Arena arena) {
        if (newState == GameState.IN_GAME) {
            Bukkit.getScheduler().cancelTask(preGameTaskId);

            // todo hide hologram for those who do not have this task
        }
    }
}
