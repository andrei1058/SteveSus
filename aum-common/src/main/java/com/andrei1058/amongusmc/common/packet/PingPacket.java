package com.andrei1058.amongusmc.common.packet;

import com.andrei1058.amoungusmc.common.api.packet.DataPacket;
import com.google.gson.JsonObject;

public class PingPacket implements DataPacket {

    private final JsonObject json = new JsonObject();

    public PingPacket(){
        json.addProperty("ping", "ok");
    }

    @Override
    public JsonObject getData() {
        return json;
    }
}
