package com.andrei1058.stevesus.arena.listener;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.GameListener;
import com.andrei1058.stevesus.arena.ArenaManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class PlayerSneakEvent implements Listener {

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        if (event.isCancelled()) return;
        Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer(event.getPlayer());
        if (arena != null) {
            final Player player = event.getPlayer();
            final boolean isSneaking = event.isSneaking();
            for (GameListener listener : arena.getGameListeners()){
                listener.onPlayerToggleSneakEvent(arena, player, isSneaking);
            }
        }
    }
}
