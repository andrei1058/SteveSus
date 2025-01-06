package com.andrei1058.stevesus.api.hook.hologram;

import com.andrei1058.stevesus.api.hook.hologram.easyholo.EasyHoloAdapter;
import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.holoeasy.HoloEasy;
import org.holoeasy.packet.PacketImpl;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public class HologramManager {

    private static HologramManager instance = new HologramManager();
    private @Nullable HologramProvider provider;

    private HologramManager() {}

    public static HologramManager getInstance() {
        return instance;
    }

    public @Nullable HologramProvider getProvider() {
        return provider;
    }

    public void setProvider(@Nullable HologramProvider provider) {
        this.provider = provider;
    }

    public void onLoad(Plugin plugin) {
        var packetEvents = Bukkit.getPluginManager().getPlugin("packetevents");
        if (null != packetEvents) {
            // maybe check if enabled and then register
            PacketEvents.setAPI(SpigotPacketEventsBuilder.build(plugin));
            //On Bukkit, calling this here is essential, hence the name "load"
            PacketEvents.getAPI().load();
        }
    }

    public void onEnable(Plugin plugin) {
        var packetEvents = Bukkit.getPluginManager().getPlugin("packetevents");
        if (null != packetEvents) {
            //Initialize!
            PacketEvents.getAPI().init();

            // ** Bind the library
            HoloEasy.bind(plugin, PacketImpl.PacketEvents);
            setProvider(new EasyHoloAdapter());
        }
    }

    public void onDisable(Plugin plugin) {
        var packetEvents = Bukkit.getPluginManager().getPlugin("packetevents");
        if (null != packetEvents) {
            //Terminate the instance (clean up process)
            PacketEvents.getAPI().terminate();
        }
    }

    public @Nullable HologramI makeUnsafe(Location location, List<Function<Player, String>> list) {
        if (null == getProvider()) {
            return null;
        }
        var holo = getProvider().spawnHologram(location);
        holo.setPageContent(list);
        return holo;
    }
}
