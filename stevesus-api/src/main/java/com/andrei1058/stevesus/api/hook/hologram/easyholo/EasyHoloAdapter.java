package com.andrei1058.stevesus.api.hook.hologram.easyholo;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.hook.hologram.HologramI;
import com.andrei1058.stevesus.api.hook.hologram.HologramProvider;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentLinkedQueue;

public class EasyHoloAdapter implements HologramProvider {

    private ConcurrentLinkedQueue<HologramI> trackedHolograms = new ConcurrentLinkedQueue<>();
    private Plugin plugin;

    @Override
    public HologramI spawnHologram(Location location) {
        var holo = new EasyHolo(location);
        trackedHolograms.add(holo);
        return holo;
    }

    @Override
    public void onArenaLeave(@NotNull Arena arena, Player player) {
        if (null == arena.getWorld()) {
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            trackedHolograms.forEach(item -> {
                var world = item.getLocation().getWorld();
                if (null != world && world.getName().equals(arena.getWorld().getName())) {
                    item.destroyPlayer(player);
                }
            });
        });
    }

    @Override
    public void onAdapterInit(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onArenaDestroy(@NotNull Arena arena) {
        if (null == arena.getWorld()) {
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            trackedHolograms.forEach(item -> {
                var world = item.getLocation().getWorld();
                if (null != world && world.getName().equals(arena.getWorld().getName())) {
                    trackedHolograms.remove(item);
                    item.remove();
                }
            });
        });
    }

}
