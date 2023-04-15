package com.andrei1058.stevesus.api.setup;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Arena Setup Session.
 * <p>
 * A Setup Session is responsible of arena setups.
 * It is used to prevent two players from modifying the same arena at the same time.
 * It will handle most of arena setup logic.
 */
@SuppressWarnings("unused")
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
     * Remove a cached data.
     */
    void removeCacheValue(String identifier);

    /**
     * Get a cached value.
     */
    @Nullable
    Object getCachedValue(String identifier);

    /**
     * A setup listener is used when you require to listen some actions
     * like player interact when someone is doing a map setup.
     * <p>
     * You should use this listener and not raw bukkit events because this is unregistered
     * when a setup session is closed.
     *
     * @param identifier can be used later to unregister listener when no longer needed.
     */
    void addSetupListener(@NotNull String identifier, @NotNull SetupListener listener);

    /**
     * Unregister a setup listener when no longer needed.
     */
    void removeSetupListener(@NotNull String identifier);

    /**
     * Get setup listener.
     * Used by main plugin to trigger events.
     */
    Collection<SetupListener> getSetupListeners();
}
