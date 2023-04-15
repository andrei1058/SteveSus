package dev.andrei1058.game.connector.socket.channel;

import dev.andrei1058.game.common.api.packet.PacketChannel;
import dev.andrei1058.game.common.api.packet.RawSocket;
import dev.andrei1058.game.common.selector.SelectorManager;
import dev.andrei1058.game.connector.SteveSusConnector;
import dev.andrei1058.game.connector.api.arena.RemoteArena;
import dev.andrei1058.game.connector.arena.ArenaManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

public class PlayerCountChannel implements PacketChannel {
    @Override
    public void read(@Nullable RawSocket receiver, JsonObject json) {
        if (json == null) return;
        if (receiver == null) return;
        if (json.isJsonNull()) return;

        int gameId;
        int players;
        int spectators;
        int vips;

        try {
            JsonElement jObject = json.get("gameId");
            gameId = jObject.getAsInt();
            jObject = json.get("players");
            players = jObject.getAsInt();
            jObject = json.get("spectators");
            spectators = jObject.getAsInt();
            jObject = json.get("vips");
            vips = jObject.getAsInt();
        } catch (UnsupportedOperationException | NullPointerException ignored) {
            SteveSusConnector.debug("Received invalid data on " + getClass().getSimpleName());
            // invalid json
            return;
        }

        SteveSusConnector.newChain().sync(() -> {
            RemoteArena remoteArena = ArenaManager.getInstance().getArena(receiver, gameId);
            if (remoteArena != null) {
                remoteArena.setCurrentPlayers(players);
                remoteArena.setCurrentSpectators(spectators);
                remoteArena.setVips(vips);

                // refresh selector here because there is not listener for that
                SelectorManager.getINSTANCE().refreshArenaSelector();
            }
        }).execute();
    }
}
