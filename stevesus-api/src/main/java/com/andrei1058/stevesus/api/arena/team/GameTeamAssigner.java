package com.andrei1058.stevesus.api.arena.team;

import com.andrei1058.stevesus.api.arena.Arena;
import org.bukkit.entity.Player;

public class GameTeamAssigner {

    private final Arena arena;

    public GameTeamAssigner(Arena arena) {
        this.arena = arena;
    }

    public void assignTeams() {
        //todo add changes in the future. player chance etc
        for (Player player : arena.getPlayers()) {
            boolean teamFound = false;
            for (Team team : arena.getGameTeams()) {
                if (!teamFound) {
                    if (team.addPlayer(player, true)) {
                        teamFound = true;
                    }
                }
            }
            // should not reach this point
            if (!teamFound) {
                throw new IllegalStateException("Could not assign a team to " + player.getName() + "! Make sure not to allow an amount of players greater than team members limit.");
            }
        }
    }
}
