package com.andrei1058.stevesus.worldmanager;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class MapLoadListener implements Listener {

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event){
        if (WorldManager.getINSTANCE().getWorldAdapter() instanceof InternalWorldAdapter){
            InternalWorldAdapter.LoadQueue loadQueue = ((InternalWorldAdapter) WorldManager.getINSTANCE().getWorldAdapter()).getQueue().peek();
            if (loadQueue != null){
                if (event.getWorld().getName().equals(loadQueue.getExpectedWorldName())){
                    ((InternalWorldAdapter) WorldManager.getINSTANCE().getWorldAdapter()).nextInQueue();
                }
            }
        }
    }
}
