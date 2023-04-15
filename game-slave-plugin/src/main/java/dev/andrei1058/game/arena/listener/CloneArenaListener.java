package dev.andrei1058.game.arena.listener;

import ch.jalu.configme.SettingsManager;
import dev.andrei1058.game.api.event.GameStateChangeEvent;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.common.api.arena.GameState;
import dev.andrei1058.game.config.ArenaConfig;
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
