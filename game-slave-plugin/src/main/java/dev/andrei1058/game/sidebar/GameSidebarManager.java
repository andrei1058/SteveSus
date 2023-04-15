package dev.andrei1058.game.sidebar;

import com.andrei1058.spigot.sidebar.SidebarLine;
import com.andrei1058.spigot.sidebar.SidebarManager;
import dev.andrei1058.game.SteveSus;
import dev.andrei1058.game.api.arena.Arena;
import dev.andrei1058.game.api.arena.team.PlayerColorAssigner;
import dev.andrei1058.game.api.locale.Locale;
import dev.andrei1058.game.api.locale.Message;
import dev.andrei1058.game.api.server.ServerType;
import dev.andrei1058.game.common.api.arena.GameState;
import dev.andrei1058.game.config.MainConfig;
import dev.andrei1058.game.language.LanguageManager;
import dev.andrei1058.game.server.ServerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

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
        if (type == SidebarType.MULTI_ARENA_LOBBY) {
            if (ServerManager.getINSTANCE().getServerType() != ServerType.MULTI_ARENA) {
                return;
            }
            arena = null;
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
        if (content == null || content.isEmpty()) {
            if (previousSidebar != null) {
                previousSidebar.remove();
            }
            return;
        }

        // if does not have already a scoreboard
        if (previousSidebar == null) {
            if (delay) {
                // give with 5 ticks of delay
                @Nullable Arena finalArena = arena;
                SteveSus.newChain().delay(20).sync(() -> {
                    GameSidebar sidebar = new GameSidebar(player, content, finalArena, playerLocale.getTimeZonedDateFormat());
                    sidebarByPlayer.put(player.getUniqueId(), sidebar);
                    if (finalArena != null && finalArena.getGameState() != GameState.IN_GAME) {
                        sidebar.getHandle().playerListClear();
                    }
                }).execute();
            } else {
                // give normally
                GameSidebar sidebar = new GameSidebar(player, content, arena, playerLocale.getTimeZonedDateFormat());
                sidebarByPlayer.put(player.getUniqueId(), sidebar);
                if (arena != null && arena.getGameState() == GameState.IN_GAME) {
                    for (Player inGame : arena.getPlayers()) {
                        sidebar.hidePlayerName(inGame);
                    }
                } else {
                    sidebar.getHandle().playerListClear();
                }
            }
        } else {
            // if already owns a sidebar
            previousSidebar.setArena(arena);
            previousSidebar.setLines(content);
            if (arena != null && arena.getGameState() == GameState.IN_GAME) {
                for (Player inGame : arena.getPlayers()) {
                    previousSidebar.hidePlayerName(inGame);
                }
            } else {
                previousSidebar.getHandle().playerListClear();
            }
        }
    }

    public static void hidePlayerNames(Arena arena) {
        if (arena.getGameState() != GameState.IN_GAME) return;
        SteveSus.newChain().delay(20).sync(() -> {
            for (GameSidebar sidebar : getInstance().getSidebars(arena)) {
                for (Player player : arena.getPlayers()) {
                    PlayerColorAssigner<?> color = arena.getPlayerColorAssigner();

                    String displayColor;
                    if (color == null) {
                        displayColor = "";
                    } else {
                        PlayerColorAssigner.PlayerColor playerColor = color.getPlayerColor(player);
                        if (playerColor == null) {
                            displayColor = "";
                        } else {
                            displayColor = playerColor.getDisplayColor(player);
                        }
                    }
                    final String prefix = LanguageManager.getINSTANCE().getMsg(sidebar.getPlayer(), Message.TAB_LIST_GENERIC_PREFIX).replace("{display_color}", displayColor);
                    final String suffix = LanguageManager.getINSTANCE().getMsg(sidebar.getPlayer(), Message.TAB_LIST_GENERIC_SUGGIX).replace("{display_color}", displayColor);
                    sidebar.getHandle().playerListCreate(player,
                            new SidebarLine() {
                                @NotNull
                                @Override
                                public String getLine() {
                                    return prefix;
                                }
                            },
                            new SidebarLine() {
                                @NotNull
                                @Override
                                public String getLine() {
                                    return suffix;
                                }
                            }, true);

                    sidebar.getHandle().playerListHideNameTag(player);
                }
            }
        }).execute();
    }

    public static void spoilGhostInTab(Player ghost, Arena arena) {
        if (arena.getGameState() != GameState.IN_GAME) return;
        PlayerColorAssigner<?> color = arena.getPlayerColorAssigner();

        String displayColor;
        if (color == null) {
            displayColor = "";
        } else {
            PlayerColorAssigner.PlayerColor playerColor = color.getPlayerColor(ghost);
            if (playerColor == null) {
                displayColor = "";
            } else {
                displayColor = playerColor.getDisplayColor(ghost);
            }
        }
        for (GameSidebar sidebar : getInstance().getSidebars(arena)) {
            final String prefix = LanguageManager.getINSTANCE().getMsg(sidebar.getPlayer(), Message.TAB_LIST_GHOST_PREFIX).replace("{display_color}", displayColor);
            final String suffix = LanguageManager.getINSTANCE().getMsg(sidebar.getPlayer(), Message.TAB_LIST_GHOST_SUFFIX).replace("{display_color}", displayColor);
            sidebar.getHandle().playerListCreate(ghost,
                    new SidebarLine() {
                        @NotNull
                        @Override
                        public String getLine() {
                            return prefix;
                        }
                    },
                    new SidebarLine() {
                        @NotNull
                        @Override
                        public String getLine() {
                            return suffix;
                        }
                    }, true);
            sidebar.hidePlayerName(ghost);
            sidebar.getHandle().playerListRefreshAnimation();
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
        GameSidebar sidebar = sidebarByPlayer.remove(player.getUniqueId());
        if (sidebar != null) {
            // remove sidebar from active list
            //sidebarByPlayer.remove(player.getUniqueId());;
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

    public Set<GameSidebar> getSidebars(Arena arena) {
        return sidebarByPlayer.values().stream().filter(sb -> sb.getArena() != null && sb.getArena().equals(arena)).collect(Collectors.toSet());
    }

    public void hidePlayerName(Player receiver, Player player) {
        GameSidebar sidebar = sidebarByPlayer.get(receiver.getUniqueId());
        if (sidebar != null) {
            sidebar.getHandle().playerListHideNameTag(player);
        }
    }
}
