package com.andrei1058.stevesus.sidebar;

import com.andrei1058.spigot.sidebar.PlaceholderProvider;
import com.andrei1058.spigot.sidebar.Sidebar;
import com.andrei1058.spigot.sidebar.SidebarLine;
import com.andrei1058.spigot.sidebar.SidebarLineAnimated;
import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.common.api.arena.GameState;
import com.andrei1058.stevesus.common.hook.HookManager;
import com.andrei1058.stevesus.common.stats.StatsManager;
import com.andrei1058.stevesus.server.ServerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

public class GameSidebar {

    // sidebar instance
    private Sidebar handle;
    // player owner
    private Player player;
    // player date format
    private SimpleDateFormat dateFormat;
    // player arena. Nullable.
    private Arena arena;

    /**
     * Create a sidebar instance.
     *
     * @param player  target player.
     * @param content sidebar lines.
     * @param arena   arena if target is in a game.
     */
    protected GameSidebar(@NotNull Player player, @NotNull List<String> content, @Nullable Arena arena, @NotNull SimpleDateFormat dateFormat) {
        this.player = player;
        this.arena = arena;
        this.dateFormat = dateFormat;

        // create sidebar
        handle = GameSidebarManager.getInstance().getHandle().createSidebar(null, Collections.emptyList(), Collections.emptyList());
        // set lines
        setLines(content);
        // apply sidebar
        handle.apply(player);
        SteveSus.debug("Gave player scoreboard: " + player.getName());
    }

    /**
     * Set and parse scoreboard lines.
     */
    public void setLines(@NotNull List<String> content) {
        // remove previous lines
        while (handle.linesAmount() > 0) {
            handle.removeLine(0);
        }

        // Remove previous placeholders
        List<String> placeholdersToRemove = new LinkedList<>();
        Arrays.asList("{on}", "{spectating}", "{countdown}").forEach(toUnregister -> {
            handle.getPlaceholders().forEach(placeholder -> {
                if (placeholder.getPlaceholder().equals(toUnregister)) {
                    placeholdersToRemove.add(placeholder.getPlaceholder());
                }
            });
        });
        placeholdersToRemove.forEach(placeholder -> handle.removePlaceholder(placeholder));


        // Set the title
        String[] title = ChatColor.translateAlternateColorCodes('&', content.remove(0)).split("[\\n,]");
        if (title.length == 1) {
            handle.setTitle(new SidebarLine() {
                @NotNull
                @Override
                public String getLine() {
                    return title[0];
                }
            });
        } else {
            handle.setTitle(new SidebarLineAnimated(title));
        }

        // Register refreshable placeholders
        handle.addPlaceholder(new PlaceholderProvider("{date}", () -> dateFormat.format(new Date(Instant.now().toEpochMilli()))));
        if (arena == null) {
            handle.addPlaceholder(new PlaceholderProvider("{on}", () -> String.valueOf(Bukkit.getOnlinePlayers().size())));
        } else {
            handle.addPlaceholder(new PlaceholderProvider("{on}", () -> String.valueOf(arena.getCurrentPlayers())));
            handle.addPlaceholder(new PlaceholderProvider("{spectating}", () -> String.valueOf(arena.getCurrentSpectators())));
            if (arena.getGameState() == GameState.STARTING || arena.getGameState() == GameState.ENDING) {
                handle.addPlaceholder(new PlaceholderProvider("{countdown}", () -> String.valueOf(arena.getCountdown())));
            }
        }


        // Set lines
        for (String line : content) {
            line = ChatColor.translateAlternateColorCodes('&', line);
            if (arena != null) {
                line = line.replace("{template}", arena.getTemplateWorld()).replace("{name}", arena.getDisplayName())
                        .replace("{status}", arena.getDisplayState(player)).replace("{on}", String.valueOf(arena.getCurrentPlayers()))
                        .replace("{max}", String.valueOf(arena.getMaxPlayers())).replace("{spectating}", String.valueOf(arena.getCurrentSpectators()))
                        .replace("{game_tag}", arena.getTag()).replace("{game_id}", String.valueOf(arena.getGameId()));
            }
            line = line.replace("{player}", player.getDisplayName()).replace("{player_raw}", player.getName())
                    .replace("{money}", String.valueOf(HookManager.getInstance().getVaultEconHook().getBalance(player)))
                    .replace("{server_name}", ServerManager.getINSTANCE().getServerName());
            line = StatsManager.getINSTANCE().replaceStats(player, line);

            // Add the line to the sidebar
            String finalTemp = line;
            SidebarLine sidebarLine = new SidebarLine() {
                @NotNull
                @Override
                public String getLine() {
                    return finalTemp;
                }
            };
            handle.addLine(sidebarLine);
        }
    }

    /**
     * Get sidebar handler.
     */
    public Sidebar getHandle() {
        return handle;
    }

    /**
     * Set arena.
     */
    public void setArena(Arena arena) {
        this.arena = arena;
    }

    /**
     * Delete current scoreboard.
     */
    public void remove() {
        GameSidebarManager.getInstance().removeSidebar(player);
    }
}
