package com.andrei1058.stevesus.connector.socket.channel;

import com.andrei1058.stevesus.connector.SteveSusConnector;
import com.andrei1058.stevesus.connector.arena.ArenaManager;
import com.andrei1058.stevesus.common.api.packet.PacketChannel;
import com.andrei1058.stevesus.common.api.packet.RawSocket;
import com.andrei1058.stevesus.connector.api.arena.RemoteArena;
import com.andrei1058.stevesus.connector.api.event.GameDropEvent;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

public class GameDropChannel implements PacketChannel {
    @Override
    public void read(@Nullable RawSocket receiver, JsonObject json) {
        if (json == null) return;
        if (receiver == null) return;
        if (json.isJsonNull()) return;

        int gameId;

        try {
            JsonElement jObject = json.get("gameId");
            gameId = jObject.getAsInt();
        } catch (Exception ignored) {
            SteveSusConnector.debug("Received invalid data on " + getClass().getSimpleName());
            // invalid json
            return;
        }

        SteveSusConnector.newChain().sync(() -> {
            RemoteArena remoteArena = ArenaManager.getInstance().getArena(receiver, gameId);
            if (remoteArena != null) {
                ArenaManager.getInstance().remove(remoteArena);
                Bukkit.getPluginManager().callEvent(new GameDropEvent(remoteArena));
            }
        }).execute();
    }
}
