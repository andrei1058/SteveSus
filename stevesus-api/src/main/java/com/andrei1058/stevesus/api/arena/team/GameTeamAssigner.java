package com.andrei1058.stevesus.api.arena.team;

import com.andrei1058.stevesus.api.arena.Arena;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;

public class GameTeamAssigner {

    private final Arena arena;

    public GameTeamAssigner(Arena arena) {
        this.arena = arena;
    }

    public void assignTeams() {
        //todo add changes in the future. player chance etc

        for (Player player : arena.getPlayers()) {
            boolean teamFound = false;
            ArrayList<Team> teams = new ArrayList<>(arena.getGameTeams());
            teams.sort(Comparator.comparingInt(team -> team.getMembers().size()));
            for (Team team : teams) {
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
