package com.andrei1058.stevesus.api.arena.vent;

import com.andrei1058.stevesus.api.SteveSusAPI;
import com.andrei1058.stevesus.api.hook.hologram.HologramI;
import com.andrei1058.stevesus.api.hook.hologram.HologramManager;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.common.CommonManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Vent {

    private final String identifier;
    private final Location location;
    private final LinkedList<Vent> connections = new LinkedList<>();
    private ItemStack displayItem;
    private @Nullable HologramI hologram;

    /**
     * Create a vent.
     *
     * @param identifier unique id.
     */
    public Vent(@NotNull String identifier, @NotNull Location location, @NotNull ItemStack displayItem) {
        this.identifier = identifier;
        this.location = location;
        this.location.setX(location.getBlockX() + 0.5);
        this.location.setZ(location.getBlockZ() + 0.5);
        this.displayItem = CommonManager.getINSTANCE().getItemSupport().addTag(displayItem, "nextVent", identifier);

        if (null == HologramManager.getInstance().getProvider()) {
            var holoManager = HologramManager.getInstance().getProvider();
            hologram = holoManager.spawnHologram(location.clone().add(0, 0.5, 0));
            hologram.setPageContent(List.of(
                    receiver -> SteveSusAPI.getInstance().getLocaleHandler().getMsg(receiver, Message.VENT_HOLO)
            ));
            hologram.hideToAll();
        }
    }

    /**
     * Check if the given block is this vent.
     * Does not check if the same world.
     */
    boolean isThis(Block check) {
        return check.getLocation().getBlockX() == location.getBlockX() && location.getBlockY() == check.getLocation().getBlockY() && check.getLocation().getBlockZ() == location.getBlockZ();
    }

    public String getIdentifier() {
        return identifier;
    }

    public Block getBlock() {
        return location.getBlock();
    }

    public Location getLocation() {
        return location;
    }

    /**
     * Add a connection.
     */
    public void addConnection(Vent vent) {
        connections.add(vent);
    }

    /**
     * Remove a vent connection.
     */
    public void removeConnection(Vent vent) {
        connections.remove(vent);
    }

    /**
     * Get vent connections.
     */
    public List<Vent> getConnections() {
        return Collections.unmodifiableList(connections);
    }

    /**
     * Set vent display item.
     * It is given to impostors when they're venting.
     */
    public void setDisplayItem(@NotNull ItemStack displayItem) {
        this.displayItem = CommonManager.getINSTANCE().getItemSupport().addTag(displayItem, "nextVent", getIdentifier());
    }

    /**
     * Get vent display item.
     * It's the item given to impostors when they're venting.
     */
    @NotNull
    public ItemStack getDisplayItem() {
        return displayItem;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Vent vent = (Vent) o;
        return vent.getIdentifier().equals(getIdentifier());
    }

    public @Nullable HologramI getHologram() {
        return hologram;
    }
}
