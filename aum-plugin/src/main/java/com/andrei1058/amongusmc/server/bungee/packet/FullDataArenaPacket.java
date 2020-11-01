package com.andrei1058.amongusmc.server.bungee.packet;

import com.andrei1058.amongusmc.api.arena.Arena;
import com.andrei1058.amoungusmc.common.api.packet.DataPacket;
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
