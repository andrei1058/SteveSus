package com.andrei1058.stevesus.common.api.packet;

public enum DefaultChannel {

    /**
     * Used to send all required data on remote when an arena is initialized or when a remote lobby is started up.
     */
    ARENA_FULL_DATA("1058-a"),
    PLAYER_COUNT_UPDATE("1058-c"),
    // this is used by lobby servers
    PLAYER_JOIN_CHANNEL("1058-b"),
    ARENA_STATUS_UPDATE("1058-d"),
    GAME_DROP("1058-e"),
    PING("1058-f");


    private final String name;

    // remember. char limit is 8.
    DefaultChannel(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return name;
    }
}
