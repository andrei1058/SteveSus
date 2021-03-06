package com.andrei1058.stevesus.connector.api.event;

import com.andrei1058.stevesus.common.api.packet.RawSocket;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SlaveDisconnectedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final RawSocket socket;

    /**
     * Triggered when a slave is disconnected.
     * A slave is a minecraft server instance with arenas.
     *
     * @param socket slave socket.
     */
    public SlaveDisconnectedEvent(RawSocket socket) {
        this.socket = socket;
    }

    /**
     * Get arena.
     *
     * @return arena.
     */
    public RawSocket getSocket() {
        return socket;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
