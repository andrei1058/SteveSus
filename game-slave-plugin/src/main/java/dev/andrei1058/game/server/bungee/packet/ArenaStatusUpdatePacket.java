package dev.andrei1058.game.server.bungee.packet;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.common.api.arena.GameState;
import dev.andrei1058.game.common.api.packet.DataPacket;
import com.google.gson.JsonObject;
import org.bukkit.inventory.ItemStack;

public class ArenaStatusUpdatePacket implements DataPacket {

    private final JsonObject json = new JsonObject();

    public ArenaStatusUpdatePacket(GameArena gameArena, GameState newState, GameState oldState) {
        json.addProperty("gameId", gameArena.getGameId());
        json.addProperty("status", newState.getStateCode());
        json.addProperty("oldStatus", oldState.getStateCode());

        ItemStack itemStack = gameArena.getDisplayItem(null);
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
