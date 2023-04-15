package dev.andrei1058.game.api.prevention;

import dev.andrei1058.game.api.prevention.abandon.AbandonCondition;
import dev.andrei1058.game.api.prevention.abandon.TriggerType;

@SuppressWarnings("unused")
public interface PreventionHandler {

    /**
     * Check if abandon system is enabled.
     */
    boolean isAbandonSystemEnabled();

    /**
     * Fetch current game-abandon trigger type.
     */
    TriggerType getCurrentAbandonTrigger();

    /**
     * Register an abandon condition.
     * Your condition will only be triggered if it is used in the yml configuration.
     * Using this method will re-load conditions from config.
     *
     * @param abandonCondition custom abandon condition.
     * @return false if there is another condition registered with the same identifier or invalid identifier regex.
     */
    boolean registerAbandonCondition(AbandonCondition abandonCondition);

    /**
     * Check if anti-farming system is enabled.
     */
    boolean isAntiFarmingEnabled();

    /**
     * How long must a user play in order to get stats.
     */
    int getMinPlayTime();
}
