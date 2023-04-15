package dev.andrei1058.game.server.bungee.packet;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.common.api.packet.DataPacket;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public class FullDataArenaPacket implements DataPacket {

    private final JsonObject object;

    public FullDataArenaPacket(@NotNull GameArena gameArena){
        object = gameArena.toJSON();
    }

    @Override
    public JsonObject getData() {
        return object;
    }
}
