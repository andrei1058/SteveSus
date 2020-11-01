package com.andrei1058.stevesus.selector;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.event.*;
import com.andrei1058.stevesus.common.selector.SelectorManager;
import com.andrei1058.stevesus.server.ServerCommonProvider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ArenaSelectorListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerGameLeave(PlayerGameLeaveEvent e) {
        SteveSus.newChain().delay(10).sync(() -> SelectorManager.getINSTANCE().refreshArenaSelector()).execute();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerGameJoin(PlayerGameJoinEvent e) {
        if (!e.isCancelled()) {
            SteveSus.newChain().delay(10).sync(() -> SelectorManager.getINSTANCE().refreshArenaSelector()).execute();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameStateChange(GameStateChangeEvent e) {
        SteveSus.newChain().delay(10).sync(() -> SelectorManager.getINSTANCE().refreshArenaSelector()).execute();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameInit(GameInitializedEvent e) {
        ServerCommonProvider.getInstance().add(e.getArena());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameRestart(GameRestartEvent e) {
        ServerCommonProvider.getInstance().remove(e.getArena());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameDisable(GameDisableEvent e) {
        ServerCommonProvider.getInstance().remove(e.getArena());
    }
}
