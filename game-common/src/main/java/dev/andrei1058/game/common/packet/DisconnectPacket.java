package dev.andrei1058.game.common.packet;

import dev.andrei1058.game.common.api.packet.DataPacket;
import com.google.gson.JsonObject;

public class DisconnectPacket implements DataPacket {
    @Override
    public JsonObject getData() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("msg", "Disconnect");
        return jsonObject;
    }
}
