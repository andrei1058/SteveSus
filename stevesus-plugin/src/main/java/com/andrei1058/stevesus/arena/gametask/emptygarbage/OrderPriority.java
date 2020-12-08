package com.andrei1058.stevesus.arena.gametask.emptygarbage;

import com.andrei1058.stevesus.SteveSus;
import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public enum OrderPriority {
    NEVER_FIRST(ChatColor.AQUA + "Never First", 2),
    NEVER_LAST(ChatColor.DARK_GREEN + "Never Last", 1),
    NONE(ChatColor.GOLD + "None", 0),
    ALWAYS_FIRST(ChatColor.GREEN + "Always First", 3),
    ALWAYS_LAST(ChatColor.LIGHT_PURPLE + "Always Last", 4);

    private final String description;
    private final int weight;

    OrderPriority(String description, int weight) {
        this.description = description;
        this.weight = weight;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Used for setup purposes.
     */
    public OrderPriority next() {
        int next = weight + 1;
        return Arrays.stream(values()).filter(flag -> flag.weight == next).findAny().orElse(NONE);
    }

    public static LinkedList<WallLever> getLessUsedPanels(int stages, EmptyGarbageTask emptyGarbageTask) {

        LinkedList<WallLever> picked = new LinkedList<>();

        // initialize options with first panel filters
        List<WallLever> options = emptyGarbageTask.getWallLevers().stream().filter(panel -> !(panel.getOrderPriority() == NEVER_FIRST || panel.getOrderPriority() == ALWAYS_LAST)).collect(Collectors.toList());

        // comparison variables
        WallLever current;

        // pick first panel
        if (options.isEmpty()) {
            SteveSus.getInstance().getLogger().warning("Tried to assign FIRST clean garbage panel but they seem to be all assigned as NEVER_FIRST or ALWAYS_LAST");
        } else {
            current = options.remove(0);

            // pick first
            for (WallLever p : options) {
                if (current.getAssignments() >= p.getAssignments()) {
                    current = p;
                }
            }
            current.increaseAssignments();
            picked.add(current);
        }

        // pick middle panels
        options = emptyGarbageTask.getWallLevers().stream().filter(panel -> !(panel.getOrderPriority() == ALWAYS_FIRST || panel.getOrderPriority() == ALWAYS_LAST) && !picked.contains(panel)).collect(Collectors.toList());
        if (!options.isEmpty()) {
            // foreach stage to be added
            for (int x = 1; x < stages - 1; x++) {
                current = options.get(0);
                // check usages
                for (WallLever p : options) {
                    if (current.getAssignments() >= p.getAssignments()) {
                        current = p;
                    }
                }
                picked.remove(current);
                picked.add(current);
                current.increaseAssignments();
            }
        }

        // pick last
        options = emptyGarbageTask.getWallLevers().stream().filter(panel -> !(panel.getOrderPriority() == ALWAYS_FIRST || panel.getOrderPriority() == NEVER_LAST) && !picked.contains(panel)).collect(Collectors.toList());
        if (options.isEmpty()) {
            SteveSus.getInstance().getLogger().warning("Tried to assign LAST clean garbage panel but they seem to be all assigned as ALWAYS_FIRST or NEVER_LAST");
        } else {
            current = options.remove(0);

            for (WallLever p : options) {
                if (current.getAssignments() >= p.getAssignments()) {
                    current = p;
                }
            }
            current.increaseAssignments();
            picked.add(current);
        }
        return picked;
    }
}
