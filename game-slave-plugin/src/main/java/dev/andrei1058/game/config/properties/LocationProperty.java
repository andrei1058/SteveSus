package dev.andrei1058.game.config.properties;

import ch.jalu.configme.properties.convertresult.ConvertErrorRecorder;
import ch.jalu.configme.properties.types.PropertyType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationProperty implements PropertyType<Location> {

    @Override
    public Location convert(Object o, ConvertErrorRecorder convertErrorRecorder) {
        if (o == null) return null;
        // world, x, y, z, yaw, pitch
        if (o instanceof String) {
            String[] retrieved = String.valueOf(o).split(",");
            if (retrieved.length < 4) return null;
            double x, y, z, yaw = 0, pitch = 0;
            World world;
            try {
                world = Bukkit.getWorld(retrieved[0]);
                x = Double.parseDouble(retrieved[1]);
                y = Double.parseDouble(retrieved[2]);
                z = Double.parseDouble(retrieved[3]);
            } catch (Exception ex) {
                return null;
            }
            if (retrieved.length >= 6) {
                try {
                    yaw = Double.parseDouble(retrieved[3]);
                    pitch = Double.parseDouble(retrieved[4]);
                } catch (Exception ex) {
                    return null;
                }
            }
            return new Location(world, x, y, z, (float) yaw, (float) pitch);
        }
        return null;
    }

    @Override
    public Object toExportValue(Location location) {
        return location.getWorld() == null ? "null" : location.getWorld().getName() + "," + round(location.getX(), 2) + "," + round(location.getY(), 2) + "," + round(location.getZ(), 2) + "," + round(location.getYaw(), 2) + "," + round(location.getPitch(), 2);
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
