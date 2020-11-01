package com.andrei1058.amongusmc.arena.listener;

import ch.jalu.configme.SettingsManager;
import com.andrei1058.amongusmc.api.event.GameStateChangeEvent;
import com.andrei1058.amongusmc.arena.ArenaManager;
import com.andrei1058.amongusmc.config.ArenaConfig;
import com.andrei1058.amoungusmc.common.api.arena.GameState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CloneArenaListener implements Listener {

    @EventHandler
    public void onGameStateChange(GameStateChangeEvent e){
        if (e.getNewState() == GameState.IN_GAME){
            // make a new arena available if required
            SettingsManager config = ArenaManager.getINSTANCE().getTemplate(e.getArena().getTemplateWorld(), false);
            int clonesAvailableAtOnce = config.getProperty(ArenaConfig.CLONES_AVAILABLE_AT_ONCE);
            int currentlyAvailable = (int) ArenaManager.getINSTANCE().getArenas().stream().filter(arena -> arena.getTemplateWorld().equals(e.getArena().getTemplateWorld())).count();
            if (currentlyAvailable < clonesAvailableAtOnce){
                ArenaManager.getINSTANCE().startArenaFromTemplate(e.getArena().getTemplateWorld());
            }
        }
    }
}
