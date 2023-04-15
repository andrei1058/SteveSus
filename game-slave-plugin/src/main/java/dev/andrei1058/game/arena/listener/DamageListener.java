package dev.andrei1058.game.arena.listener;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.arena.GameListener;
import dev.andrei1058.game.api.arena.meeting.MeetingButton;
import dev.andrei1058.game.arena.ArenaManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class DamageListener implements Listener {

    @EventHandler
    public void onDamageFromEntity(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        GameArena gameArena = ArenaManager.getINSTANCE().getArenaByWorld(e.getEntity().getWorld().getName());
        if (gameArena != null) {
            e.setCancelled(true);
            if (!(e.getDamager() instanceof Player)) return;

            if (gameArena.getMeetingButton() != null) {
                if (e.getEntity().hasMetadata(MeetingButton.MEETING_BUTTON_META_DATA_KEY)) {
                    gameArena.getMeetingButton().onClick((Player) e.getDamager(), gameArena);
                }
            }

            for (GameListener gameListener : gameArena.getGameListeners()) {
                gameListener.onPlayerInteractEntity(gameArena, (Player) e.getDamager(), e.getEntity());
            }
        }
    }

    @EventHandler
    public void onDamageFromBlock(EntityDamageByBlockEvent e) {
        if (e.isCancelled()) return;
        GameArena gameArena = ArenaManager.getINSTANCE().getArenaByWorld(e.getEntity().getWorld().getName());
        if (gameArena != null) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.isCancelled()) return;
        GameArena gameArena = ArenaManager.getINSTANCE().getArenaByWorld(e.getEntity().getWorld().getName());
        if (gameArena != null) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        if (e.isCancelled()) return;
        GameArena gameArena = ArenaManager.getINSTANCE().getArenaByWorld(e.getEntity().getWorld().getName());
        if (gameArena != null) {
            e.setCancelled(true);
        }
    }
}
