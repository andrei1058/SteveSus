package com.andrei1058.stevesus.arena.gametask.wiring;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.task.GameTask;
import com.andrei1058.stevesus.api.arena.task.TaskHandler;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;

public class FixWiring extends GameTask {

    private final LinkedList<Player> assignedPlayers = new LinkedList<>();

    @Override
    public TaskHandler getHandler() {
        return FixWiringHandler.getInstance();
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
}
