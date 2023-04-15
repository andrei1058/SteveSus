package dev.andrei1058.game.prevention.abandon;

import dev.andrei1058.game.api.arena.Arena;
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
        Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer(e.getPlayer());
        if (arena != null){
            if (arena.isPlayer(e.getPlayer())){
                final String command = e.getMessage();
                if (PreventionManager.getInstance().getCommandTriggers().stream().anyMatch(command::startsWith)){
                    if (arena.getGameState() == GameState.IN_GAME) {
                        if (PreventionManager.getInstance().triggerAbandon(arena, e.getPlayer())) {
                            PreventionManager.getInstance().setAbandoned(e.getPlayer().getUniqueId());
                        }
                    }
                }
            }
        }
    }
}