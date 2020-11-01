package com.andrei1058.stevesus.sidebar;

import com.andrei1058.spigot.sidebar.SidebarManager;
import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.locale.Locale;
import com.andrei1058.stevesus.api.server.ServerType;
import com.andrei1058.stevesus.config.MainConfig;
import com.andrei1058.stevesus.language.LanguageManager;
import com.andrei1058.stevesus.server.ServerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class GameSidebarManager {

    // prevent instantiation
    private GameSidebarManager() throws InstantiationException {
        this.handle = new SidebarManager();
        // register placeholders refresh task
        int placeholdersRefreshRate = ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.SIDEBAR_PLACEHOLDERS_REFRESH_INTERVAL);
        if (placeholdersRefreshRate <= 0) {
            SteveSus.getInstance().getLogger().warning("Sidebar PLACEHOLDERS refresh is disabled. Refresh interval is set to: " + placeholdersRefreshRate);
        } else {
            if (placeholdersRefreshRate < 20) {
                SteveSus.getInstance().getLogger().warning("Sidebar PLACEHOLDERS refresh interval is lower than 20 ticks (1 second).");
                SteveSus.getInstance().getLogger().warning("This is not an error but keep in mind it could affect server performance.");
                SteveSus.getInstance().getLogger().warning("Current refresh interval: every " + placeholdersRefreshRate + " ticks.");
            }
            Bukkit.getScheduler().runTaskTimer(SteveSus.getInstance(), () -> sidebarByPlayer.values().forEach(sidebar -> sidebar.getHandle().refreshPlaceholders()), 20L, placeholdersRefreshRate);
        }
        // register title refresh task
        int titleRefreshRate = ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.SIDEBAR_TITLE_REFRESH_INTERVAL);
        if (titleRefreshRate <= 0) {
            SteveSus.getInstance().getLogger().warning("Sidebar TITLE refresh is disabled. Refresh interval is set to: " + placeholdersRefreshRate);
        } else {
            if (titleRefreshRate < 20) {
                SteveSus.getInstance().getLogger().warning("Sidebar TITLE refresh interval is lower than 20 ticks (1 second).");
                SteveSus.getInstance().getLogger().warning("This is not an error but keep in mind it could affect server performance.");
                SteveSus.getInstance().getLogger().warning("Current refresh interval: every " + placeholdersRefreshRate + " ticks.");
            }
            Bukkit.getScheduler().runTaskTimer(SteveSus.getInstance(), () -> sidebarByPlayer.values().forEach(sidebar -> sidebar.getHandle().refreshTitle()), 20L, titleRefreshRate);
        }
    }

    // manager instance
    private static GameSidebarManager instance;

    // active scoreboards
    private final HashMap<UUID, GameSidebar> sidebarByPlayer = new HashMap<>();

    // sidebar API manager instance
    private final SidebarManager handle;

    /**
     * Set a player scoreboard.
     *
     * @param player target player.
     * @param type   scoreboard type.
     * @param arena  arena. If in a game.
     * @param delay  if scoreboard should be given with a bit of delay.
     */
    public void setSidebar(@NotNull Player player, @NotNull SidebarType type, @Nullable Arena arena, boolean delay) {
        // give lobby sb only in multi arena mode
        if (type == SidebarType.MULTI_ARENA_LOBBY && ServerManager.getINSTANCE().getServerType() != ServerType.MULTI_ARENA) {
            return;
        }
        // if player is offline return
        if (!player.isOnline()) {
            return;
        }

        // player language
        Locale playerLocale = LanguageManager.getINSTANCE().getLocale(player);
        // scoreboard lines
        List<String> content = type.getContent(playerLocale, arena);
        // previous sidebar
        GameSidebar previousSidebar = getPlayerSidebar(player.getUniqueId());

        // if sidebar lines are empty remove scoreboard
        if (content.isEmpty()) {
            if (previousSidebar != null) {
                previousSidebar.remove();
            }
            return;
        }

        // if does not have already a scoreboard
        if (previousSidebar == null) {
            if (delay) {
                // give with 5 ticks of delay
                SteveSus.newChain().delay(5).sync(() -> {
                    sidebarByPlayer.put(player.getUniqueId(), new GameSidebar(player, content, arena, playerLocale.getTimeZonedDateFormat()));
                }).execute();
            } else {
                // give normally
                sidebarByPlayer.put(player.getUniqueId(), new GameSidebar(player, content, arena, playerLocale.getTimeZonedDateFormat()));
            }
        } else {
            // if already owns a sidebar
            previousSidebar.setArena(arena);
            previousSidebar.setLines(content);
        }
    }

    /**
     * Get API sidebar manager.
     */
    public SidebarManager getHandle() {
        return handle;
    }

    /**
     * Remove a sidebar from list.
     */
    public void removeSidebar(Player player) {
        GameSidebar sidebar = getPlayerSidebar(player.getUniqueId());
        if (sidebar != null) {
            // remove sidebar from active list
            sidebarByPlayer.remove(player.getUniqueId());
            // remove player data from current sidebar
            sidebar.getHandle().remove(player.getUniqueId());
            // remove trace of player from other scoreboards
            sidebarByPlayer.values().forEach(sb -> sb.getHandle().playerListRemove(player.getName()));
        }
    }


    ////////////////////////////////////// STATIC

    /**
     * Get a player scoreboard.
     *
     * @param player sidebar owner.
     */
    @Nullable
    protected static GameSidebar getPlayerSidebar(UUID player) {
        return getInstance().sidebarByPlayer.get(player);
    }

    /**
     * Initialize sidebar manager at onEnable.
     */
    public static void onEnable() throws InstantiationException {
        if (instance != null) return;
        instance = new GameSidebarManager();
    }


    /**
     * Get sidebar manager.
     */
    public static GameSidebarManager getInstance() {
        return instance;
    }
}
