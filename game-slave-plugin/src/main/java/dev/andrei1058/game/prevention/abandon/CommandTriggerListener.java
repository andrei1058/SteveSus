package dev.andrei1058.game.prevention.abandon;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.common.api.arena.GameState;
import dev.andrei1058.game.prevention.PreventionManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandTriggerListener implements Listener {

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e){
        if (e.isCancelled()) return;
        GameArena gameArena = ArenaManager.getINSTANCE().getArenaByPlayer(e.getPlayer());
        if (gameArena != null){
            if (gameArena.isPlayer(e.getPlayer())){
                final String command = e.getMessage();
                if (PreventionManager.getInstance().getCommandTriggers().stream().anyMatch(command::startsWith)){
                    if (gameArena.getGameState() == GameState.IN_GAME) {
                        if (PreventionManager.getInstance().triggerAbandon(gameArena, e.getPlayer())) {
                            PreventionManager.getInstance().setAbandoned(e.getPlayer().getUniqueId());
                        }
                    }
                }
            }
        }
    }
}
