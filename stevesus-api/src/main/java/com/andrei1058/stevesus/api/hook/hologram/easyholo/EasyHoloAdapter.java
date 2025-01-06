package com.andrei1058.stevesus.api.hook.hologram.easyholo;

import com.andrei1058.stevesus.api.hook.hologram.HologramI;
import com.andrei1058.stevesus.api.hook.hologram.HologramProvider;
import org.bukkit.Location;

public class EasyHoloAdapter implements HologramProvider {
    @Override
    public HologramI spawnHologram(Location location) {
        return new EasyHolo(location);
    }
}
