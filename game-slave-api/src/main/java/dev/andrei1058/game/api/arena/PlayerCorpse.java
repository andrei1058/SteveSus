package dev.andrei1058.game.api.arena;

import com.andrei1058.hologramapi.Hologram;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface PlayerCorpse {

    /**
     * Send destroy packet.
     */
    void destroy();

    /**
     * Move corpse to the given location.
     */
    void teleport(Location location);

    /**
     * Play an animation on that corpse.
     * Protocol packet.
     */
    void playAnimation(int animation);

    /**
     * Get entity id.
     */
    int getEntityId();

    /**
     * Whose body is that.
     */
    UUID getOwner();

    /**
     * Get hologram to hide/ show it when necessary.
     */
    @Nullable Hologram getHologram();

    /**
     * Check if the given location is in corpse's range.
     * Used to show/ hide holograms 3*3*3.
     */
    boolean isInRange(Location location);
}
