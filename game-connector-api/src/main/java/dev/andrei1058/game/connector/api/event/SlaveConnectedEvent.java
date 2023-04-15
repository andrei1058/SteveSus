package dev.andrei1058.game.connector.api.event;

import dev.andrei1058.game.common.api.packet.RawSocket;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SlaveConnectedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final RawSocket socket;

    /**
     * Triggered when a new remote slave is connected.
     * A slave is a minecraft server instance with arenas.
     *
     * @param socket slave socket.
     */
    public SlaveConnectedEvent(RawSocket socket) {
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
