package dev.andrei1058.game.api.arena;

import dev.andrei1058.game.api.arena.task.TaskMeterUpdatePolicy;
import dev.andrei1058.game.common.api.arena.GameState;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Live settings are pre-game settings, meaning you can customize your game using this.
 * You can update them during the game as well.
 */
@SuppressWarnings({"unused", "BooleanMethodIsAlwaysInverted"})
public class LiveSettings {

    private boolean visualTasksEnabled = true;
    private IntegerRange commonTasks = new IntegerRange(1, 1, 5);
    private IntegerRange shortTasks = new IntegerRange(1, 2, 10);
    private IntegerRange longTasks = new IntegerRange(1, 1, 5);
    private IntegerRange meetingsPerPlayer = new IntegerRange(-1, 2, 20);
    private IntegerRange talkingDuration = new IntegerRange(10, 45, 120);
    private IntegerRange votingDuration = new IntegerRange(10, 20, 60);
    private boolean anonymousVotes = false;
    private DoubleRange getKillDistance = new DoubleRange(1, 2.8, 4);
    private IntegerRange killCooldown = new IntegerRange(1, 45, 360);
    private boolean confirmEjects = true;
    private IntegerRange impostors = new IntegerRange(1, 1, 2);
    private IntegerRange emergencyCoolDown = new IntegerRange(5, 15, 120);
    private DoubleRange playerSpeed = new DoubleRange(0.5, 1.0, 2.0);
    private TaskMeterUpdatePolicy taskMeterUpdatePolicy = TaskMeterUpdatePolicy.ALWAYS;
    private boolean sprintAllowed = false;
    private IntegerRange sabotageCooldown = new IntegerRange(10, 45, 60);
    private GameArena gameArena;

    /**
     * This is triggered when live settings is initialized by arena.
     */
    public void init(GameArena gameArena) {
        this.gameArena = gameArena;
    }

    /**
     * Null if settings were not initialized yet by the arena.
     */
    @Nullable
    public GameArena getArena() {
        return gameArena;
    }

    /**
     * Get number of common tasks.
     */
    public IntegerRange getCommonTasks() {
        return commonTasks;
    }

    /**
     * Get amount of short tasks.
     */
    public IntegerRange getShortTasks() {
        return shortTasks;
    }

    /**
     * Get amount of long tasks.
     */
    public IntegerRange getLongTasks() {
        return longTasks;
    }

    /**
     * Get how many meetings can a player start.
     * <p>
     * Set meetings per player.
     * Can do that only if the game didn't start already.
     * If you want to give a player more meetings use {@link GameArena#setMeetingsLeft(Player, int)}.
     */
    public IntegerRange getMeetingsPerPlayer() {
        return meetingsPerPlayer;
    }

    public IntegerRange getTalkingDuration() {
        return talkingDuration;
    }

    public IntegerRange getVotingDuration() {
        return votingDuration;
    }

    public boolean isAnonymousVotes() {
        return anonymousVotes;
    }

    public void setAnonymousVotes(boolean anonymousVotes) {
        this.anonymousVotes = anonymousVotes;
    }

    public DoubleRange getKillDistance() {
        return getKillDistance;
    }

    public IntegerRange getKillCooldown() {
        return killCooldown;
    }

    /**
     * Check if visual tasks are enabled.
     */
    public boolean isVisualTasksEnabled() {
        return visualTasksEnabled;
    }

    /**
     * Set number of common tasks.
     */
    public void setCommonTasks(IntegerRange commonTasks) {
        this.commonTasks = commonTasks;
    }

    public boolean isConfirmEjects() {
        return confirmEjects;
    }

    public void setConfirmEjects(boolean confirmEjects) {
        this.confirmEjects = confirmEjects;
    }

    /**
     * Set number of tasks.
     */
    public void setLongTasks(IntegerRange longTasks) {
        this.longTasks = longTasks;
    }

    public void setMeetingsPerPlayer(IntegerRange meetingsPerPlayer) {
        this.meetingsPerPlayer = meetingsPerPlayer;
    }

    /**
     * Set number of tasks.
     * Changes allowed during waiting and starting phase.
     */
    public void setShortTasks(IntegerRange shortTasks) {
        this.shortTasks = shortTasks;
    }

