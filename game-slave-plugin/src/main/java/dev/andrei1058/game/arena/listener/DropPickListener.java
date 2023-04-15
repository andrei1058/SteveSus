package dev.andrei1058.game.arena.listener;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.arena.ArenaManager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class DropPickListener implements Listener {

    @EventHandler
    public void onItemPickUp(EntityPickupItemEvent e) {
        if (e.isCancelled()) return;
        if (e.getEntity().getType() != EntityType.PLAYER) return;
        GameArena gameArena = ArenaManager.getINSTANCE().getArenaByPlayer((Player) e.getEntity());
        if (gameArena != null) {
            e.setCancelled(true);
            /*if (arena.getGameState() == GameState.WAITING || arena.getGameState() == GameState.STARTING){
                e.setCancelled(true);
            } else {
                if (arena.isSpectator((Player) e.getEntity())){
                    e.setCancelled(true);
                }
            }*/
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        if (e.isCancelled()) return;
        GameArena gameArena = ArenaManager.getINSTANCE().getArenaByPlayer(e.getPlayer());
        if (gameArena != null) {
            e.setCancelled(true);
            /*if (arena.getGameState() == GameState.WAITING || arena.getGameState() == GameState.STARTING){
                e.setCancelled(true);
            } else {
                if (arena.isSpectator(e.getPlayer())){
                    e.setCancelled(true);
                }
            }*/
        }
    }

    /*@EventHandler
    public void onItemDrop(EntityDropItemEvent e){
        if (e.isCancelled()) return;
        if (e.getEntity().getType() != EntityType.PLAYER) return;
        Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer((Player) e.getEntity());
        if (arena != null){
            if (arena.getGameState() == GameState.WAITING || arena.getGameState() == GameState.STARTING){
                e.setCancelled(true);
            } else {
                if (arena.isSpectator((Player) e.getEntity())){
                    e.setCancelled(true);
                }
            }
        }
    }*/
}
