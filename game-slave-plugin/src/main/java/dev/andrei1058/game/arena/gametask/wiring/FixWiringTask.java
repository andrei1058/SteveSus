package dev.andrei1058.game.arena.gametask.wiring;

import dev.andrei1058.game.SteveSus;
import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.arena.GameListener;
import dev.andrei1058.game.api.arena.room.GameRoom;
import dev.andrei1058.game.api.arena.task.GameTask;
import dev.andrei1058.game.api.arena.task.TaskProvider;
import dev.andrei1058.game.api.event.PlayerTaskDoneEvent;
import dev.andrei1058.game.api.locale.Locale;
import dev.andrei1058.game.api.locale.Message;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.arena.gametask.wiring.panel.WallPanel;
import dev.andrei1058.game.common.api.arena.GameState;
import dev.andrei1058.game.hook.glowing.GlowingManager;
import dev.andrei1058.game.language.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.*;
import java.util.stream.Collectors;

public class FixWiringTask extends GameTask {

    private final List<WallPanel> wallPanels = new ArrayList<>();
    private final int stages;

    // panels are removed when gets fixed
    private final HashMap<UUID, LinkedList<WallPanel>> playerAssignedPanels = new HashMap<>();
    private final LinkedList<UUID> currentlyOpenPanel = new LinkedList<>();

    public FixWiringTask(List<WallPanel> panelList, int stages, String localName, GameArena gameArena) {
        super(localName);
        wallPanels.addAll(panelList);
        Collections.shuffle(wallPanels);
        this.stages = stages;
        gameArena.registerGameListener(new WiringListener());
    }

    @Override
    public TaskProvider getHandler() {
        return FixWiringProvider.getInstance();
    }

    @Override
    public void onInterrupt(Player player, GameArena gameArena) {
        if (isDoingTask(player)) {
            player.closeInventory();
        }
    }

    @Override
    public int getCurrentStage(Player player) {
        return stages - (playerAssignedPanels.containsKey(player.getUniqueId()) ? playerAssignedPanels.get(player.getUniqueId()).size() : 0);
    }

    @Override
    public int getCurrentStage(UUID player) {
        return stages - (playerAssignedPanels.containsKey(player) ? playerAssignedPanels.get(player).size() : 0);
    }

    @Override
    public int getTotalStages(Player player) {
        return stages;
    }

    @Override
    public int getTotalStages(UUID player) {
        return stages;
    }

    @Override
    public void assignToPlayer(Player player, GameArena gameArena) {

        LinkedList<WallPanel> playerPanels = getLessUsedPanels(stages);
        if (playerPanels.isEmpty()) {
            SteveSus.getInstance().getLogger().warning("Cannot assign wring task to " + player.getName() + " on " + gameArena.getTemplateWorld() + "(" + gameArena.getGameId() + "). Bad wiring panels configuration.");
            return;
        }
        playerAssignedPanels.put(player.getUniqueId(), playerPanels);
        playerPanels.getFirst().startGlowing(player);

    }

    @Override
    public Set<UUID> getAssignedPlayers() {
        return Collections.unmodifiableSet(playerAssignedPanels.keySet());
    }

    @Override
    public boolean hasTask(Player player) {
        return playerAssignedPanels.containsKey(player.getUniqueId());
    }

    @Override
    public boolean isDoingTask(Player player) {
        return currentlyOpenPanel.contains(player.getUniqueId());
    }

    @Override
    public void enableIndicators() {
        playerAssignedPanels.forEach((player, panels) -> {
            if (panels.size() != 0) {
                panels.getFirst().startGlowing(player);
            }
        });
    }

    @Override
    public void disableIndicators() {
        playerAssignedPanels.forEach((player, panels) -> {
            if (panels.size() != 0) {
                panels.getFirst().stopGlowing(player);
            }
        });
    }

