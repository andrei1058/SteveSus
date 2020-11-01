package com.andrei1058.amongusmc.connector.socket.channel;

import com.andrei1058.amongusmc.common.gui.ItemUtil;
import com.andrei1058.amongusmc.connector.AmongUsConnector;
import com.andrei1058.amongusmc.connector.arena.ArenaManager;
import com.andrei1058.amoungusmc.common.api.arena.GameState;
import com.andrei1058.amoungusmc.common.api.packet.PacketChannel;
import com.andrei1058.amoungusmc.common.api.packet.RawSocket;
import com.andrei1058.amoungusmc.connector.api.arena.RemoteArena;
import com.andrei1058.amoungusmc.connector.api.event.GameStateChangeEvent;
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
            AmongUsConnector.debug("Received invalid data on " + getClass().getSimpleName());
            // invalid json
            return;
        }

        ItemStack finalDisplayItem = displayItem;
        AmongUsConnector.newChain().sync(() -> {
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
