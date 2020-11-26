package com.andrei1058.stevesus.api.arena.task;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.team.Team;
import com.andrei1058.stevesus.common.api.arena.GameState;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class GameTaskAssigner {

    private final Arena arena;
    private final LinkedList<GameTask> commonTasks = new LinkedList<>();

    public GameTaskAssigner(Arena arena) {
        this.arena = arena;
    }

    /**
     * Assign tasks to team members if this team can have tasks.
     */
    public void assignTasks(Team team) {
        if (!team.canHaveTasks()) return;
        team.getMembers().forEach(this::assignTasks);
    }

    private void assignTasks(Player player) {
        if (arena.getGameState() != GameState.IN_GAME) return;

        Random random = new Random();

        // assign common tasks
        if (commonTasks.isEmpty()) {
            // eventually chose common tasks
            if (arena.getLiveSettings().getCommonTasks().getCurrentValue() != 0) {
                List<GameTask> tasks = arena.getLoadedGameTasks().stream()
                        .distinct() // prevent overflow in do while
                        .filter(task -> task.getHandler().getTaskType() == TaskType.COMMON).collect(Collectors.toList());
                int maxEntry = Math.min(arena.getLiveSettings().getCommonTasks().getCurrentValue(), tasks.size());
                for (int i = 0; i < maxEntry; i++) {
                    int entry = tasks.size() == 1 ? 0 : random.nextInt(tasks.size() - 1);
                    GameTask chosenTask = tasks.remove(entry);
                    commonTasks.add(chosenTask);
                    chosenTask.assignToPlayer(player, arena);
                }
            }
        } else {
            commonTasks.forEach(task -> task.assignToPlayer(player, arena));
        }

        // assign short tasks
        if (arena.getLiveSettings().getShortTasks().getCurrentValue() != 0) {
            List<GameTask> tasks = arena.getLoadedGameTasks().stream()
                    .distinct() // prevent overflow in do while
                    .filter(task -> task.getHandler().getTaskType() == TaskType.SHORT).collect(Collectors.toList());
            int maxEntry = Math.min(arena.getLiveSettings().getShortTasks().getCurrentValue(), tasks.size());
            for (int i = 0; i < maxEntry; i++) {
                int entry = tasks.size() == 1 ? 0 : random.nextInt(tasks.size() - 1);
                GameTask chosenTask = tasks.remove(entry);
                chosenTask.assignToPlayer(player, arena);
            }
        }

        // assign long tasks
        if (arena.getLiveSettings().getLongTasks().getCurrentValue() != 0) {
            List<GameTask> tasks = arena.getLoadedGameTasks().stream()
                    .distinct() // prevent overflow in do while
                    .filter(task -> task.getHandler().getTaskType() == TaskType.LONG).collect(Collectors.toList());
            int maxEntry = Math.min(arena.getLiveSettings().getLongTasks().getCurrentValue(), tasks.size());
            for (int i = 0; i < maxEntry; i++) {
                int entry = tasks.size() == 1 ? 0 : random.nextInt(tasks.size() - 1);
                GameTask chosenTask = tasks.remove(entry);
                chosenTask.assignToPlayer(player, arena);
            }
        }
    }
}
