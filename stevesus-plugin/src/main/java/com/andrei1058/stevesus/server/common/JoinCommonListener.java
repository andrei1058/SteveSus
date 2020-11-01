package com.andrei1058.stevesus.server.common;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.language.LanguageManager;
import com.andrei1058.stevesus.common.stats.StatsManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.UUID;

public class JoinCommonListener implements Listener {


    @EventHandler(priority = EventPriority.MONITOR)
    public void onPreLoginForLanguage(AsyncPlayerPreLoginEvent e) {
        if (e.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            // Do nothing if login fails
            return;
        }
        final UUID p = e.getUniqueId();
        SteveSus.newChain().sync(() -> StatsManager.getINSTANCE().fetchStats(p)).execute();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLoginForLanguage(PlayerLoginEvent e) {
        if (e.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            LanguageManager.getINSTANCE().setPlayerLocale(e.getPlayer().getUniqueId(), null, false);
        }
    }


    // Used to show some details to andrei1058
    // No sensitive data
    public static void displayCustomerDetails(Player player) {
        if (player == null) return;
        //TODO IMPROVE, ADD MORE DETAILS
        if (player.getName().equalsIgnoreCase("andrei1058") || player.getName().equalsIgnoreCase("andreea1058") || player.getName().equalsIgnoreCase("Dani3l_FTW")
                || SteveSus.getInstance().getDescription().getAuthors().contains(player.getName())) {
            player.sendMessage(color("&8[&f" + SteveSus.getInstance().getName() + " v" + SteveSus.getInstance().getDescription().getVersion() + "&8]&7&m---------------------------"));
            player.sendMessage("");
            player.sendMessage(color("&7User ID: &f%%__USER__%%"));
            player.sendMessage(color("&7Download ID: &f%%__NONCE__%%"));
            player.sendMessage("");
            player.sendMessage(color("&8[&f" + SteveSus.getInstance().getName() + "&8]&7&m---------------------------"));
        }
    }

    private static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
