package com.andrei1058.stevesus.arena.gametask.wiring;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.GameListener;
import com.andrei1058.stevesus.api.arena.task.GameTask;
import com.andrei1058.stevesus.api.arena.task.TaskProvider;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class FixWiring extends GameTask {

    // player, player current stage. max stage == finished
    private final LinkedHashMap<UUID, Integer> assignedPlayers = new LinkedHashMap<>();

    private final List<WiringPanel> wiringPanels = new ArrayList<>();
    private final int stages;
    private final String localName;
    private final List<UUID> currentlyDoingThisTask = new ArrayList<>();

    public FixWiring(List<WiringPanel> panelList, int stages, String localName, Arena arena) {
        wiringPanels.addAll(panelList);
        this.stages = stages;
        this.localName = localName;
        arena.registerGameListener(new WiringListener());
    }

    @Override
    public TaskProvider getHandler() {
        return FixWiringProvider.getInstance();
    }

    @Override
    public String getLocalName() {
        return localName;
    }

    @Override
    public void onInterrupt(Player player, Arena arena) {
        if (isDoingTask(player)){
            //todo
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
        return stages;
    }

    @Override
    public int getTotalStages(UUID player) {
        return stages;
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
    public boolean isDoingTask(UUID player) {
        return currentlyDoingThisTask.contains(player);
    }

    @Override
    public void enableIndicators() {
        //todo
    }

    @Override
    public void disableIndicators() {
//todo
    }

    /**
     * Some panels cannot be first and that's why flags were introduced.
     */
    public enum PanelFlag {
        NEVER_FIRST(ChatColor.AQUA + "Never First", 2),
        NEVER_LAST(ChatColor.DARK_GREEN + "Never Last", 1),
        REGULAR(ChatColor.GOLD + "Regular", 0),
        ALWAYS_FIRST(ChatColor.GREEN + "Always First", 3),
        ALWAYS_LAST(ChatColor.LIGHT_PURPLE + "Always Last", 4);

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

    private class WiringListener implements GameListener {
        // todo
    }
}
