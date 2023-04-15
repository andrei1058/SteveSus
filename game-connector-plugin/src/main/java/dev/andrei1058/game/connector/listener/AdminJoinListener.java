package dev.andrei1058.game.connector.listener;

import dev.andrei1058.game.connector.SteveSusConnector;
import dev.andrei1058.game.connector.socket.slave.SlaveServerSocket;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class AdminJoinListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        SteveSusConnector.newChain().async(() -> SlaveServerSocket.getSockets().values().forEach(slave -> {
            if (slave.getName() != null && slave.getName().equalsIgnoreCase("notSet")) {
                p.sendMessage(ChatColor.RED + "[" + SteveSusConnector.getInstance().getName() + "] " + ChatColor.RESET +
                        "Slave connected on " + slave.getHostAndPort() + " does not have an assigned bungee name! You won't be able to connect to its games.");
            }
        })).execute();
    }
}
