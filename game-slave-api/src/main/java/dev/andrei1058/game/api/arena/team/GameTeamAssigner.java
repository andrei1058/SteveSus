package dev.andrei1058.game.api.arena.team;

import dev.andrei1058.game.api.SteveSusAPI;
import dev.andrei1058.game.api.arena.Arena;
import dev.andrei1058.game.api.locale.ChatUtil;
import dev.andrei1058.game.api.locale.Locale;
import dev.andrei1058.game.api.locale.Message;
import dev.andrei1058.game.api.server.GameSound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;

public class GameTeamAssigner {

    public void assignTeams(Arena arena) {
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
