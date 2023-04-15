package dev.andrei1058.game.server.bungee.channel;

import dev.andrei1058.game.SteveSus;
import dev.andrei1058.game.common.api.packet.PacketChannel;
import dev.andrei1058.game.common.api.packet.RawSocket;
import dev.andrei1058.game.language.LanguageManager;
import dev.andrei1058.game.server.bungee.ProxyUser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

public class PlayerJoinChannel implements PacketChannel {

    @Override
    public void read(@Nullable RawSocket receiver, JsonObject json) {

        String player;
        int gameId;
        String langIso;
        String target;

        try {
            JsonElement je = json.get("player");
            player = je.getAsString();
            je = json.get("gameId");
            gameId = je.getAsInt();
            if (json.has("lang")) {
                je = json.get("lang");
                langIso = je.getAsString();
            } else {
                langIso = LanguageManager.getINSTANCE().getDefaultLocale().getIsoCode();
            }
            if (json.has("target")) {
                je = json.get("target");
                target = je.getAsString();
            } else {
                target = null;
            }
        } catch (Exception ignored) {
            SteveSus.debug("Received bad data on " + getClass().getSimpleName());
            return;
        }

        // cache for about 7 seconds for join delays
        SteveSus.newChain().sync(() -> new ProxyUser(player, gameId, langIso, target)).execute();
    }
}
