package dev.andrei1058.game.api;

import dev.andrei1058.game.api.arena.team.Team;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
public class GameStats {

    private final HashMap<UUID, Integer> kills = new HashMap<>();
    private final HashMap<UUID, Integer> sabotages = new HashMap<>();
    private final HashMap<UUID, Integer> fixedSabotages = new HashMap<>();
    private final HashMap<UUID, Integer> tasks = new HashMap<>();
    private List<Team> winners = null;

    public int getKills(UUID player) {
        return kills.getOrDefault(player, 0);
    }

    /**
     * Increment by 1.
     */
    public void addKill(UUID player) {
        if (kills.containsKey(player)) {
            kills.replace(player, kills.get(player) + 1);
        } else {
            kills.put(player, 1);
        }

    }

    public int getSabotages(UUID player) {
        return sabotages.getOrDefault(player, 0);
    }

    /**
     * Increment by 1.
     */
    public void addSabotage(UUID player) {
        if (sabotages.containsKey(player)) {
            sabotages.replace(player, sabotages.get(player) + 1);
        } else {
            sabotages.put(player, 1);
        }
    }

    public int getTasks(UUID player) {
        return tasks.getOrDefault(player, 0);
    }

    /**
     * Increment by 1.
     */
    public void addTask(UUID player) {
        if (tasks.containsKey(player)) {
            tasks.replace(player, tasks.get(player) + 1);
        } else {
            tasks.put(player, 1);
        }
    }

    public int getFixedSabotages(UUID player) {
        return fixedSabotages.getOrDefault(player, 0);
    }

    /**
     * Increment by 1.
     */
    public void addFixedSabotage(UUID player) {
        if (fixedSabotages.containsKey(player)) {
            fixedSabotages.replace(player, fixedSabotages.get(player) + 1);
        } else {
            fixedSabotages.put(player, 1);
        }
    }

    /**
     * Get winner teams. Available only after game end.
     */
    public @Nullable List<Team> getWinners() {
        return winners;
    }

    public void setWinners(@Nullable List<Team> winners) {
        this.winners = winners;
    }
}
