package com.andrei1058.amongusmc.api.world;

import org.jetbrains.annotations.Nullable;

public interface WorldManager {

    /**
     * Get server world adapter.
     *
     * @return world handler.
     */
    WorldAdapter getWorldAdapter();

    /**
     * Change server World Adapter.
     *
     * @param worldAdapter your custom adapter. Use NULL to switch back to the internal adapter.
     * @return true if set successfully, otherwise false if there are loaded arenas or setup sessions.
     */
    boolean setWorldAdapter(@Nullable WorldAdapter worldAdapter);
}
