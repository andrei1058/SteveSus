package com.andrei1058.stevesus.api.setup;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

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

    /**
     * This must be used when a player started doing a custom setup part.
     * Like game tasks, setting cameras etc.
     * So, set to false if player shouldn't be allowed to use commands during your custom setup part.
     */
    void setAllowCommands(boolean toggle);

    /**
     * Check if player is setting a custom thing.
     * Like game tasks, camera etc.
     * This is used to block commands usage until this
     * is set to ture.
     */
    boolean canUseCommands();

    /**
     * Store some data that can be accessed later via {@link #getCachedValue(String)}.
     */
    void cacheValue(String identifier, Object value);

    /**
     * Get a cached value.
     */
    @Nullable
    Object getCachedValue(String identifier);
}
