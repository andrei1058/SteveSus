package com.andrei1058.stevesus.arena.region;

import org.bukkit.Location;

public class CircleRegion implements Region {

    Location location;
    double range;
    boolean protect;

    public CircleRegion(Location location, double range, boolean protect) {
        this.location = location;
        this.range = range;
        this.protect = protect;
    }

    @Override
    public boolean isInRegion(Location location) {
        return location.distance(this.location) <= range;
    }

    @Override
    public boolean isProtected() {
        return protect;
    }
}
