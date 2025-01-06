package com.andrei1058.stevesus.api.hook.hologram;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.Function;

/**
 * Hologram interface to make it work with any hologram dependency.
 * Used as Wrapper.
 */
public interface HologramI {

    /**
     * @param pageContent per player lines.
     */
    void setPageContent(List<Function<Player, String>> pageContent);

    /**
     * Make hologram invisible to everyone.
     */
    void hideToAll();

    /**
     * Refresh hologram for given player.
     */

    void refreshLines(Player player);

    void refreshForAll();

    /**
     * Toggle {@link #hideToAll()}.
     */
    void unHide();

    void showToPlayer(Player player);
    void hideFromPlayer(Player player);

    /**
     * Destroy hologram.
     * Remove any trace of players and worlds etc.
     */
    void remove();

    boolean isHiddenFor(Player player);

    Location getLocation();

    /**
     * Remove any trace of the given player to avoid memory leaks.
     */
    void destroyPlayer(Player player);
}