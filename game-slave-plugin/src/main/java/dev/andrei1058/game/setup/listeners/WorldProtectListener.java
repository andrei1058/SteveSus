package dev.andrei1058.game.setup.listeners;

import dev.andrei1058.game.setup.SetupManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WorldProtectListener implements Listener {

    @EventHandler
    public void onIceMelt(BlockFadeEvent e) {
        if (e.isCancelled()) return;
        if (SetupManager.getINSTANCE().getSession(e.getBlock().getWorld().getName()) != null) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        if (e.isCancelled()) return;
        if (SetupManager.getINSTANCE().getSession(e.getEntity().getWorld().getName()) != null) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e) {
        if (e.isCancelled()) return;
        if (SetupManager.getINSTANCE().getSession(e.getBlock().getWorld().getName()) != null) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onWeather(WeatherChangeEvent e) {
        if (e.isCancelled()) return;
        if (!e.toWeatherState()) return;
        if (SetupManager.getINSTANCE().getSession(e.getWorld().getName()) != null) {
            e.setCancelled(true);
        }
    }
}
