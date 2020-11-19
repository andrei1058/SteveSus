package com.andrei1058.stevesus.api.arena.room;

import com.andrei1058.stevesus.api.locale.Locale;
import com.andrei1058.stevesus.api.locale.Message;

public class GameRoom {

    private final String identifier;
    private final Region region;

    public GameRoom(Region region, String identifier) {
        this.region = region;
        this.identifier = identifier;
    }

    public Region getRegion() {
        return region;
    }

    String getIdentifier() {
        return identifier;
    }

    public String getDisplayName(Locale locale) {
        return locale.getMsg(null, Message.GAME_ROOM_NAME_.toString() + getIdentifier());
    }
}
