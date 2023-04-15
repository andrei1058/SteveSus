package dev.andrei1058.game.api.arena.room;

import dev.andrei1058.game.api.locale.Locale;
import dev.andrei1058.game.api.locale.Message;

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
