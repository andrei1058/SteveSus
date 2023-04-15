package dev.andrei1058.game.api.arena.sabotage;

public interface TimedSabotage {

    /**
     * Get countdown if active.
     */
    int getCountDown();

    /**
     * Ticked every second.
     */
    void doTick();
}
