package com.andrei1058.stevesus.arena.listener;

import ch.jalu.configme.SettingsManager;
import com.andrei1058.stevesus.api.event.GameRestartEvent;
import com.andrei1058.stevesus.api.server.ServerType;
import com.andrei1058.stevesus.arena.ArenaHandler;
import com.andrei1058.stevesus.config.ArenaConfig;
import com.andrei1058.stevesus.server.ServerManager;
import com.andrei1058.stevesus.server.bungeelegacy.BungeeLegacyRestartManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RestartArenaListener implements Listener {

    @EventHandler
    public void onGameRestart(GameRestartEvent e){
        ArenaHandler.getINSTANCE().removeArena(e.getArena());
        if (ServerManager.getINSTANCE().getServerType() == ServerType.BUNGEE_LEGACY){
            BungeeLegacyRestartManager.getInstance().performAction(e.getArena());
        } else {
            // make a new arena available if possible
            SettingsManager config = ArenaHandler.getINSTANCE().getTemplate(e.getArena().getTemplateWorld(), false);
            int clonesAvailableAtOnce = config.getProperty(ArenaConfig.CLONES_AVAILABLE_AT_ONCE);
            int currentlyAvailable = (int) ArenaHandler.getINSTANCE().getArenas().stream().filter(arena -> arena.getTemplateWorld().equals(e.getArena().getTemplateWorld())).count();
            if (currentlyAvailable < clonesAvailableAtOnce){
                ArenaHandler.getINSTANCE().startArenaFromTemplate(e.getArena().getTemplateWorld());
            }
        }
    }
}
