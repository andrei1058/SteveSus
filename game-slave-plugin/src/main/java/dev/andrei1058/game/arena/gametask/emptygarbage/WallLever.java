package dev.andrei1058.game.arena.gametask.emptygarbage;

import dev.andrei1058.game.api.glow.GlowColor;
import dev.andrei1058.game.api.glow.GlowingBox;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

public class WallLever {

    private final GlowingBox glowingBox;
    private final OrderPriority orderPriority;
    private final Location dropLocation;
    private int assignements = 0;

    public WallLever(Location location, @Nullable Location drop, OrderPriority orderPriority) {
        glowingBox = new GlowingBox(location.add(0.5, 0, 0.5), 2, GlowColor.DARK_PURPLE);
        this.dropLocation = drop;
        this.orderPriority = orderPriority;
    }

    public GlowingBox getGlowingBox() {
        return glowingBox;
    }

    public OrderPriority getOrderPriority() {
        return orderPriority;
    }

    public Location getDropLocation() {
        return dropLocation;
    }

    public int getAssignments() {
        return assignements;
    }

    public void increaseAssignments() {
        assignements++;
    }
}