    /**
     * Toggle visual tasks.
     */
    public void setVisualTasksEnabled(boolean visualTasksEnabled) {
        this.visualTasksEnabled = visualTasksEnabled;
    }

    public void setTalkingDuration(IntegerRange talkingDuration) {
        this.talkingDuration = talkingDuration;
    }

    public void setGetKillDistance(DoubleRange getKillDistance) {
        this.getKillDistance = getKillDistance;
    }

    public void setKillCooldown(IntegerRange killCooldown) {
        this.killCooldown = killCooldown;
    }

    public void setVotingDuration(IntegerRange votingDuration) {
        this.votingDuration = votingDuration;
    }

    public IntegerRange getImpostors() {
        return impostors;
    }

    public void setImpostors(IntegerRange impostors) {
        this.impostors = impostors;
    }

    public IntegerRange getEmergencyCoolDown() {
        return emergencyCoolDown;
    }

    public void setEmergencyCoolDown(IntegerRange emergencyCoolDown) {
        this.emergencyCoolDown = emergencyCoolDown;
    }

    public DoubleRange getPlayerSpeed() {
        return playerSpeed;
    }

    public void setPlayerSpeed(DoubleRange playerSpeed) {
        this.playerSpeed = playerSpeed;
    }

    public IntegerRange getSabotageCooldown() {
        return sabotageCooldown;
    }

    public void setSabotageCooldown(IntegerRange sabotageCooldown) {
        this.sabotageCooldown = sabotageCooldown;
    }

    /**
     * Get arena task meter policy.
     */
    public TaskMeterUpdatePolicy getTaskMeterUpdatePolicy() {
        return taskMeterUpdatePolicy;
    }

    /**
     * Change task meter update policy.
     */
    public void setTaskMeterUpdatePolicy(TaskMeterUpdatePolicy taskMeterUpdatePolicy) {
        if (getArena() != null) {
            if (this.taskMeterUpdatePolicy == TaskMeterUpdatePolicy.NEVER && taskMeterUpdatePolicy != TaskMeterUpdatePolicy.NEVER) {
                if (getArena().getGameState() == GameState.IN_GAME) {
                    getArena().refreshTaskMeter();
                }
            }
            if (this.taskMeterUpdatePolicy == TaskMeterUpdatePolicy.NEVER) {
                getArena().refreshTaskMeter();
            }
        }
        this.taskMeterUpdatePolicy = taskMeterUpdatePolicy;
    }

    public void setSprintAllowed(boolean sprintAllowed) {
        this.sprintAllowed = sprintAllowed;
    }

    public boolean isSprintAllowed() {
        return sprintAllowed;
    }

    public static class IntegerRange {
        private int minValue;
        private int currentValue;
        private int maxValue;

        public IntegerRange(int minValue, int defaultValue, int maxValue) {
            this.minValue = minValue;
            this.currentValue = defaultValue;
            this.maxValue = maxValue;
        }

        public int getCurrentValue() {
            return currentValue;
        }

        public void setCurrentValue(int currentValue) {
            if (minValue > currentValue || maxValue < currentValue) return;
            this.currentValue = currentValue;
        }

        public void setMinValue(int minValue) {
            this.minValue = minValue;
        }

        public void setMaxValue(int maxValue) {
            this.maxValue = maxValue;
        }

        public int getMinValue() {
            return minValue;
        }

        public int getMaxValue() {
            return maxValue;
        }
    }

    public static class DoubleRange {
        private double minValue;
        private double currentValue;
        private double maxValue;

        public DoubleRange(double minValue, double defaultValue, double maxValue) {
            this.minValue = minValue;
            this.currentValue = defaultValue;
            this.maxValue = maxValue;
        }

        public void setCurrentValue(double currentValue) {
            if (minValue > currentValue || maxValue < currentValue) return;
            this.currentValue = currentValue;
        }

        public double getCurrentValue() {
            return currentValue;
        }

        public void setMaxValue(double maxValue) {
            this.maxValue = maxValue;
        }

        public void setMinValue(double minValue) {
            this.minValue = minValue;
        }

        public double getMinValue() {
            return minValue;
        }

        public double getMaxValue() {
            return maxValue;
        }
    }

}
