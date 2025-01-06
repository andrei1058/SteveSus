package com.andrei1058.stevesus.api.hook.hologram;

import com.andrei1058.stevesus.api.arena.Arena;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public interface HologramProvider {
    HologramI spawnHologram(Location location);

    /**
     * Clean up player traces.
     * Usually used to remove current player from hashmaps etc.
     *
     * @param arena  game session.
     * @param player subject.
     */

    void onArenaLeave(Arena arena, Player player);

    void onAdapterInit(Plugin plugin);

    void onArenaDestroy(Arena arena);
}
