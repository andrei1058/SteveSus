package dev.andrei1058.game.connector.socket.channel;

import dev.andrei1058.game.common.CommonManager;
import dev.andrei1058.game.common.api.packet.DefaultChannel;
import dev.andrei1058.game.common.api.packet.PacketChannel;
import dev.andrei1058.game.common.api.packet.RawSocket;
import dev.andrei1058.game.common.packet.PingPacket;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

public class SlavePingChannel implements PacketChannel {
    @Override
    public void read(@Nullable RawSocket receiver, JsonObject json) {
        if (json == null) return;
        if (receiver == null) return;
        if (json.isJsonNull()) return;
        CommonManager.getINSTANCE().getCommonProvider().getPacketsHandler().sendPacket(receiver, DefaultChannel.PING.toString(), new PingPacket(), false);
    }
}

