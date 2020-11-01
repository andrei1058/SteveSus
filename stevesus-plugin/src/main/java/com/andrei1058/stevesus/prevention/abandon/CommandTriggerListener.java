package com.andrei1058.stevesus.prevention.abandon;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.arena.ArenaHandler;
import com.andrei1058.stevesus.prevention.PreventionManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandTriggerListener implements Listener {

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e){
        if (e.isCancelled()) return;
        Arena arena = ArenaHandler.getINSTANCE().getArenaByPlayer(e.getPlayer());
        if (arena != null){
            if (arena.isPlayer(e.getPlayer())){
                final String command = e.getMessage();
                if (PreventionManager.getInstance().getCommandTriggers().stream().anyMatch(command::startsWith)){
                    if (PreventionManager.getInstance().triggerAbandon(arena, e.getPlayer())){
                        PreventionManager.getInstance().setAbandoned(e.getPlayer().getUniqueId());
                    }
                }
            }
        }
    }
}
