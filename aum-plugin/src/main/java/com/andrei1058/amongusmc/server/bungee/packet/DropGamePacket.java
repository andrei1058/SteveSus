package com.andrei1058.amongusmc.server.bungee.packet;

import com.andrei1058.amoungusmc.common.api.packet.DataPacket;
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
