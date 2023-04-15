package dev.andrei1058.game.sidebar;

import com.andrei1058.spigot.sidebar.PlaceholderProvider;
import com.andrei1058.spigot.sidebar.Sidebar;
import com.andrei1058.spigot.sidebar.SidebarLine;
import com.andrei1058.spigot.sidebar.SidebarLineAnimated;
import dev.andrei1058.game.SteveSus;
import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.arena.room.GameRoom;
import dev.andrei1058.game.api.arena.task.GameTask;
import dev.andrei1058.game.api.locale.Message;
import dev.andrei1058.game.common.api.arena.GameState;
import dev.andrei1058.game.common.hook.HookManager;
import dev.andrei1058.game.common.stats.StatsManager;
import dev.andrei1058.game.language.LanguageManager;
import dev.andrei1058.game.server.ServerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class GameSidebar {

    // sidebar instance
    private final Sidebar handle;
    // player owner
    private final Player player;
    // player date format
    private SimpleDateFormat dateFormat;
    // player arena. Nullable.
    private GameArena gameArena;
    private String taskFormatCache = null;
    private boolean maskData = false;

    /**
     * Create a sidebar instance.
     *
     * @param player  target player.
     * @param content sidebar lines.
     * @param gameArena   arena if target is in a game.
     */
    protected GameSidebar(@NotNull Player player, @NotNull List<String> content, @Nullable GameArena gameArena, @NotNull SimpleDateFormat dateFormat) {
        this.player = player;
        this.gameArena = gameArena;
        this.dateFormat = dateFormat;

        List<PlaceholderProvider> somePlaceholders = new ArrayList<>();
        // register some placeholders
        somePlaceholders.add(new PlaceholderProvider("{date}", () -> getDateFormat().format(new Date(Instant.now().toEpochMilli()))));
        somePlaceholders.add(new PlaceholderProvider("{player}", getPlayer()::getDisplayName));
        somePlaceholders.add(new PlaceholderProvider("{money}", () -> String.valueOf(HookManager.getInstance().getVaultEconHook().getBalance(getPlayer()))));
        somePlaceholders.add(new PlaceholderProvider("{on}", () -> {
            if (getArena() == null) {
                return String.valueOf(Bukkit.getOnlinePlayers().size());
            } else {
                return String.valueOf(getArena().getCurrentPlayers());
            }
        }));
        somePlaceholders.add(new PlaceholderProvider("{room}", () -> {
            if (getArena() == null) {
                return "";
            }
            GameRoom room = getArena().getPlayerRoom(getPlayer());
            if (room == null) {
                return LanguageManager.getINSTANCE().getMsg(getPlayer(), Message.GAME_ROOM_NO_NAME);
            }
            return room.getDisplayName(LanguageManager.getINSTANCE().getLocale(getPlayer()));
        }));
        somePlaceholders.add(new PlaceholderProvider("{tasks_long}", ()->{
            if (getArena() == null){
                return "0";
            } else {
                return String.valueOf(getArena().getLiveSettings().getLongTasks().getCurrentValue());
            }
        }));
        somePlaceholders.add(new PlaceholderProvider("{tasks_short}", ()->{
            if (getArena() == null){
                return "0";
            } else {
                return String.valueOf(getArena().getLiveSettings().getShortTasks().getCurrentValue());
            }
        }));
        somePlaceholders.add(new PlaceholderProvider("{tasks_common}", ()->{
            if (getArena() == null){
                return "0";
            } else {
                return String.valueOf(getArena().getLiveSettings().getCommonTasks().getCurrentValue());
            }
        }));
        somePlaceholders.add(new PlaceholderProvider("{tasks_visual}", ()->{
            if (getArena() == null){
                return "false";
            } else {
                return String.valueOf(getArena().getLiveSettings().isVisualTasksEnabled());
            }
        }));

        // create sidebar
        handle = GameSidebarManager.getInstance().getHandle().createSidebar(new SidebarLine() {
            @NotNull
            @Override
            public String getLine() {
                return "temp";
            }
        }, Collections.emptyList(), somePlaceholders);

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
        if (getArena() != null) {
            if (LanguageManager.getINSTANCE().getLocale(getPlayer()).hasPath(Message.GAME_TASK_SCOREBOARD_FORMAT.toString() + "-" + getArena().getTemplateWorld())) {
                this.taskFormatCache = LanguageManager.getINSTANCE().getMsg(getPlayer(), Message.GAME_TASK_SCOREBOARD_FORMAT.toString() + "-" + getArena().getTemplateWorld());
            } else if (LanguageManager.getINSTANCE().getDefaultLocale().hasPath(Message.GAME_TASK_SCOREBOARD_FORMAT.toString() + "-" + getArena().getTemplateWorld())) {
                this.taskFormatCache = LanguageManager.getINSTANCE().getDefaultLocale().getMsg(getPlayer(), Message.GAME_TASK_SCOREBOARD_FORMAT.toString() + "-" + getArena().getTemplateWorld());
            } else {
                this.taskFormatCache = LanguageManager.getINSTANCE().getMsg(getPlayer(), Message.GAME_TASK_SCOREBOARD_FORMAT.toString());
            }
        }

        // remove previous lines
        while (getHandle().linesAmount() > 0) {
            getHandle().removeLine(0);
        }

        // Remove previous placeholders
        List<String> placeholdersToRemove = new LinkedList<>();
        Arrays.asList("{spectating}", "{countdown}").forEach(toUnregister -> handle.getPlaceholders().forEach(placeholder -> {
            if (placeholder.getPlaceholder().equals(toUnregister)) {
                placeholdersToRemove.add(placeholder.getPlaceholder());
            } else if (placeholder.getPlaceholder().startsWith("{task_")) {
                placeholdersToRemove.add(placeholder.getPlaceholder());
            }
        }));
        placeholdersToRemove.forEach(handle::removePlaceholder);


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
        if (gameArena != null) {
            handle.addPlaceholder(new PlaceholderProvider("{spectating}", () -> String.valueOf(gameArena.getCurrentSpectators())));
            if (gameArena.getGameState() == GameState.STARTING || gameArena.getGameState() == GameState.ENDING) {
                handle.addPlaceholder(new PlaceholderProvider("{countdown}", () -> String.valueOf(gameArena.getCountdown())));
            }
        }

        List<GameTask> playerTasks = null;
        if (gameArena != null && gameArena.getGameState() == GameState.IN_GAME) {
            playerTasks = gameArena.getLoadedGameTasks().stream().filter(task -> task.hasTask(player)).collect(Collectors.toList());
        }


        // Set lines
        for (String line : content) {
            line = ChatColor.translateAlternateColorCodes('&', line);
            if (gameArena != null) {
                if (line.contains("{task}")) {
                    GameTask currentTask = playerTasks == null ? null : (playerTasks.isEmpty() ? null : playerTasks.remove(0));
                    if (currentTask != null) {
                        final String taskName = ChatColor.stripColor(LanguageManager.getINSTANCE().getMsg(player, Message.GAME_TASK_NAME_PATH_.toString() + currentTask.getHandler().getIdentifier()));
                        PlaceholderProvider taskPlaceholder = new PlaceholderProvider("{task_" + currentTask.getLocalName() + "}", () -> {
                            int currentStage;
                            int totalStages;
                            boolean isDone = (totalStages = currentTask.getTotalStages(player)) == (currentStage = currentTask.getCurrentStage(player));
                            return taskFormatCache.replace("{task_name}", isDone ? ChatColor.STRIKETHROUGH + taskName : isMaskData() ? ChatColor.MAGIC + taskName : taskName).replace("{task_stage}", String.valueOf(currentStage)).replace("{task_stages}", String.valueOf(totalStages));
                        });
                        handle.addPlaceholder(taskPlaceholder);
                        handle.addLine(new SidebarLine() {
                            @NotNull
                            @Override
                            public String getLine() {
                                return "{task_" + currentTask.getLocalName() + "}";
                            }
                        });
                    }
                    continue;
                }
                line = line.replace("{template}", gameArena.getTemplateWorld()).replace("{name}", gameArena.getDisplayName())
                        .replace("{status}", gameArena.getDisplayState(player))
                        .replace("{max}", String.valueOf(gameArena.getMaxPlayers())).replace("{spectating}", String.valueOf(gameArena.getCurrentSpectators()))
                        .replace("{game_tag}", gameArena.getTag()).replace("{game_id}", String.valueOf(gameArena.getGameId()));
            }
            line = line.replace("{player_raw}", player.getName())
                    .replace("{server_name}", ServerManager.getINSTANCE().getServerName())
                    .replace("{version}", SteveSus.getInstance().getDescription().getVersion());
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

    public void setMaskData(boolean maskData) {
        this.maskData = maskData;
    }

    public boolean isMaskData() {
        return maskData;
    }

    public GameArena getArena() {
        return gameArena;
    }

    public Player getPlayer() {
        return player;
    }

    public SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(SimpleDateFormat dateFormat) {
        this.dateFormat = dateFormat;
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
    public void setArena(GameArena gameArena) {
        this.gameArena = gameArena;
    }

    /**
     * Delete current scoreboard.
     */
    public void remove() {
        GameSidebarManager.getInstance().removeSidebar(player);
    }

    public void hidePlayerName(Player player) {
        this.handle.playerListHideNameTag(player);
    }
}
