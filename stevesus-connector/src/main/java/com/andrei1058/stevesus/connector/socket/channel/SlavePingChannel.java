package com.andrei1058.stevesus.connector.socket.channel;

import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.packet.PingPacket;
import com.andrei1058.stevesus.common.api.packet.DefaultChannel;
import com.andrei1058.stevesus.common.api.packet.PacketChannel;
import com.andrei1058.stevesus.common.api.packet.RawSocket;
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

