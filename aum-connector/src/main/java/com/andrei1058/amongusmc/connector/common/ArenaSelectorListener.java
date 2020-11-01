package com.andrei1058.amongusmc.connector.common;

import com.andrei1058.amongusmc.common.selector.SelectorManager;
import com.andrei1058.amongusmc.connector.AmongUsConnector;
import com.andrei1058.amongusmc.connector.arena.ArenaManager;
import com.andrei1058.amoungusmc.connector.api.event.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ArenaSelectorListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerGameJoin(PlayerGameJoinEvent e) {
        if (!e.isCancelled()) {
            AmongUsConnector.newChain().delay(10).sync(() -> SelectorManager.getINSTANCE().refreshArenaSelector()).execute();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameStateChange(GameStateChangeEvent e) {
        AmongUsConnector.newChain().delay(10).sync(() -> SelectorManager.getINSTANCE().refreshArenaSelector()).execute();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameDrop(GameDropEvent e) {
        ArenaManager.getInstance().remove(e.getArena());
    }
}
