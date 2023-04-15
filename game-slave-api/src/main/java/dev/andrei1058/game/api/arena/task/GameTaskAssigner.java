package dev.andrei1058.game.api.arena.task;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.arena.team.Team;
import dev.andrei1058.game.common.api.arena.GameState;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class GameTaskAssigner {

    private final GameArena gameArena;
    private final LinkedList<GameTask> commonTasks = new LinkedList<>();

    public GameTaskAssigner(GameArena gameArena) {
        this.gameArena = gameArena;
    }

    /**
     * Assign tasks to team members if this team can have tasks.
     */
    public void assignTasks(Team team) {
        if (!team.canHaveTasks()) return;
        team.getMembers().forEach(this::assignTasks);
    }

    private void assignTasks(Player player) {
        if (gameArena.getGameState() != GameState.IN_GAME) return;

        Random random = new Random();

        // assign common tasks
        if (commonTasks.isEmpty()) {
            // eventually chose common tasks
            if (gameArena.getLiveSettings().getCommonTasks().getCurrentValue() != 0) {
                List<GameTask> tasks = gameArena.getLoadedGameTasks().stream()
                        .distinct() // prevent overflow in do while
                        .filter(task -> task.getHandler().getTaskType() == TaskType.COMMON).collect(Collectors.toList());
                int maxEntry = Math.min(gameArena.getLiveSettings().getCommonTasks().getCurrentValue(), tasks.size());
                for (int i = 0; i < maxEntry; i++) {
                    int entry = tasks.size() == 1 ? 0 : random.nextInt(tasks.size());
                    GameTask chosenTask = tasks.remove(entry);
                    commonTasks.add(chosenTask);
                    chosenTask.assignToPlayer(player, gameArena);
                }
            }
        } else {
            commonTasks.forEach(task -> task.assignToPlayer(player, gameArena));
        }

        // assign short tasks
        if (gameArena.getLiveSettings().getShortTasks().getCurrentValue() != 0) {
            List<GameTask> tasks = gameArena.getLoadedGameTasks().stream()
                    .distinct() // prevent overflow in do while
                    .filter(task -> task.getHandler().getTaskType() == TaskType.SHORT).collect(Collectors.toList());
            int maxEntry = Math.min(gameArena.getLiveSettings().getShortTasks().getCurrentValue(), tasks.size());
            for (int i = 0; i < maxEntry; i++) {
                int entry = tasks.size() == 1 ? 0 : random.nextInt(tasks.size());
                GameTask chosenTask = tasks.remove(entry);
                chosenTask.assignToPlayer(player, gameArena);
            }
        }

        // assign long tasks
        if (gameArena.getLiveSettings().getLongTasks().getCurrentValue() != 0) {
            List<GameTask> tasks = gameArena.getLoadedGameTasks().stream()
                    .distinct() // prevent overflow in do while
                    .filter(task -> task.getHandler().getTaskType() == TaskType.LONG).collect(Collectors.toList());
            int maxEntry = Math.min(gameArena.getLiveSettings().getLongTasks().getCurrentValue(), tasks.size());
            for (int i = 0; i < maxEntry; i++) {
                int entry = tasks.size() == 1 ? 0 : random.nextInt(tasks.size());
                GameTask chosenTask = tasks.remove(entry);
                chosenTask.assignToPlayer(player, gameArena);
            }
        }
    }
}
