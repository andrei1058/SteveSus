package com.andrei1058.stevesus.arena.listener;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.arena.ArenaManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageListener implements Listener {

    @EventHandler
    public void onDamageFromEntity(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        Arena arena = ArenaManager.getINSTANCE().getArenaByWorld(e.getEntity().getWorld().getName());
        if (arena != null){
            e.setCancelled(true);
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
}
