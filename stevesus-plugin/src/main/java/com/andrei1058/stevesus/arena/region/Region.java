package com.andrei1058.stevesus.arena.region;

import org.bukkit.Location;

public interface Region {

    /**
     * Check if location is in region.
     */
    boolean isInRegion(Location location);

    /**
     * Check if is a protected region.
     */
    boolean isProtected();

}
