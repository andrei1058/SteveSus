package com.andrei1058.stevesus.server.bungee.packet;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.common.api.packet.DataPacket;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public class FullDataArenaPacket implements DataPacket {

    private final JsonObject object;

    public FullDataArenaPacket(@NotNull Arena arena){
        object = arena.toJSON();
    }

    @Override
    public JsonObject getData() {
        return object;
    }
}
