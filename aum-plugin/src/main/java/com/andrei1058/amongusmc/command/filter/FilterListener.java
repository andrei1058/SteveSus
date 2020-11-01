package com.andrei1058.amongusmc.command.filter;

import com.andrei1058.amongusmc.api.arena.Arena;
import com.andrei1058.amongusmc.arena.ArenaManager;
import com.andrei1058.amoungusmc.common.api.arena.GameState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class FilterListener implements Listener {

    @EventHandler
    public void onPreCommand(PlayerCommandPreprocessEvent e){
        if (e.isCancelled()) return;
        Arena a = ArenaManager.getINSTANCE().getArenaByPlayer(e.getPlayer());
        if (a == null) return;
        if (a.getGameState() == GameState.WAITING || a.getGameState() == GameState.STARTING){
            if (CommandFilter.getPreGame().checkCommand(e.getPlayer(), e.getMessage())){
                e.setCancelled(true);
            }
        } else if (a.getGameState() == GameState.IN_GAME || a.getGameState() == GameState.ENDING){
            if (CommandFilter.getInGame().checkCommand(e.getPlayer(), e.getMessage())){
                e.setCancelled(true);
            }
        }
    }
}
