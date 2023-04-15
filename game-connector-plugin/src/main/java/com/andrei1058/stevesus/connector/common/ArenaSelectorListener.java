package com.andrei1058.stevesus.connector.common;

import com.andrei1058.stevesus.common.selector.SelectorManager;
import com.andrei1058.stevesus.connector.SteveSusConnector;
import com.andrei1058.stevesus.connector.api.event.GameDropEvent;
import com.andrei1058.stevesus.connector.api.event.GameStateChangeEvent;
import com.andrei1058.stevesus.connector.api.event.PlayerGameJoinEvent;
import com.andrei1058.stevesus.connector.arena.ArenaManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ArenaSelectorListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerGameJoin(PlayerGameJoinEvent e) {
        if (!e.isCancelled()) {
            SteveSusConnector.newChain().delay(10).sync(() -> SelectorManager.getINSTANCE().refreshArenaSelector()).execute();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameStateChange(GameStateChangeEvent e) {
        SteveSusConnector.newChain().delay(10).sync(() -> SelectorManager.getINSTANCE().refreshArenaSelector()).execute();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameDrop(GameDropEvent e) {
        ArenaManager.getInstance().remove(e.getArena());
    }
}
