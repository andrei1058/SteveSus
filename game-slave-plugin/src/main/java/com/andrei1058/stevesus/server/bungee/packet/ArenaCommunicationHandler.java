package com.andrei1058.stevesus.server.bungee.packet;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.common.api.packet.CommunicationHandler;
import com.andrei1058.stevesus.common.api.packet.DataPacket;
import com.andrei1058.stevesus.common.api.packet.PacketChannel;
import com.andrei1058.stevesus.common.api.packet.RawSocket;
import com.andrei1058.stevesus.config.MainConfig;
import com.andrei1058.stevesus.server.ServerManager;
import com.andrei1058.stevesus.server.bungee.remote.RemoteLobby;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;

public class ArenaCommunicationHandler implements CommunicationHandler {

    private static final ConcurrentHashMap<String, PacketChannel> channelsByName = new ConcurrentHashMap<>();

    @Override
    public boolean registerIncomingPacketChannel(String channel, PacketChannel packetChannel) {
        if (channel.trim().length() > 8) return false;
        if (isChannelRegistered(channel)) return false;
        channelsByName.put(channel, packetChannel);
        return true;
    }

    @Override
    public boolean isChannelRegistered(String channel) {
        return channelsByName.containsKey(channel);
    }

    @Override
    public void sendPacket(String channel, DataPacket packet, boolean async) {
        if (packet == null) return;
        JsonObject jsonPart = packet.getData();
        if (jsonPart == null) return;
        if (jsonPart.isJsonNull()) return;
        if (jsonPart.size() == 0) return;
        JsonObject packetRoot = new JsonObject();
        packetRoot.addProperty("channel", channel);
        packetRoot.addProperty("server", ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.BUNGEE_AUTO_SCALE_PROXIED_NAME));
        packetRoot.add("data", jsonPart);

        if (async) {
            SteveSus.newChain().async(() -> RemoteLobby.getSockets().values().forEach(lobby -> lobby.sendPacket(packetRoot))).execute();
        } else {
            RemoteLobby.getSockets().values().forEach(lobby -> lobby.sendPacket(packetRoot));
        }
    }

    @Override
    public void sendPacket(RawSocket socket, String channel, DataPacket packet, boolean async) {
        if (socket == null) return;
        JsonObject jsonPart = packet.getData();
        if (jsonPart == null) return;
        if (jsonPart.isJsonNull()) return;
        if (jsonPart.size() == 0) return;
        JsonObject packetRoot = new JsonObject();
        packetRoot.addProperty("channel", channel);
        packetRoot.addProperty("server", ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.BUNGEE_AUTO_SCALE_PROXIED_NAME));
        packetRoot.add("data", jsonPart);
        if (async) {
            SteveSus.newChain().async(() -> socket.sendPacket(packetRoot));
        } else {
            socket.sendPacket(packetRoot);
        }
    }

    @Override
    public @Nullable PacketChannel getChannelByName(String name) {
        return channelsByName.get(name);
    }
}
