package dev.andrei1058.game.common.api.packet;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

public interface PacketChannel {

    /**
     * @param receiver socked that received this message.
     */
    void read(@Nullable RawSocket receiver, JsonObject object);
}
