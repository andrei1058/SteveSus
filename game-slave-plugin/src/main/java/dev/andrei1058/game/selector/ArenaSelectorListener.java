package dev.andrei1058.game.selector;

import dev.andrei1058.game.SteveSus;
import dev.andrei1058.game.api.event.*;
import dev.andrei1058.game.common.selector.SelectorManager;
import dev.andrei1058.game.server.ServerCommonProvider;
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
