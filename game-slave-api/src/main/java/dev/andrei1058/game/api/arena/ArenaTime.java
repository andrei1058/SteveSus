package dev.andrei1058.game.api.arena;

import org.jetbrains.annotations.Nullable;

/**
 * Gameplay time.
 */
public enum ArenaTime {

    DAY(2000, 9000),
    NIGHT(12786, 23216);

    private final int startTick;
    private final int endTick;

    /**
     * Game time.
     *
     * @param endTick   start range.
     * @param startTick end range.
     */
    ArenaTime(int startTick, int endTick) {
        this.startTick = startTick;
        this.endTick = endTick;
    }

    /**
     * Check if given time is in range.
     */
    public boolean isInRange(long currentTime) {
        return currentTime >= startTick && currentTime < endTick;
    }

    /**
     * Get start tick.
     * Used to change map time when required.
     */
    public int getStartTick() {
        return startTick;
    }

    /**
     * Get time of the day by name.
     */
    @Nullable
    public static ArenaTime getByName(String name) {
        switch (name.toLowerCase()){
            case "day":
            case "d":
            case "noon":
                return DAY;
            case "night":
            case "n":
            case "midnight":
                return NIGHT;
        }
        return null;
    }
}
