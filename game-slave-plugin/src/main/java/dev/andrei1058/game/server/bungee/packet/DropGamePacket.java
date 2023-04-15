package dev.andrei1058.game.server.bungee.packet;

import dev.andrei1058.game.common.api.packet.DataPacket;
import com.google.gson.JsonObject;

/**
 * Sent when a game is destroyed.
 */
public class DropGamePacket implements DataPacket {

    private final JsonObject json = new JsonObject();

    public DropGamePacket(int gameId){
        json.addProperty("gameId", gameId);
    }

    @Override
    public JsonObject getData() {
        return json;
    }
}
