package dev.andrei1058.game.common.packet;

import dev.andrei1058.game.common.api.packet.DataPacket;
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
