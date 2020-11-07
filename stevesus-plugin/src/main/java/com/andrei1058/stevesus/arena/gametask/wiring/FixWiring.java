package com.andrei1058.stevesus.arena.gametask.wiring;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.task.GameTask;
import com.andrei1058.stevesus.api.arena.task.TaskHandler;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Arrays;
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

    /**
     * Some panels cannot be first and that's why flags were introduced.
     */
    public enum PanelFlag {
        NEVER_FIRST(ChatColor.AQUA + "Never First", 2),
        NEVER_LAST(ChatColor.LIGHT_PURPLE + "Never Last", 1),
        REGULAR(ChatColor.GOLD + "Regular", 0);

        private final String description;
        private final int weight;

        PanelFlag(String description, int weight) {
            this.description = description;
            this.weight = weight;
        }

        public String getDescription() {
            return description;
        }

        /**
         * Used for setup purposes.
         */
        PanelFlag next() {
            int next = weight + 1;
            return Arrays.stream(values()).filter(flag -> flag.weight == next).findAny().orElse(REGULAR);
        }
    }
}
