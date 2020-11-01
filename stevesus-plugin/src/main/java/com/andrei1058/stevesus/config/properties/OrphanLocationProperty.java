package com.andrei1058.stevesus.config.properties;

import ch.jalu.configme.properties.convertresult.ConvertErrorRecorder;
import ch.jalu.configme.properties.types.PropertyType;
import org.bukkit.Location;

public class OrphanLocationProperty implements PropertyType<Location> {

    @Override
    public Location convert(Object o, ConvertErrorRecorder convertErrorRecorder) {
        if (o == null) return null;
        // x, y, z, yaw, pitch
        if (o instanceof String) {
            String[] retrieved = String.valueOf(o).split(",");
            if (retrieved.length < 3) return null;
            double x, y, z, yaw = 0, pitch = 0;
            try {
                x = Double.parseDouble(retrieved[0]);
                y = Double.parseDouble(retrieved[1]);
                z = Double.parseDouble(retrieved[2]);
            } catch (Exception ex) {
                return null;
            }
            if (retrieved.length >= 5) {
                try {
                    yaw = Double.parseDouble(retrieved[3]);
                    pitch = Double.parseDouble(retrieved[4]);
                } catch (Exception ex) {
                    return null;
                }
            }
            return new Location(null, x, y, z, (float) yaw, (float) pitch);
        }
        return null;
    }

    @Override
    public Object toExportValue(Location location) {
        return round(location.getX(), 2) + "," + round(location.getY(), 2) + "," + round(location.getZ(), 2) + "," + round(location.getYaw(), 2) + "," + round(location.getPitch(), 2);
    }

    // stackoverflow
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}
