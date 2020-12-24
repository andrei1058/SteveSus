package com.andrei1058.stevesus.arena.gametask.wiring;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.GameListener;
import com.andrei1058.stevesus.api.arena.room.GameRoom;
import com.andrei1058.stevesus.api.arena.task.GameTask;
import com.andrei1058.stevesus.api.arena.task.TaskProvider;
import com.andrei1058.stevesus.api.locale.Locale;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.arena.ArenaManager;
import com.andrei1058.stevesus.arena.gametask.wiring.panel.WallPanel;
import com.andrei1058.stevesus.common.api.arena.GameState;
import com.andrei1058.stevesus.hook.glowing.GlowingManager;
import com.andrei1058.stevesus.language.LanguageManager;
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

    public FixWiringTask(List<WallPanel> panelList, int stages, String localName, Arena arena) {
        super(localName);
        wallPanels.addAll(panelList);
        Collections.shuffle(wallPanels);
        this.stages = stages;
        arena.registerGameListener(new WiringListener());
    }

    @Override
    public TaskProvider getHandler() {
        return FixWiringProvider.getInstance();
    }

    @Override
    public void onInterrupt(Player player, Arena arena) {
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
    public void assignToPlayer(Player player, Arena arena) {

        LinkedList<WallPanel> playerPanels = getLessUsedPanels(stages);
        if (playerPanels.isEmpty()) {
            SteveSus.getInstance().getLogger().warning("Cannot assign wring task to " + player.getName() + " on " + arena.getTemplateWorld() + "(" + arena.getGameId() + "). Bad wiring panels configuration.");
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
        Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer(player);
        if (arena == null) return;
        if (getCurrentStage(player) != getTotalStages(player)) {
            WallPanel currentPanel = playerAssignedPanels.get(player.getUniqueId()).removeFirst();
            currentPanel.stopGlowing(player);
            // mark done or
            if (playerAssignedPanels.get(player.getUniqueId()).isEmpty()) {
                player.playSound(player.getLocation(), Sound.ENTITY_CAT_PURREOW, 1, 1);
                arena.refreshTaskMeter();
                arena.getGameEndConditions().tickGameEndConditions(arena);
            } else {
                // or assign next
                WallPanel panel = playerAssignedPanels.get(player.getUniqueId()).getFirst();
                panel.startGlowing(player);
                GameRoom room = arena.getRoom(panel.getItemFrame().getLocation());
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
        public void onEntityPunch(Arena arena, Player player, Entity entity) {
            tryOpenGUI(player, entity, arena);
        }

        @Override
        public void onPlayerInteractEntity(Arena arena, Player player, Entity entity) {
            tryOpenGUI(player, entity, arena);
        }

        @Override
        public void onGameStateChange(Arena arena, GameState oldState, GameState newState) {
            if (newState == GameState.IN_GAME) {
                wallPanels.forEach(panel -> {
                    arena.getPlayers().forEach(player -> {
                        if (!(hasTask(player) && GlowingManager.isGlowing(panel.getItemFrame(), player))) {
                            panel.getHologram().hide(player);
                        }
                    });
                    panel.getHologram().show();
                });
            }
        }

        @Override
        public void onPlayerJoin(Arena arena, Player player) {
            if (arena.getGameState() == GameState.IN_GAME) {
                wallPanels.forEach(panel -> panel.getHologram().hide(player));
            } else {
                // hide existing glowing
                for (WallPanel wallPanel : wallPanels) {
                    GlowingManager.getInstance().removeGlowing(wallPanel.getItemFrame(), player);
                }
            }
        }

        @Override
        public void onInventoryClose(Arena arena, Player player, Inventory inventory) {
            player.setItemOnCursor(null);
            currentlyOpenPanel.remove(player.getUniqueId());
        }
    }

    private void tryOpenGUI(Player player, Entity entity, Arena arena) {
        if (!arena.isTasksAllowedATM()) return;
        if (hasTask(player)) {
            // should prevent called twice
            if (currentlyOpenPanel.contains(player.getUniqueId())) return;
            if (getCurrentStage(player) != getTotalStages(player)) {
                if (arena.getCamHandler() != null && arena.getCamHandler().isOnCam(player, arena)) return;
                WallPanel panel = playerAssignedPanels.get(player.getUniqueId()).getFirst();
                if (panel != null && panel.getItemFrame().equals(entity)) {
                    panel.startFixingPanel(player, this);
                    currentlyOpenPanel.add(player.getUniqueId());
                }
            }
        }
    }
}
