package dev.andrei1058.game.connector.socket;

import dev.andrei1058.game.common.api.packet.CommunicationHandler;
import dev.andrei1058.game.common.api.packet.DataPacket;
import dev.andrei1058.game.common.api.packet.PacketChannel;
import dev.andrei1058.game.common.api.packet.RawSocket;
import dev.andrei1058.game.connector.SteveSusConnector;
import dev.andrei1058.game.connector.socket.slave.SlaveServerSocket;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;

public class LobbyCommunicationHandler implements CommunicationHandler {

    private static final ConcurrentHashMap<String, PacketChannel> channelsByName = new ConcurrentHashMap<>();

    @Override
    public boolean registerIncomingPacketChannel(String channel, PacketChannel packetChannel) {
        if (channel.trim().length() > 8) return false;
        if (isChannelRegistered(channel)) return false;
        channelsByName.put(channel, packetChannel);
        SteveSusConnector.debug("Registered new channel: " + channel);
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
        packetRoot.add("data", jsonPart);
        if (async) {
            SteveSusConnector.newChain().async(() -> SlaveServerSocket.getSockets().values().forEach(slave -> slave.sendPacket(packetRoot)));
        } else {
            SlaveServerSocket.getSockets().values().forEach(slave -> slave.sendPacket(packetRoot));
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
        packetRoot.add("data", jsonPart);
        if (async) {
            SteveSusConnector.newChain().async(() -> socket.sendPacket(packetRoot));
        } else {
            socket.sendPacket(packetRoot);
        }
    }

    @Override
    public @Nullable PacketChannel getChannelByName(String name) {
        return channelsByName.get(name);
    }
}