    public void fixedOneAndGiveNext(Player player) {
        GameArena gameArena = ArenaManager.getINSTANCE().getArenaByPlayer(player);
        if (gameArena == null) return;
        if (getCurrentStage(player) != getTotalStages(player)) {
            WallPanel currentPanel = playerAssignedPanels.get(player.getUniqueId()).removeFirst();
            currentPanel.stopGlowing(player);
            // mark done or
            if (playerAssignedPanels.get(player.getUniqueId()).isEmpty()) {
                player.playSound(player.getLocation(), Sound.ENTITY_CAT_PURREOW, 1, 1);
                gameArena.refreshTaskMeter();
                gameArena.getGameEndConditions().tickGameEndConditions(gameArena);

                PlayerTaskDoneEvent taskDoneEvent = new PlayerTaskDoneEvent(gameArena, this, player);
                Bukkit.getPluginManager().callEvent(taskDoneEvent);
            } else {
                // or assign next
                WallPanel panel = playerAssignedPanels.get(player.getUniqueId()).getFirst();
                panel.startGlowing(player);
                GameRoom room = gameArena.getRoom(panel.getItemFrame().getLocation());
                Locale playerLang = LanguageManager.getINSTANCE().getLocale(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                player.sendMessage(playerLang.getMsg(player, FixWiringProvider.NEXT_PANEL).replace("{room}", (room == null ? playerLang.getMsg(null, Message.GAME_ROOM_NO_NAME) : room.getDisplayName(playerLang))));
            }
        }
    }

    /**
     * Some panels cannot be first and that's why flags were introduced.
     */
    public enum PanelFlag {
        NEVER_FIRST(ChatColor.AQUA + "Never First", 2),
        NEVER_LAST(ChatColor.DARK_GREEN + "Never Last", 1),
        REGULAR(ChatColor.GOLD + "Regular", 0),
        ALWAYS_FIRST(ChatColor.GREEN + "Always First", 3),
        ALWAYS_LAST(ChatColor.LIGHT_PURPLE + "Always Last", 4);

        private final String description;
        private final int weight;

        PanelFlag(String description, int weight) {
            this.description = description;
            this.weight = weight;
        }

        public String getDescription() {
            return description;
        }

        /**
         * Used for setup purposes.
         */
        PanelFlag next() {
            int next = weight + 1;
            return Arrays.stream(values()).filter(flag -> flag.weight == next).findAny().orElse(REGULAR);
        }
    }


    private LinkedList<WallPanel> getLessUsedPanels(int stages) {

        LinkedList<WallPanel> picked = new LinkedList<>();

        // initialize options with first panel filters
        List<WallPanel> options = wallPanels.stream().filter(panel -> !(panel.getFlag() == PanelFlag.NEVER_FIRST || panel.getFlag() == PanelFlag.ALWAYS_LAST)).collect(Collectors.toList());

        // comparison variables
        WallPanel current;

        // pick first panel
        if (options.isEmpty()) {
            SteveSus.getInstance().getLogger().warning("Tried to assign FIRST wiring panel but they seem to be all assigned as NEVER_FIRST or ALWAYS_LAST");
        } else {
            current = options.remove(0);

            // pick first
            for (WallPanel p : options) {
                if (current.getAssignments() >= p.getAssignments()) {
                    current = p;
                }
            }
            current.increaseAssignments();
            picked.add(current);
        }

        // pick middle panels
        options = wallPanels.stream().filter(panel -> !(panel.getFlag() == PanelFlag.ALWAYS_FIRST || panel.getFlag() == PanelFlag.ALWAYS_LAST) && !picked.contains(panel)).collect(Collectors.toList());
        if (options.isEmpty()) {
            SteveSus.getInstance().getLogger().warning("Tried to assign MIDDLE wiring panels but they seem to be all assigned as ALWAYS_FIRST or ALWAYS_LAST");
        } else {
            // foreach stage to be added
            for (int x = 1; x < stages - 1; x++) {
                current = options.get(0);
                // check usages
                for (WallPanel p : options) {
                    if (current.getAssignments() >= p.getAssignments()) {
                        current = p;
                    }
                }
                picked.remove(current);
                picked.add(current);
                current.increaseAssignments();
            }
        }

        // pick last
        options = wallPanels.stream().filter(panel -> !(panel.getFlag() == PanelFlag.ALWAYS_FIRST || panel.getFlag() == PanelFlag.NEVER_LAST) && !picked.contains(panel)).collect(Collectors.toList());
        if (options.isEmpty()) {
            SteveSus.getInstance().getLogger().warning("Tried to assign LAST wiring panel but they seem to be all assigned as ALWAYS_FIRST or NEVER_LAST");
        } else {
            current = options.remove(0);

            for (WallPanel p : options) {
                if (current.getAssignments() >= p.getAssignments()) {
                    current = p;
                }
            }
            current.increaseAssignments();
            picked.add(current);
        }
        return picked;
    }

    private class WiringListener implements GameListener {
        @Override
        public void onEntityPunch(GameArena gameArena, Player player, Entity entity) {
            tryOpenGUI(player, entity, gameArena);
        }

        @Override
        public void onPlayerInteractEntity(GameArena gameArena, Player player, Entity entity) {
            tryOpenGUI(player, entity, gameArena);
        }

        @Override
        public void onGameStateChange(GameArena gameArena, GameState oldState, GameState newState) {
            if (newState == GameState.IN_GAME) {
                wallPanels.forEach(panel -> {
                    gameArena.getPlayers().forEach(player -> {
                        if (!(hasTask(player) && GlowingManager.isGlowing(panel.getItemFrame(), player))) {
                            if (panel.getHologram() != null) {
                                panel.getHologram().hide(player);
                            }
                        }
                    });
                    if (panel.getHologram() != null) {
                        panel.getHologram().show();
                    }
                });
            }
        }

        @Override
        public void onPlayerJoin(GameArena gameArena, Player player) {
            if (gameArena.getGameState() == GameState.IN_GAME) {
                wallPanels.forEach(panel -> {
                    if (panel.getHologram() != null) {
                        panel.getHologram().hide(player);
                    }
                });
            } else {
                // hide existing glowing
                for (WallPanel wallPanel : wallPanels) {
                    GlowingManager.getInstance().removeGlowing(wallPanel.getItemFrame(), player);
                }
            }
        }

        @Override
        public void onInventoryClose(GameArena gameArena, Player player, Inventory inventory) {
            player.setItemOnCursor(null);
            currentlyOpenPanel.remove(player.getUniqueId());
        }
    }

    private void tryOpenGUI(Player player, Entity entity, GameArena gameArena) {
        if (!gameArena.isTasksAllowedATM()) return;
        if (hasTask(player)) {
            // should prevent called twice
            if (currentlyOpenPanel.contains(player.getUniqueId())) return;
            if (getCurrentStage(player) != getTotalStages(player)) {
                if (gameArena.getCamHandler() != null && gameArena.getCamHandler().isOnCam(player, gameArena)) return;
                WallPanel panel = playerAssignedPanels.get(player.getUniqueId()).getFirst();
                if (panel != null && panel.getItemFrame() != null && panel.getItemFrame().equals(entity)) {
                    panel.startFixingPanel(player, this);
                    currentlyOpenPanel.add(player.getUniqueId());
                }
            }
        }
    }
}
