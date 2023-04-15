package dev.andrei1058.game.connector.common;

import dev.andrei1058.game.common.selector.SelectorManager;
import dev.andrei1058.game.connector.SteveSusConnector;
import dev.andrei1058.game.connector.api.event.GameDropEvent;
import dev.andrei1058.game.connector.api.event.GameStateChangeEvent;
import dev.andrei1058.game.connector.api.event.PlayerGameJoinEvent;
import dev.andrei1058.game.connector.arena.ArenaManager;
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
