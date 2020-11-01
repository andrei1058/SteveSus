package com.andrei1058.amongusmc.prevention.abandon.condition;

import com.andrei1058.amongusmc.api.arena.Arena;
import com.andrei1058.amongusmc.api.prevention.abandon.AbandonCondition;
import org.bukkit.entity.Player;

import java.time.Instant;

public class PlayTimeCondition implements AbandonCondition {

    private int seconds;

    @Override
    public String getIdentifier() {
        return "played_less_than";
    }

    @Override
    public boolean getOutcome(Player player, Arena arena) {
        Instant minTime = arena.getStartTime().plusSeconds(seconds);
        return Instant.now().isBefore(minTime);
    }

    @Override
    public boolean onLoad(String configData) {
        if (configData.trim().isEmpty()) return false;
        try {
            seconds = Integer.parseInt(configData);
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }
}
