package dev.andrei1058.game.prevention.abandon.condition;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.prevention.abandon.AbandonCondition;
import org.bukkit.entity.Player;

import java.time.Instant;

public class PlayTimeCondition implements AbandonCondition {

    private int seconds;

    @Override
    public String getIdentifier() {
        return "played_less_than";
    }

    @Override
    public boolean getOutcome(Player player, GameArena gameArena) {
        if (gameArena == null) return false;
        if (gameArena.getStartTime() == null) return false;
        Instant minTime = gameArena.getStartTime().plusSeconds(seconds);
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
