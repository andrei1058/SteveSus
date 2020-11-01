package com.andrei1058.amongusmc.api.setup;

import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Arena Setup Session.
 * <p>
 * A Setup Session is responsible of arena setups.
 * It is used to prevent two players from modifying the same arena at the same time.
 * It will handle most of arena setup logic.
 */
public interface SetupSession {

    /**
     * Get the player doing the setup.
     *
     * @return player.
     */
    Player getPlayer();

    /**
     * Get world name.
     *
     * @return world name.
     */
    String getWorldName();

    /**
     * Triggered when requested world is loaded.
     * Use this to handle player teleport etc.
     * 
     * @param world loaded world.
     */
    void onStart(World world);

    /**
     * Triggered on player disconnect, setup finish, etc.
     * World unload is handled at {@link SetupHandler#removeSession(SetupSession)}
     */
    void onStop();
}
