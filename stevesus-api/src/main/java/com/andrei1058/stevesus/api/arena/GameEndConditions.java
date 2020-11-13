package com.andrei1058.stevesus.api.arena;

import com.andrei1058.stevesus.api.SteveSusAPI;
import com.andrei1058.stevesus.api.arena.team.Team;
import com.andrei1058.stevesus.api.event.GameFinishEvent;
import com.andrei1058.stevesus.api.locale.ChatUtil;
import com.andrei1058.stevesus.api.locale.Locale;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.api.server.GameSound;
import com.andrei1058.stevesus.common.api.arena.GameState;
import org.bukkit.Bukkit;
import org.bukkit.Particle;

import java.util.LinkedList;
import java.util.UUID;

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

            LinkedList<UUID> winners = new LinkedList<>();
            Bukkit.getPluginManager().callEvent(new GameFinishEvent(arena, winners));
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
                arena.switchState(GameState.ENDING);
                arena.getPlayers().forEach(player -> {
                    Locale lang = SteveSusAPI.getInstance().getLocaleHandler().getLocale(player);
                    lang.getMsgList(player, Message.GAME_END_CREW_WON_CHAT).forEach(string -> player.sendMessage(ChatUtil.centerMessage(string)));
                    player.sendTitle(lang.getMsg(player, Message.GAME_END_CREW_WON_TITLE), lang.getMsg(player, Message.GAME_END_CREW_WON_SUBTITLE), 10, 60, 10);
                });
                arena.getSpectators().forEach(player -> {
                    Locale lang = SteveSusAPI.getInstance().getLocaleHandler().getLocale(player);
                    lang.getMsgList(player, Message.GAME_END_CREW_WON_CHAT).forEach(string -> player.sendMessage(ChatUtil.centerMessage(string)));
                    player.sendTitle(lang.getMsg(player, Message.GAME_END_CREW_WON_TITLE), lang.getMsg(player, Message.GAME_END_CREW_WON_SUBTITLE), 10, 60, 10);
                });
                GameSound.INNOCENTS_WIN.playToPlayers(arena.getPlayers());
                GameSound.INNOCENTS_WIN.playToPlayers(arena.getSpectators());
            } else if (crew.getMembers().isEmpty()) {
                // impostors won
                arena.switchState(GameState.ENDING);
                arena.getPlayers().forEach(player -> {
                    Locale lang = SteveSusAPI.getInstance().getLocaleHandler().getLocale(player);
                    lang.getMsgList(player, Message.GAME_END_IMPOSTORS_WON_CHAT).forEach(string -> player.sendMessage(ChatUtil.centerMessage(string)));
                    player.sendTitle(lang.getMsg(player, Message.GAME_END_IMPOSTORS_WON_TITLE), lang.getMsg(player, Message.GAME_END_IMPOSTORS_WON_SUBTITLE), 10, 60, 10);
                });
                arena.getSpectators().forEach(player -> {
                    Locale lang = SteveSusAPI.getInstance().getLocaleHandler().getLocale(player);
                    lang.getMsgList(player, Message.GAME_END_IMPOSTORS_WON_CHAT).forEach(string -> player.sendMessage(ChatUtil.centerMessage(string)));
                    player.sendTitle(lang.getMsg(player, Message.GAME_END_IMPOSTORS_WON_TITLE), lang.getMsg(player, Message.GAME_END_IMPOSTORS_WON_SUBTITLE), 10, 60, 10);
                });
                GameSound.IMPOSTORS_WIN.playToPlayers(arena.getPlayers());
                GameSound.IMPOSTORS_WIN.playToPlayers(arena.getSpectators());
            } else if (impostors.getMembers().size() == crew.getMembers().size()) {
                // impostors won
                arena.switchState(GameState.ENDING);
                arena.getPlayers().forEach(player -> {
                    Locale lang = SteveSusAPI.getInstance().getLocaleHandler().getLocale(player);
                    lang.getMsgList(player, Message.GAME_END_IMPOSTORS_WON_CHAT).forEach(string -> player.sendMessage(ChatUtil.centerMessage(string)));
                    player.sendTitle(lang.getMsg(player, Message.GAME_END_IMPOSTORS_WON_TITLE), lang.getMsg(player, Message.GAME_END_IMPOSTORS_WON_SUBTITLE), 10, 60, 10);
                });
                arena.getSpectators().forEach(player -> {
                    Locale lang = SteveSusAPI.getInstance().getLocaleHandler().getLocale(player);
                    lang.getMsgList(player, Message.GAME_END_IMPOSTORS_WON_CHAT).forEach(string -> player.sendMessage(ChatUtil.centerMessage(string)));
                    player.sendTitle(lang.getMsg(player, Message.GAME_END_IMPOSTORS_WON_TITLE), lang.getMsg(player, Message.GAME_END_IMPOSTORS_WON_SUBTITLE), 10, 60, 10);
                });
                GameSound.IMPOSTORS_WIN.playToPlayers(arena.getPlayers());
                GameSound.IMPOSTORS_WIN.playToPlayers(arena.getSpectators());
            }
        }
    }
}
