package com.andrei1058.stevesus.arena.listener;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.MeetingButton;
import com.andrei1058.stevesus.arena.ArenaManager;
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
        Arena arena = ArenaManager.getINSTANCE().getArenaByWorld(e.getEntity().getWorld().getName());
        if (arena != null){
            e.setCancelled(true);

            if (arena.getMeetingButton() == null) return;
            if (!(e.getDamager() instanceof Player)) return;
            if (e.getEntity().hasMetadata(MeetingButton.MEETING_BUTTON_META_DATA_KEY)) {
                arena.getMeetingButton().onClick((Player) e.getDamager(), arena);
            }
        }
    }

    @EventHandler
    public void onDamageFromBlock(EntityDamageByBlockEvent e) {
        if (e.isCancelled()) return;
        Arena arena = ArenaManager.getINSTANCE().getArenaByWorld(e.getEntity().getWorld().getName());
        if (arena != null){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.isCancelled()) return;
        Arena arena = ArenaManager.getINSTANCE().getArenaByWorld(e.getEntity().getWorld().getName());
        if (arena != null){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e){
        if (e.isCancelled()) return;
        Arena arena = ArenaManager.getINSTANCE().getArenaByWorld(e.getEntity().getWorld().getName());
        if (arena != null){
            e.setCancelled(true);
        }
    }
}
