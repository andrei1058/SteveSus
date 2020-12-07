package com.andrei1058.stevesus.arena.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

public class EntityTargetListener implements Listener {

    @EventHandler
    public void onTarget(EntityTargetLivingEntityEvent event){
        event.setCancelled(true);
    }
}
