package com.andrei1058.amongusmc.selector;

import com.andrei1058.amongusmc.AmongUsMc;
import com.andrei1058.amongusmc.api.event.*;
import com.andrei1058.amongusmc.server.ServerCommonProvider;
import com.andrei1058.amongusmc.common.selector.SelectorManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ArenaSelectorListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerGameLeave(PlayerGameLeaveEvent e) {
        AmongUsMc.newChain().delay(10).sync(() -> SelectorManager.getINSTANCE().refreshArenaSelector()).execute();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerGameJoin(PlayerGameJoinEvent e) {
        if (!e.isCancelled()) {
            AmongUsMc.newChain().delay(10).sync(() -> SelectorManager.getINSTANCE().refreshArenaSelector()).execute();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameStateChange(GameStateChangeEvent e) {
        AmongUsMc.newChain().delay(10).sync(() -> SelectorManager.getINSTANCE().refreshArenaSelector()).execute();
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
