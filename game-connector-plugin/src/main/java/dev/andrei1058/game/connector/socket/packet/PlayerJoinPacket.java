package dev.andrei1058.game.connector.socket.packet;

import dev.andrei1058.game.common.api.arena.DisplayableArena;
import dev.andrei1058.game.common.api.packet.DataPacket;
import dev.andrei1058.game.connector.language.LanguageManager;
import com.google.gson.JsonObject;
import org.bukkit.entity.Player;

public class PlayerJoinPacket implements DataPacket {

    private final JsonObject packet = new JsonObject();

    public PlayerJoinPacket(Player player, String partyOwner, DisplayableArena arena) {
        packet.addProperty("player", player.getUniqueId().toString());
        if (partyOwner != null) {
            packet.addProperty("target", partyOwner);
        }
        packet.addProperty("gameId", arena.getGameId());
        packet.addProperty("lang", LanguageManager.getINSTANCE().getLocale(player).getIsoCode());
    }

    @Override
    public JsonObject getData() {
        return packet;
    }
}
