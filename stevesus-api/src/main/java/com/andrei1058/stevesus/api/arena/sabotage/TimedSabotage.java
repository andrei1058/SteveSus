package com.andrei1058.stevesus.api.arena.sabotage;

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
