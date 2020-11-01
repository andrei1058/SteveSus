package com.andrei1058.amongusmc.server.bungee.packet;

import com.andrei1058.amoungusmc.common.api.packet.DataPacket;
import com.google.gson.JsonObject;

public class PlayerCountUpdatePacket implements DataPacket {
    private final JsonObject json = new JsonObject();

    public PlayerCountUpdatePacket(int gameId, int players, int spectators, int vips){
        json.addProperty("gameId", gameId);
        json.addProperty("players", players);
        json.addProperty("spectators", spectators);
        json.addProperty("vips", vips);
    }


    @Override
    public JsonObject getData() {
        return json;
    }
}
