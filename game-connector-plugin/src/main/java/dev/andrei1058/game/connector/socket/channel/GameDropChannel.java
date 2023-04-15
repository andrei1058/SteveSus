package dev.andrei1058.game.connector.socket.channel;

import dev.andrei1058.game.common.api.packet.PacketChannel;
import dev.andrei1058.game.common.api.packet.RawSocket;
import dev.andrei1058.game.connector.SteveSusConnector;
import dev.andrei1058.game.connector.api.arena.RemoteArena;
import dev.andrei1058.game.connector.api.event.GameDropEvent;
import dev.andrei1058.game.connector.arena.ArenaManager;
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
