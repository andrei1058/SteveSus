package dev.andrei1058.game.common.api.packet;

import org.jetbrains.annotations.Nullable;

public interface CommunicationHandler {

    /**
     * Register incoming packet listener.
     * Incoming packet channel from lobby servers.
     * You should check first if a channel is already registered with {@link #isChannelRegistered(String)}.
     * You cannot listen to someone else's packet id.
     * This is used only when server type is bungee.
     *
     * @param channel       channel id. Char limit is 8. Caps is ignored. Space is ignored.
     * @param packetChannel incoming packets from remote lobbies listener.
     * @return true if registered successfully.
     */
    boolean registerIncomingPacketChannel(String channel, PacketChannel packetChannel);

    /**
     * Check if there is a registered packet listener with the given name.
     *
     * @param channel channel name.
     */
    boolean isChannelRegistered(String channel);

    /**
     * Send a packet to remote sockets.
     * Json based. Packets should be sent async.
     *
     * @param channel remote channel that will handle this packet.
     * @param packet  your custom Json data.
     * @param async   true if you want to send it async or false if you're already sending it from an async task or u know if you know what you're doing.
     */
    void sendPacket(String channel, DataPacket packet, boolean async);

    /**
     * Send a packet to a specific socket.
     * Json based. Packets should be sent async.
     *
     * @param socket target socket.
     * @param channel remote channel that will handle this packet.
     * @param packet  your custom Json data.
     * @param async   true if you want to send it async or false if you're already sending it from an async task or u know if you know what you're doing.
     */
    void sendPacket(RawSocket socket, String channel, DataPacket packet, boolean async);

    /**
     * Retrieve a channel by id.
     *
     * @param name channel id.
     * @return null if not found.
     */
    @Nullable
    PacketChannel getChannelByName(String name);
}
