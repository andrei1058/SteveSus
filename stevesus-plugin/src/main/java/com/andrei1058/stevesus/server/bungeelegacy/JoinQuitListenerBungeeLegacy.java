package com.andrei1058.stevesus.server.bungeelegacy;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.common.api.arena.GameState;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.arena.ArenaHandler;
import com.andrei1058.stevesus.common.command.CommonCmdManager;
import com.andrei1058.stevesus.common.api.locale.CommonMessage;
import com.andrei1058.stevesus.common.api.locale.CommonLocale;
import com.andrei1058.stevesus.common.database.DatabaseManager;
import com.andrei1058.stevesus.language.LanguageManager;
import com.andrei1058.stevesus.server.common.JoinCommonListener;
import com.andrei1058.dbi.operator.EqualsOperator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.UUID;

public class JoinQuitListenerBungeeLegacy implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPreLoginForLanguage(AsyncPlayerPreLoginEvent e) {
        if (e.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            // Do nothing if login fails
            return;
        }
        final UUID p = e.getUniqueId();
        CommonLocale translation = DatabaseManager.getINSTANCE().getDatabase().select(LanguageManager.getINSTANCE().getLanguageTable().LANGUAGE, LanguageManager.getINSTANCE().getLanguageTable(), new EqualsOperator<>(LanguageManager.getINSTANCE().getLanguageTable().PRIMARY_KEY, p));
        SteveSus.newChain().sync(() -> LanguageManager.getINSTANCE().setPlayerLocale(p, LanguageManager.getINSTANCE().getLocale(translation.getIsoCode()), false)).execute();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLoginForLanguage(PlayerLoginEvent e) {
        if (e.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            LanguageManager.getINSTANCE().setPlayerLocale(e.getPlayer().getUniqueId(), null, false);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLogin(PlayerLoginEvent e) {
        final Player p = e.getPlayer();

        // Do not allow login if the arena wasn't loaded yet
        if (ArenaHandler.getINSTANCE().getArenas().isEmpty()) {
            if (!ArenaHandler.getINSTANCE().getEnableQueue().isEmpty()) {
                e.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, LanguageManager.getINSTANCE().getMsg(p, CommonMessage.ARENA_STATUS_ENDING_NAME));
                return;
            }
        }

        Arena arena = ArenaHandler.getINSTANCE().getArenas().get(0);
        if (arena != null) {

            // Player logic
            if (arena.getGameState() == GameState.WAITING || (arena.getGameState() == GameState.STARTING && arena.getCountdown() > 3)) {
                // If arena is full
                if (arena.isFull()) {
                    // Vip join feature
                    if (ArenaHandler.getINSTANCE().hasVipJoin(p)) {
                        boolean canJoin = false;
                        for (Player inGame : arena.getPlayers()) {
                            if (!ArenaHandler.getINSTANCE().hasVipJoin(inGame)) {
                                canJoin = true;
                                inGame.kickPlayer(LanguageManager.getINSTANCE().getMsg(inGame, Message.VIP_JOIN_KICKED));
                                break;
                            }
                        }
                        if (!canJoin) {
                            e.disallow(PlayerLoginEvent.Result.KICK_FULL, LanguageManager.getINSTANCE().getDefaultLocale().getMsg(e.getPlayer(), Message.VIP_JOIN_DENIED));
                        }
                    } else {
                        e.disallow(PlayerLoginEvent.Result.KICK_OTHER, LanguageManager.getINSTANCE().getMsg(e.getPlayer(), CommonMessage.ARENA_JOIN_DENIED_GAME_FULL));
                    }
                }
            } else if (arena.getGameState() == GameState.IN_GAME) {
                // Spectator logic
                if (!(!arena.getSpectatePermission().isEmpty() && p.hasPermission(arena.getSpectatePermission()))) {
                    e.disallow(PlayerLoginEvent.Result.KICK_OTHER, LanguageManager.getINSTANCE().getDefaultLocale().getMsg(e.getPlayer(), CommonMessage.ARENA_JOIN_DENIED_SPECTATOR));
                }
            } else {
                e.disallow(PlayerLoginEvent.Result.KICK_OTHER, LanguageManager.getINSTANCE().getMsg(p, CommonMessage.ARENA_STATUS_ENDING_NAME));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent e) {
        e.setJoinMessage("");
        final Player p = e.getPlayer();

        // Do not allow login if the arena wasn't loaded yet
        // I know this code is already in the login event but other plugins may allow login
        if (ArenaHandler.getINSTANCE().getArenas().isEmpty()) {
            if (!ArenaHandler.getINSTANCE().getEnableQueue().isEmpty()) {
                p.kickPlayer(LanguageManager.getINSTANCE().getMsg(p, CommonMessage.ARENA_STATUS_ENDING_NAME));
                return;
            }
        }

        JoinCommonListener.displayCustomerDetails(p);

        if (ArenaHandler.getINSTANCE().getArenas().isEmpty()) {
            // Show setup commands if there is no arena available
            if (CommonCmdManager.getINSTANCE().getMainCmd().hasPermission(p)) {
                p.performCommand(CommonCmdManager.getINSTANCE().getMainCmd().getName());
            }
        } else {
            Arena arena = ArenaHandler.getINSTANCE().getArenas().get(0);
            // Add player if the game is in waiting
            if (arena.getGameState() == GameState.WAITING || arena.getGameState() == GameState.STARTING) {
                if (!arena.addPlayer(p, false)) {
                    p.kickPlayer(LanguageManager.getINSTANCE().getMsg(p, CommonMessage.ARENA_JOIN_DENIED_GAME_FULL));
                }
            } else {
                // Add spectator
                if (!arena.addSpectator(p, null)) {
                    p.kickPlayer(LanguageManager.getINSTANCE().getMsg(p, CommonMessage.ARENA_JOIN_DENIED_SPECTATOR));
                }
            }
        }
    }
}


