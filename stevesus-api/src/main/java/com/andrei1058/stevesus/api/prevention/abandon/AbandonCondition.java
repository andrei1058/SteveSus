package com.andrei1058.stevesus.api.prevention.abandon;

import com.andrei1058.stevesus.api.arena.Arena;
import org.bukkit.entity.Player;

public interface AbandonCondition {

    String IDENTIFIER_REGEX = "^[a-zA-Z0-9_]+$";

    /**
     * Get condition identifier.
     * Used to parse condition data from config.
     */
    String getIdentifier();

    /**
     * Process and give result.
     *
     * @return true if abandon condition is satisfied.
     */
    boolean getOutcome(Player player, Arena arena);

    /**
     * Called when fetching data from config file.
     *
     * @return true if enabled successfully.
     */
    boolean onLoad(String configData);
}
