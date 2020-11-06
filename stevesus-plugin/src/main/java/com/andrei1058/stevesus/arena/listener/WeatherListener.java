package com.andrei1058.stevesus.arena.listener;

import com.andrei1058.stevesus.arena.ArenaHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WeatherListener implements Listener {

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent e){
        if (e.isCancelled()) return;
        if (!e.toWeatherState()) return;
        if (ArenaHandler.getINSTANCE().getArenaByWorld(e.getWorld().getName()) == null) return;
        e.setCancelled(true);
    }
}
