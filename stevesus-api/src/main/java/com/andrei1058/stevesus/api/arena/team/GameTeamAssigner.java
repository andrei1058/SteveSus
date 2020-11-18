package com.andrei1058.stevesus.api.arena.team;

import com.andrei1058.stevesus.api.SteveSusAPI;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.locale.ChatUtil;
import com.andrei1058.stevesus.api.locale.Locale;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.api.server.GameSound;
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

        Team crewMates = arena.getTeamByName("crew");
        Team impostors = arena.getTeamByName("impostor");
        if (crewMates != null && impostors != null) {
            for (Player crew : crewMates.getMembers()) {
                Locale lang = SteveSusAPI.getInstance().getLocaleHandler().getLocale(crew);
                crew.sendTitle(lang.getMsg(crew, Message.GAME_START_CREW_TITLE), lang.getMsg(crew, Message.GAME_START_CREW_SUBTITLE), 10, 30, 10);
                for (String string : lang.getMsgList(crew, Message.GAME_START_CHAT_CREWMATES)) {
                    if (string.contains("{format_impostor}")) {
                        string = string.replace("{format_impostor}", lang.getMsg(crew, impostors.getMembers().size() > 1 ? Message.GAME_START_CHAT_FORMAT_IMPOSTORS : Message.GAME_START_CHAT_FORMAT_IMPOSTOR));
                    }
                    string = ChatUtil.centerMessage(string);
                    crew.sendMessage(string);
                }
            }
            GameSound.GAME_START_CREW.playToPlayers(crewMates.getMembers());
        }
    }
}
