package dev.andrei1058.game.api.arena.vent;

import com.andrei1058.hologramapi.Hologram;
import com.andrei1058.hologramapi.HologramPage;
import com.andrei1058.hologramapi.content.LineTextContent;
import dev.andrei1058.game.api.SteveSusAPI;
import dev.andrei1058.game.api.locale.Message;
import com.andrei1058.stevesus.common.CommonManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Vent {

    private final String identifier;
    private final Location location;
    private final LinkedList<Vent> connections = new LinkedList<>();
    private ItemStack displayItem;
    private final Hologram hologram;

    /**
     * Create a vent.
     *
     * @param identifier unique id.
     * @param block      trap block on which players must shift to start venting.
     */
    public Vent(@NotNull String identifier, @NotNull Location location, @NotNull ItemStack displayItem) {
        this.identifier = identifier;
        this.location = location;
        this.location.setX(location.getBlockX() + 0.5);
        this.location.setZ(location.getBlockZ() + 0.5);
        this.displayItem = CommonManager.getINSTANCE().getItemSupport().addTag(displayItem, "nextVent", identifier);
        hologram = new Hologram(location.clone().add(0, 0.5, 0), 1);
        HologramPage page = hologram.getPage(0);
        assert page != null;
        page.setLineContent(0, new LineTextContent(s -> SteveSusAPI.getInstance().getLocaleHandler().getMsg(s, Message.VENT_HOLO)));
        hologram.hide();
        hologram.allowCollisions(false);
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

    public Hologram getHologram() {
        return hologram;
    }
}
