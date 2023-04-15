package dev.andrei1058.game.server.disconnect;

import co.aikar.taskchain.TaskChain;
import dev.andrei1058.game.SteveSus;
import dev.andrei1058.game.api.server.DisconnectHandler;
import dev.andrei1058.game.config.MainConfig;
import dev.andrei1058.game.server.ServerManager;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class InternalDisconnectHandler implements DisconnectHandler {

    public String getName() {
        return "internal";
    }

    @Override
    public void performDisconnect(Player player) {
        TaskChain<?> chain = SteveSus.newChain();

        String[] servers = ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.DISCONNECT_ADAPTER_INTERNAL).trim().split(",");
        Arrays.asList(servers).forEach(sv -> chain.sync(() -> {
            if (!player.isOnline()) {
                chain.abortChain();
            }
            //noinspection UnstableApiUsage
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(sv);
            player.sendPluginMessage(SteveSus.getInstance(), "BungeeCord", out.toByteArray());
        }).delay(20));

        chain.sync(() -> {
            if (player.isOnline()) {
                player.kickPlayer(ChatColor.BLUE + "You're not supposed to be still here!\n It looks like fallback lobbies are down and you were kicked.");
            }
        });
        chain.execute();
    }
}
