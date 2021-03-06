package com.andrei1058.stevesus.api.server;

public enum ServerType {

    /**
     * No lobby, just multiple arenas with auto-scaling feature.
     */
    BUNGEE,
    /**
     * No lobby, just a single arena.
     */
    BUNGEE_LEGACY,
    /**
     * Lobby and multiple arenas.
     */
    MULTI_ARENA
}
