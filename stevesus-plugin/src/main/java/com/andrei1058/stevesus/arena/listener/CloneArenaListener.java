package com.andrei1058.stevesus.arena.listener;

import ch.jalu.configme.SettingsManager;
import com.andrei1058.stevesus.api.event.GameStateChangeEvent;
import com.andrei1058.stevesus.arena.ArenaHandler;
import com.andrei1058.stevesus.common.api.arena.GameState;
import com.andrei1058.stevesus.config.ArenaConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CloneArenaListener implements Listener {

    @EventHandler
    public void onGameStateChange(GameStateChangeEvent e){
        if (e.getNewState() == GameState.IN_GAME){
            // make a new arena available if required
            SettingsManager config = ArenaHandler.getINSTANCE().getTemplate(e.getArena().getTemplateWorld(), false);
            int clonesAvailableAtOnce = config.getProperty(ArenaConfig.CLONES_AVAILABLE_AT_ONCE);
            int currentlyAvailable = (int) ArenaHandler.getINSTANCE().getArenas().stream().filter(arena -> arena.getTemplateWorld().equals(e.getArena().getTemplateWorld())).count();
            if (currentlyAvailable < clonesAvailableAtOnce){
                ArenaHandler.getINSTANCE().startArenaFromTemplate(e.getArena().getTemplateWorld());
            }
        }
    }
}
