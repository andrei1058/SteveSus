package com.andrei1058.stevesus.common.packet;

import com.andrei1058.stevesus.common.api.packet.DataPacket;
import com.google.gson.JsonObject;

public class DisconnectPacket implements DataPacket {
    @Override
    public JsonObject getData() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("msg", "Disconnect");
        return jsonObject;
    }
}
