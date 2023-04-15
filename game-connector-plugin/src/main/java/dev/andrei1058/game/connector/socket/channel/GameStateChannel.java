package dev.andrei1058.game.connector.socket.channel;

import dev.andrei1058.game.common.api.arena.GameState;
import dev.andrei1058.game.common.api.packet.PacketChannel;
import dev.andrei1058.game.common.api.packet.RawSocket;
import dev.andrei1058.game.common.gui.ItemUtil;
import dev.andrei1058.game.connector.SteveSusConnector;
import dev.andrei1058.game.connector.api.arena.RemoteArena;
import dev.andrei1058.game.connector.api.event.GameStateChangeEvent;
import dev.andrei1058.game.connector.arena.ArenaManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class GameStateChannel implements PacketChannel {
    @Override
    public void read(@Nullable RawSocket receiver, JsonObject json) {
        if (json == null) return;
        if (receiver == null) return;
        if (json.isJsonNull()) return;

        int gameId;

        GameState status;
        GameState oldStatus;
        ItemStack displayItem = null;

        try {
            JsonElement jObject = json.get("gameId");
            gameId = jObject.getAsInt();
            jObject = json.get("status");
            int newS = jObject.getAsInt();
            jObject = json.get("oldStatus");
            int oldS = jObject.getAsInt();

            status = GameState.getByCode(newS);
            oldStatus = GameState.getByCode(oldS);

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
        } catch (Exception ignored) {
            SteveSusConnector.debug("Received invalid data on " + getClass().getSimpleName());
            // invalid json
            return;
        }

        ItemStack finalDisplayItem = displayItem;
        SteveSusConnector.newChain().sync(() -> {
            RemoteArena remoteArena = ArenaManager.getInstance().getArena(receiver, gameId);
            if (remoteArena != null) {
                remoteArena.setGameState(status);
                if (finalDisplayItem != null){
                    remoteArena.setDisplayItem(finalDisplayItem);
                }
                Bukkit.getPluginManager().callEvent(new GameStateChangeEvent(remoteArena, oldStatus, status));
            }
        }).execute();
    }
}
