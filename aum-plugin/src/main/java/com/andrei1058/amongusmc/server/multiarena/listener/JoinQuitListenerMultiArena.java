package com.andrei1058.amongusmc.server.multiarena.listener;

import com.andrei1058.amongusmc.AmongUsMc;
import com.andrei1058.amongusmc.arena.ArenaManager;
import com.andrei1058.amongusmc.common.command.CommonCmdManager;
import com.andrei1058.amongusmc.sidebar.GameSidebarManager;
import com.andrei1058.amongusmc.sidebar.SidebarType;
import com.andrei1058.amoungusmc.common.api.locale.CommonLocale;
import com.andrei1058.amongusmc.config.MainConfig;
import com.andrei1058.amongusmc.common.database.DatabaseManager;
import com.andrei1058.amongusmc.language.LanguageManager;
import com.andrei1058.amongusmc.server.common.JoinCommonListener;
import com.andrei1058.amongusmc.server.ServerManager;
import com.andrei1058.dbi.operator.EqualsOperator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.UUID;

public class JoinQuitListenerMultiArena implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPreLoginForLanguage(AsyncPlayerPreLoginEvent e) {
        if (e.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            // Do nothing if login fails
            return;
        }
        final UUID p = e.getUniqueId();
        CommonLocale translation = DatabaseManager.getINSTANCE().getDatabase().select(LanguageManager.getINSTANCE().getLanguageTable().LANGUAGE, LanguageManager.getINSTANCE().getLanguageTable(), new EqualsOperator<>(LanguageManager.getINSTANCE().getLanguageTable().PRIMARY_KEY, p));
        AmongUsMc.newChain().sync(() -> LanguageManager.getINSTANCE().setPlayerLocale(p, LanguageManager.getINSTANCE().getLocale(translation.getIsoCode()), false)).execute();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLoginForLanguage(PlayerLoginEvent e) {
        if (e.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            LanguageManager.getINSTANCE().setPlayerLocale(e.getPlayer().getUniqueId(), null, false);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent e) {
        e.setJoinMessage("");
        final Player p = e.getPlayer();

        JoinCommonListener.displayCustomerDetails(p);

        // Show commands if player is op and there is no set arenas
        if (p.isOp()) {
            if (ArenaManager.getINSTANCE().getArenas().isEmpty()) {
                p.performCommand(CommonCmdManager.getINSTANCE().getMainCmd().getName());
            }
        }

        Bukkit.getScheduler().runTaskLater(AmongUsMc.getInstance(), () -> {
            // Hide new player to players and spectators, and vice versa
            // Players from lobby will remain visible
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (ArenaManager.getINSTANCE().isInArena(online)) {
                    online.hidePlayer(AmongUsMc.getInstance(), p);
                    p.hidePlayer(AmongUsMc.getInstance(), online);
                } else {
                    online.showPlayer(AmongUsMc.getInstance(), p);
                    p.showPlayer(AmongUsMc.getInstance(), online);
                }
            }
        }, 14L);

        // Teleport to lobby location
        Location lobbyLocation = ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.MULTI_ARENA_SPAWN_LOC);
        if (lobbyLocation != null && lobbyLocation.getWorld() != null) {
            Bukkit.getScheduler().runTaskLater(AmongUsMc.getInstance(), () -> p.teleport(lobbyLocation, PlayerTeleportEvent.TeleportCause.PLUGIN), 2L);
        } else {
            Bukkit.getScheduler().runTaskLater(AmongUsMc.getInstance(), () -> p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN), 2L);
        }

        p.setExp(0);
        p.setHealthScale(20);
        p.setFoodLevel(20);
        p.getInventory().setArmorContents(null);

        // set sidebar
        GameSidebarManager.getInstance().setSidebar(p, SidebarType.MULTI_ARENA_LOBBY, null, true);
    }
}
