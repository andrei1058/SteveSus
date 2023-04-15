package dev.andrei1058.game.arena.listener;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.arena.GameListener;
import dev.andrei1058.game.arena.ArenaManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class PlayerSneakEvent implements Listener {

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        if (event.isCancelled()) return;
        GameArena gameArena = ArenaManager.getINSTANCE().getArenaByPlayer(event.getPlayer());
        if (gameArena != null) {
            final Player player = event.getPlayer();
            final boolean isSneaking = event.isSneaking();
            for (GameListener listener : gameArena.getGameListeners()){
                listener.onPlayerToggleSneakEvent(gameArena, player, isSneaking);
            }
        }
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event){
        if (event.isCancelled()) return;
        GameArena gameArena = ArenaManager.getINSTANCE().getArenaByPlayer(event.getPlayer());
        if (gameArena != null) {
            final Player player = event.getPlayer();
            final boolean isSneaking = event.isFlying();
            for (GameListener listener : gameArena.getGameListeners()){
                listener.onPlayerToggleFly(gameArena, player, isSneaking);
            }
        }
    }
}
