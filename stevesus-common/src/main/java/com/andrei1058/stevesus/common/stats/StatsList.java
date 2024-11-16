package com.andrei1058.stevesus.common.stats;

public enum StatsList {
    PRIMARY_KEY("player"),
    FIRST_PLAY("first_play"),
    LAST_PLAY("last_play"),
    GAMES_ABANDONED("games_abandoned"),
    GAMES_WON("games_won"),
    GAMES_LOST("games_lost"),

    KILLS("kills"),
    SABOTAGES("sabotages"),
    FIXED_SABOTAGES("sabotages_fixed"),
    TASKS("tasks");

    private final String name;

    StatsList(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
