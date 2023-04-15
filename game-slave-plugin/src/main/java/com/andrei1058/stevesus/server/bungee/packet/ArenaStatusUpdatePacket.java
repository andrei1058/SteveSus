package com.andrei1058.stevesus.server.bungee.packet;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.common.api.arena.GameState;
import com.andrei1058.stevesus.common.api.packet.DataPacket;
import com.google.gson.JsonObject;
import org.bukkit.inventory.ItemStack;

public class ArenaStatusUpdatePacket implements DataPacket {

    private final JsonObject json = new JsonObject();

    public ArenaStatusUpdatePacket(Arena arena, GameState newState, GameState oldState) {
        json.addProperty("gameId", arena.getGameId());
        json.addProperty("status", newState.getStateCode());
        json.addProperty("oldStatus", oldState.getStateCode());

        ItemStack itemStack = arena.getDisplayItem(null);
        if (itemStack != null) {
            JsonObject statusItem = new JsonObject();
            statusItem.addProperty("material", itemStack.getType().toString());
            //noinspection deprecation
            statusItem.addProperty("data", itemStack.getData().getData());
            statusItem.addProperty("enchanted", itemStack.getEnchantments().size() != 0);
            json.add("displayItem", statusItem);
        }
    }

    @Override
    public JsonObject getData() {
        return json;
    }
}
