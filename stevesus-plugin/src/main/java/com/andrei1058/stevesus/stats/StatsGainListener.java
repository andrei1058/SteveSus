package com.andrei1058.stevesus.stats;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.team.Team;
import com.andrei1058.stevesus.api.event.GameFinishEvent;
import com.andrei1058.stevesus.api.event.PlayerGameLeaveEvent;
import com.andrei1058.stevesus.api.event.PlayerToSpectatorEvent;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.arena.ArenaManager;
import com.andrei1058.stevesus.common.stats.PlayerStatsCache;
import com.andrei1058.stevesus.common.stats.StatsManager;
import com.andrei1058.stevesus.language.LanguageManager;
import com.andrei1058.stevesus.prevention.PreventionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.Date;
import java.time.Instant;

public class StatsGainListener implements Listener {

    // save stats on player leave
    @EventHandler
    public void onGameQuit(PlayerGameLeaveEvent e) {
        if (!e.isSpectator()) {
            PlayerStatsCache stats = StatsManager.getINSTANCE().getPlayerStats(e.getPlayer().getUniqueId());
            if (stats != null) {
                boolean isAbandon = e.isAbandon();
                if (isAbandon) {
                    stats.saveStats(e.isAbandon());
                    return;
                }
                if (PreventionManager.getInstance().canReceiveWin(e.getArena())) {

                    //todo has rejoin system?
                    stats.setGamesPlayed(stats.getGamesPlayed() + 1);
                    //todo is looser?
                    stats.setGamesLost(stats.getGamesLost() + 1);

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
            if (PreventionManager.getInstance().canReceiveWin(e.getArena())) {
                stats.setGamesPlayed(stats.getGamesPlayed() + 1);

                //todo is looser?
                stats.setGamesLost(stats.getGamesLost() + 1);

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
                        if (PreventionManager.getInstance().canReceiveWin(winnerArena)) {
                            PlayerStatsCache stats = StatsManager.getINSTANCE().getPlayerStats(winner.getUniqueId());
                            if (stats != null) {
                                stats.setGamesPlayed(stats.getGamesPlayed() + 1);
                                stats.setGamesWon(stats.getGamesWon() + 1);
                                stats.setLastPlay(new Date(Instant.now().toEpochMilli()));
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
}
