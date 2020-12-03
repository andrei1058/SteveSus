package com.andrei1058.stevesus.api.arena;

import com.andrei1058.stevesus.api.SteveSusAPI;
import com.andrei1058.stevesus.api.arena.task.GameTask;
import com.andrei1058.stevesus.api.arena.team.PlayerColorAssigner;
import com.andrei1058.stevesus.api.arena.team.Team;
import com.andrei1058.stevesus.api.event.GameFinishEvent;
import com.andrei1058.stevesus.api.locale.ChatUtil;
import com.andrei1058.stevesus.api.locale.Locale;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.api.server.GameSound;
import com.andrei1058.stevesus.common.api.arena.GameState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class GameEndConditions {

    public GameEndConditions() {
    }

    /**
     * Check if it is the case to assign a winner and or restart arena.
     * <p>
     * This method will handle everything. Sending messages etc.
     * Works with IN_GAME status only.
     */
    public void tickGameEndConditions(Arena arena) {
        if (arena.getGameState() != GameState.IN_GAME) return;

        // if no players left
        if (arena.getPlayers().isEmpty()) {
            arena.switchState(GameState.ENDING);

            Bukkit.getPluginManager().callEvent(new GameFinishEvent(arena, new LinkedList<>()));
            if (arena.getWorld().getPlayers().isEmpty()) {
                arena.setCountdown(3);
            }
        } else {
            Team impostors = arena.getTeamByName("impostor");
            Team crew = arena.getTeamByName("crew");
            if (impostors == null) {
                throw new IllegalStateException("Could not find impostor team! If you are using custom teams make sure to register your own game end conditions.");
            }
            if (crew == null) {
                throw new IllegalStateException("Could not find impostor team! If you are using custom teams make sure to register your own game end conditions.");
            }

            if (impostors.getMembers().isEmpty()) {
                // crew won
                crewWins(arena, Message.WIN_REASON_IMPOSTORS_EXCLUDED.toString());
            } else if (crew.getMembers().isEmpty()) {
                // impostors won
                impostorsWin(arena, Message.DEFEAT_REASON_NO_MORE_INNOCENTS.toString());
            } else if (impostors.getMembers().size() == crew.getMembers().size()) {
                // impostors won
                impostorsWin(arena, Message.DEFEAT_REASON_ALL_KILLED.toString());
            } else {
                // check tasks
                int assignedTasks = 0;
                int finishedTasks = 0;
                for (GameTask task : arena.getLoadedGameTasks()) {
                    assignedTasks += task.getAssignedPlayers().size();
                    finishedTasks += task.getAssignedPlayers().stream().filter(player -> task.getCurrentStage(player) == task.getTotalStages(player)).count();
                }
                if (assignedTasks == finishedTasks) {
                    // crew wins
                    crewWins(arena, Message.WIN_REASON_TASKS_COMPLETED.toString());
                }
            }
        }
    }

    private static void crewWins(Arena arena, String reasonPath) {
        arena.switchState(GameState.ENDING);
        // this list is passed to the game end/ win event
        LinkedList<Team> winnerTeams = new LinkedList<>();

        // build winners list string per language
        HashMap<Locale, String> winnerNamesPerLanguage = new HashMap<>();
        List<Locale> filteredLanguages = new LinkedList<>();
        for (Player player : arena.getPlayers()) {
            Locale playerLang = SteveSusAPI.getInstance().getLocaleHandler().getLocale(player);
            if (!filteredLanguages.contains(playerLang)) {
                filteredLanguages.add(playerLang);
            }
        }
        for (Player player : arena.getSpectators()) {
            Locale playerLang = SteveSusAPI.getInstance().getLocaleHandler().getLocale(player);
            if (!filteredLanguages.contains(playerLang)) {
                filteredLanguages.add(playerLang);
            }
        }
        for (Locale locale : filteredLanguages) {
            String separator = locale.getMsg(null, Message.GAME_END_CREW_WON_NAME_SEPARATOR);
            StringBuilder stringBuilder = new StringBuilder();
            for (Team team : arena.getGameTeams()) {
                if (team.isInnocent()) {
                    winnerTeams.add(team);
                    for (Player member : team.getMembers()) {
                        String displayColor = "";
                        if (arena.getPlayerColorAssigner() != null) {
                            PlayerColorAssigner.PlayerColor playerColor = arena.getPlayerColorAssigner().getPlayerColor(member);
                            if (playerColor != null) {
                                displayColor = playerColor.getDisplayColor(member);
                            }
                        }
                        String formattedString = locale.getMsg(null, Message.GAME_END_CREW_WON_NAME_FORMAT).replace("{display_name}", member.getDisplayName())
                                .replace("{name}", member.getName()).replace("{display_color}", displayColor);
                        if (formattedString.contains("{tasks_done}")) {
                            formattedString = formattedString.replace("{tasks_done}", String.valueOf(arena.getLoadedGameTasks().stream().filter(task -> task.getCurrentStage(member) == task.getTotalStages(member)).count()));
                        }
                        if (formattedString.contains("{tasks_total}")) {
                            formattedString = formattedString.replace("{tasks_total}", String.valueOf(arena.getLoadedGameTasks().stream().filter(task -> task.hasTask(member)).count()));
                        }
                        stringBuilder.append(formattedString);
                        stringBuilder.append(separator);
                    }
                }
            }
            String result = stringBuilder.toString();

            if (result.endsWith(separator)) {
                result = result.substring(0, result.length() - separator.length());
            }
            result += locale.getMsg(null, Message.GAME_END_CREW_WON_NAME_DOT);
            winnerNamesPerLanguage.put(locale, result);
        }

        for (Player player : arena.getPlayers()) {
            Locale lang = SteveSusAPI.getInstance().getLocaleHandler().getLocale(player);
            for (String string : lang.getMsgList(player, Message.GAME_END_CREW_WON_CHAT)) {
                if (string.contains("{reason}")) {
                    if (reasonPath != null) {
                        string = lang.getMsg(player, reasonPath);
                        player.sendMessage(ChatUtil.centerMessage(string));
                    }
                } else if (string.contains("{names}")) {
                    for (String msg : string.replace("{names}", winnerNamesPerLanguage.get(lang)).split("\\\\n")) {
                        player.sendMessage(ChatUtil.centerMessage(msg));
                    }
                } else {
                    player.sendMessage(ChatUtil.centerMessage(string));
                }
            }
            player.sendTitle(lang.getMsg(player, Message.GAME_END_CREW_WON_TITLE), lang.getMsg(player, Message.GAME_END_CREW_WON_SUBTITLE), 10, 60, 10);
        }
        for (Player player : arena.getSpectators()) {
            Locale lang = SteveSusAPI.getInstance().getLocaleHandler().getLocale(player);
            for (String string : lang.getMsgList(player, Message.GAME_END_CREW_WON_CHAT)) {
                if (string.contains("{reason}")) {
                    if (reasonPath != null) {
                        string = lang.getMsg(player, reasonPath);
                        player.sendMessage(ChatUtil.centerMessage(string));
                    }
                } else if (string.contains("{names}")) {
                    for (String msg : string.replace("{names}", winnerNamesPerLanguage.get(lang)).split("\\\\n")) {
                        player.sendMessage(ChatUtil.centerMessage(msg));
                    }
                } else {
                    player.sendMessage(ChatUtil.centerMessage(string));
                }
            }
            player.sendTitle(lang.getMsg(player, Message.GAME_END_CREW_WON_TITLE), lang.getMsg(player, Message.GAME_END_CREW_WON_SUBTITLE), 10, 60, 10);
        }
        GameSound.INNOCENTS_WIN.playToPlayers(arena.getPlayers());
        GameSound.INNOCENTS_WIN.playToPlayers(arena.getSpectators());
        GameFinishEvent event = new GameFinishEvent(arena, winnerTeams);
        Bukkit.getPluginManager().callEvent(event);
    }

    private static void impostorsWin(Arena arena, String reasonPath) {
        arena.switchState(GameState.ENDING);

        // this list is passed to the game end/ win event
        LinkedList<Team> winnerTeams = new LinkedList<>();

        // build winners list string per language
        HashMap<Locale, String> winnerNamesPerLanguage = new HashMap<>();
        List<Locale> filteredLanguages = new LinkedList<>();
        for (Player player : arena.getPlayers()) {
            Locale playerLang = SteveSusAPI.getInstance().getLocaleHandler().getLocale(player);
            if (!filteredLanguages.contains(playerLang)) {
                filteredLanguages.add(playerLang);
            }
        }
        for (Player player : arena.getSpectators()) {
            Locale playerLang = SteveSusAPI.getInstance().getLocaleHandler().getLocale(player);
            if (!filteredLanguages.contains(playerLang)) {
                filteredLanguages.add(playerLang);
            }
        }
        for (Locale locale : filteredLanguages) {
            String separator = locale.getMsg(null, Message.GAME_END_IMPOSTORS_WON_NAME_SEPARATOR);
            StringBuilder stringBuilder = new StringBuilder();
            for (Team team : arena.getGameTeams()) {
                if (!team.isInnocent()) {
                    winnerTeams.add(team);
                    for (Player member : team.getMembers()) {
                        String displayColor = "";
                        if (arena.getPlayerColorAssigner() != null) {
                            PlayerColorAssigner.PlayerColor playerColor = arena.getPlayerColorAssigner().getPlayerColor(member);
                            if (playerColor != null) {
                                displayColor = playerColor.getDisplayColor(member);
                            }
                        }
                        String formattedString = locale.getMsg(null, Message.GAME_END_IMPOSTORS_WON_NAME_FORMAT).replace("{display_name}", member.getDisplayName())
                                .replace("{name}", member.getName()).replace("{display_color}", displayColor);
                        stringBuilder.append(formattedString);
                        stringBuilder.append(separator);
                    }
                }
            }
            String result = stringBuilder.toString();

            if (result.endsWith(separator)) {
                result = result.substring(0, result.length() - separator.length());
            }
            result += locale.getMsg(null, Message.GAME_END_IMPOSTORS_WON_NAME_DOT);
            winnerNamesPerLanguage.put(locale, result);
        }

        for (Player player : arena.getPlayers()) {
            Locale lang = SteveSusAPI.getInstance().getLocaleHandler().getLocale(player);
            for (String string : lang.getMsgList(player, Message.GAME_END_IMPOSTORS_WON_CHAT)) {
                if (string.contains("{reason}")) {
                    if (reasonPath != null) {
                        string = lang.getMsg(player, reasonPath);
                        player.sendMessage(ChatUtil.centerMessage(string));
                    }
                } else if (string.contains("{names}")) {
                    for (String msg : string.replace("{names}", winnerNamesPerLanguage.get(lang)).split("\\\\n")) {
                        player.sendMessage(ChatUtil.centerMessage(msg));
                    }
                } else {
                    player.sendMessage(ChatUtil.centerMessage(string));
                }
            }
            player.sendTitle(lang.getMsg(player, Message.GAME_END_IMPOSTORS_WON_TITLE), lang.getMsg(player, Message.GAME_END_IMPOSTORS_WON_SUBTITLE), 10, 60, 10);
        }
        for (Player player : arena.getSpectators()) {
            Locale lang = SteveSusAPI.getInstance().getLocaleHandler().getLocale(player);
            for (String string : lang.getMsgList(player, Message.GAME_END_IMPOSTORS_WON_CHAT)) {
                if (string.contains("{reason}")) {
                    if (reasonPath != null) {
                        string = lang.getMsg(player, reasonPath);
                        player.sendMessage(ChatUtil.centerMessage(string));
                    }
                } else if (string.contains("{names}")) {
                    for (String msg : string.replace("{names}", winnerNamesPerLanguage.get(lang)).split("\\\\n")) {
                        player.sendMessage(ChatUtil.centerMessage(msg));
                    }
                } else {
                    player.sendMessage(ChatUtil.centerMessage(string));
                }
            }
            player.sendTitle(lang.getMsg(player, Message.GAME_END_IMPOSTORS_WON_TITLE), lang.getMsg(player, Message.GAME_END_IMPOSTORS_WON_SUBTITLE), 10, 60, 10);
        }
        GameSound.IMPOSTORS_WIN.playToPlayers(arena.getPlayers());
        GameSound.IMPOSTORS_WIN.playToPlayers(arena.getSpectators());
        GameFinishEvent event = new GameFinishEvent(arena, winnerTeams);
        Bukkit.getPluginManager().callEvent(event);
    }
}
