package dev.andrei1058.game.connector.socket.channel;

import dev.andrei1058.game.common.api.arena.GameState;
import dev.andrei1058.game.common.api.packet.PacketChannel;
import dev.andrei1058.game.common.api.packet.RawSocket;
import dev.andrei1058.game.common.gui.ItemUtil;
import dev.andrei1058.game.connector.SteveSusConnector;
import dev.andrei1058.game.connector.api.arena.RemoteArena;
import dev.andrei1058.game.connector.arena.ArenaManager;
import dev.andrei1058.game.connector.arena.CachedArena;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class ArenaFullDataChannel implements PacketChannel {

    @Override
    public void read(@Nullable RawSocket receiver, JsonObject json) {
        if (json == null) return;
        if (receiver == null) return;
        if (json.isJsonNull()) return;

        int gameId;
        String template;
        String displayName;
        GameState gameState;
        String spectatePerm;
        int maxPlayers;
        int minPlayers;
        int players;
        int spectators;
        int vips = 0;
        ItemStack displayItem = null;

        try {
            JsonElement jObject = json.get("gameId");
            gameId = jObject.getAsInt();
            jObject = json.get("template");
            template = jObject.getAsString();
            jObject = json.get("displayName");
            displayName = jObject.getAsString();
            jObject = json.get("status");
            int stateCode = jObject.getAsInt();
            gameState = GameState.getByCode(stateCode);
            if (gameState == null) {
                // invalid state
                return;
            }
            jObject = json.get("spectate");
            spectatePerm = jObject.getAsString();
            jObject = json.get("maxPlayers");
            maxPlayers = jObject.getAsInt();
            jObject = json.get("minPlayers");
            minPlayers = jObject.getAsInt();
            jObject = json.get("players");
            players = jObject.getAsInt();
            jObject = json.get("spectators");
            spectators = jObject.getAsInt();

            if (json.has("vips")){
                jObject = json.get("vips");
                vips = jObject.getAsInt();
            }

            if (json.get("displayItem") != null) {
                JsonObject jItem = json.get("displayItem").getAsJsonObject();

                String material;
                byte data = 0;
                boolean enchanted = false;

                jObject = jItem.get("material");
                material = jObject.getAsString();
                if (jItem.has("enchanted")) {
                    jObject = jItem.get("enchanted");
                    enchanted = jObject.getAsBoolean();
                }
                if (jItem.has("data")) {
                    jObject = jItem.get("data");
                    data = jObject.getAsByte();
                }

                displayItem = ItemUtil.createItem(material, data, 1, enchanted, new ArrayList<>());
            }
        } catch (UnsupportedOperationException | NullPointerException ignored) {
            SteveSusConnector.debug("Received invalid data on " + getClass().getSimpleName());
            // invalid json
            return;
        }

        RemoteArena remoteArena = ArenaManager.getInstance().getArena(receiver, gameId);
        if (remoteArena == null) {
            RemoteArena toAdd = new CachedArena(receiver, gameId, template, displayName, gameState, spectatePerm, maxPlayers, minPlayers, players, spectators, vips, displayItem);
            SteveSusConnector.newChain().sync(() -> ArenaManager.getInstance().add(toAdd)).execute();
        } else {
            //todo replace all data
        }
    }
}
