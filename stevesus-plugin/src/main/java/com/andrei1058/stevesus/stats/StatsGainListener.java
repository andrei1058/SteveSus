package com.andrei1058.stevesus.stats;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.team.Team;
import com.andrei1058.stevesus.api.event.*;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.arena.ArenaManager;
import com.andrei1058.stevesus.common.api.arena.GameState;
import com.andrei1058.stevesus.common.stats.PlayerStatsCache;
import com.andrei1058.stevesus.common.stats.StatsManager;
import com.andrei1058.stevesus.language.LanguageManager;
import com.andrei1058.stevesus.prevention.PreventionManager;
import com.andrei1058.stevesus.server.common.ServerQuitListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.Date;
import java.time.Instant;

public class StatsGainListener implements Listener {

    public StatsGainListener() {
        ServerQuitListener.registerInternalQuit(player -> StatsManager.getINSTANCE().clear(player.getUniqueId()));
    }

    // save stats on player leave
    @EventHandler
    public void onGameQuit(PlayerGameLeaveEvent e) {
        if (e.getArena().getGameState() == GameState.WAITING || e.getArena().getGameState() == GameState.STARTING) return;
        if (!e.isSpectator()) {
            PlayerStatsCache stats = StatsManager.getINSTANCE().getPlayerStats(e.getPlayer().getUniqueId());
            if (stats != null) {
                boolean isAbandon = e.isAbandon();
                if (isAbandon) {
                    stats.saveStats(e.isAbandon());
                    stats.setLastPlay(new Date(Instant.now().toEpochMilli()));
                    if (stats.getFirstPlay() == null && e.getArena().getStartTime() != null) {
                        stats.setFirstPlay(new Date(e.getArena().getStartTime().toEpochMilli()));
                    }
                    return;
                }
                if (PreventionManager.getInstance().canReceiveStats(e.getArena())) {

                    // skip if winner because their stats are saved from game end event
                    if (e.getArena().getStats().getWinners() != null) {
                        for (Team team : e.getArena().getGameTeams()) {
                            for (Player member : team.getMembers()) {
                                if (member.getUniqueId().equals(e.getPlayer().getUniqueId())) {
                                    return;
                                }
                            }
                        }
                    }
                    if (stats.getFirstPlay() == null && e.getArena().getStartTime() != null) {
                        stats.setFirstPlay(new Date(e.getArena().getStartTime().toEpochMilli()));
                    }

                    stats.setLastPlay(new Date(Instant.now().toEpochMilli()));
                    stats.setGamesLost(stats.getGamesLost() + 1);

                    stats.setKills(stats.getKills() + e.getArena().getStats().getKills(e.getPlayer().getUniqueId()));
                    stats.setSabotages(stats.getSabotages() + e.getArena().getStats().getSabotages(e.getPlayer().getUniqueId()));
                    stats.setTasks(stats.getSabotages() + e.getArena().getStats().getTasks(e.getPlayer().getUniqueId()));

                    stats.saveStats(false);
                } else {
                    e.getPlayer().sendMessage(LanguageManager.getINSTANCE().getMsg(e.getPlayer(), Message.PREVENTION_GAME_TOO_SHORT)
                            .replace("{map}", e.getArena().getDisplayName()));
                }
            }
        }
    }

    // save stats on eliminate
    @EventHandler
    public void onEliminate(PlayerToSpectatorEvent e) {
        PlayerStatsCache stats = StatsManager.getINSTANCE().getPlayerStats(e.getPlayer().getUniqueId());
        if (stats != null) {
            if (PreventionManager.getInstance().canReceiveStats(e.getArena())) {

                if (stats.getFirstPlay() == null && e.getArena().getStartTime() != null) {
                    stats.setFirstPlay(new Date(e.getArena().getStartTime().toEpochMilli()));
                }

                //is looser?
                stats.setGamesLost(stats.getGamesLost() + 1);

                stats.setKills(stats.getKills() + e.getArena().getStats().getKills(e.getPlayer().getUniqueId()));
                stats.setSabotages(stats.getSabotages() + e.getArena().getStats().getSabotages(e.getPlayer().getUniqueId()));
                stats.setTasks(stats.getSabotages() + e.getArena().getStats().getTasks(e.getPlayer().getUniqueId()));

                stats.setLastPlay(new Date(Instant.now().toEpochMilli()));
                stats.saveStats(false);
            } else {
                e.getPlayer().sendMessage(LanguageManager.getINSTANCE().getMsg(e.getPlayer(), Message.PREVENTION_GAME_TOO_SHORT)
                        .replace("{map}", e.getArena().getDisplayName()));
            }
        }
    }

    @EventHandler
    public void onGameEnd(GameFinishEvent e) {
        for (Team winnerTeam : e.getWinners()) {
            for (Player winner : winnerTeam.getMembers()) {
                if (winner != null) {
                    Arena winnerArena = ArenaManager.getINSTANCE().getArenaByPlayer(winner);
                    if (winnerArena != null && winnerArena.equals(e.getArena())) {
                        if (PreventionManager.getInstance().canReceiveStats(winnerArena)) {
                            PlayerStatsCache stats = StatsManager.getINSTANCE().getPlayerStats(winner.getUniqueId());
                            if (stats != null) {
                                if (stats.getFirstPlay() == null && e.getArena().getStartTime() != null) {
                                    stats.setFirstPlay(new Date(e.getArena().getStartTime().toEpochMilli()));
                                }
                                stats.setGamesWon(stats.getGamesWon() + 1);
                                stats.setLastPlay(new Date(Instant.now().toEpochMilli()));
                                stats.setKills(stats.getKills() + winnerArena.getStats().getKills(winner.getUniqueId()));
                                stats.setSabotages(stats.getSabotages() + winnerArena.getStats().getSabotages(winner.getUniqueId()));
                                stats.setTasks(stats.getSabotages() + winnerArena.getStats().getTasks(winner.getUniqueId()));
                                stats.saveStats(false);
                            }
                        } else {
                            winner.sendMessage(LanguageManager.getINSTANCE().getMsg(winner, Message.PREVENTION_GAME_TOO_SHORT)
                                    .replace("{map}", e.getArena().getDisplayName()));
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onKill(PlayerKillEvent event){
        event.getArena().getStats().addKill(event.getKiller().getUniqueId());
    }

    @EventHandler
    public void onSabotage(GameSabotageActivateEvent event){
        event.getArena().getStats().addSabotage(event.getTrigger().getUniqueId());
    }

    @EventHandler
    public void onSabotage(GameSabotageDeactivateEvent event){
        for (Player contributor : event.getPlayers()){
            event.getArena().getStats().addFixedSabotage(contributor.getUniqueId());
        }
    }

    @EventHandler
    public void onTaskDone(PlayerTaskDoneEvent event){
        event.getArena().getStats().addTask(event.getPlayer().getUniqueId());
    }
}
