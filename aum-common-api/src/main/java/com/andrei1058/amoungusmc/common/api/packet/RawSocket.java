package com.andrei1058.amoungusmc.common.api.packet;

import com.google.gson.JsonObject;

public interface RawSocket {

    /**
     * Send a packet trough a socket.
     * Json based. Packets should be sent async.
     *
     * @param data your custom Json data.
     */
    void sendPacket(JsonObject data);

    /**
     * Get last time when received a packet.
     * Used for life check.
     */
    long getLastPacket();

    /**
     * Close communication.
     */
    void close();

    /**
     * Get bungee name/ identifier.
     */
    String getName();
}
