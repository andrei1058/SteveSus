package com.andrei1058.stevesus.server.multiarena.listener;

import com.andrei1058.dbi.operator.EqualsOperator;
import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.arena.ArenaHandler;
import com.andrei1058.stevesus.common.api.locale.CommonLocale;
import com.andrei1058.stevesus.common.command.CommonCmdManager;
import com.andrei1058.stevesus.common.database.DatabaseManager;
import com.andrei1058.stevesus.config.MainConfig;
import com.andrei1058.stevesus.language.LanguageManager;
import com.andrei1058.stevesus.server.ServerManager;
import com.andrei1058.stevesus.server.common.JoinCommonListener;
import com.andrei1058.stevesus.sidebar.GameSidebarManager;
import com.andrei1058.stevesus.sidebar.SidebarType;
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
        SteveSus.newChain().sync(() -> LanguageManager.getINSTANCE().setPlayerLocale(p, LanguageManager.getINSTANCE().getLocale(translation.getIsoCode()), false)).execute();
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
            if (ArenaHandler.getINSTANCE().getArenas().isEmpty()) {
                p.performCommand(CommonCmdManager.getINSTANCE().getMainCmd().getName());
            }
        }

        Bukkit.getScheduler().runTaskLater(SteveSus.getInstance(), () -> {
            // Hide new player to players and spectators, and vice versa
            // Players from lobby will remain visible
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (ArenaHandler.getINSTANCE().isInArena(online)) {
                    online.hidePlayer(SteveSus.getInstance(), p);
                    p.hidePlayer(SteveSus.getInstance(), online);
                } else {
                    online.showPlayer(SteveSus.getInstance(), p);
                    p.showPlayer(SteveSus.getInstance(), online);
                }
            }
        }, 14L);

        // Teleport to lobby location
        Location lobbyLocation = ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.MULTI_ARENA_SPAWN_LOC);
        if (lobbyLocation != null && lobbyLocation.getWorld() != null) {
            Bukkit.getScheduler().runTaskLater(SteveSus.getInstance(), () -> p.teleport(lobbyLocation, PlayerTeleportEvent.TeleportCause.PLUGIN), 2L);
        } else {
            Bukkit.getScheduler().runTaskLater(SteveSus.getInstance(), () -> p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN), 2L);
        }

        p.setExp(0);
        p.setHealthScale(20);
        p.setFoodLevel(20);
        p.getInventory().setArmorContents(null);

        // set sidebar
        GameSidebarManager.getInstance().setSidebar(p, SidebarType.MULTI_ARENA_LOBBY, null, true);
    }
}
