package com.andrei1058.stevesus.arena.gametask.emptygarbage;

import co.aikar.taskchain.TaskChain;
import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.GameListener;
import com.andrei1058.stevesus.api.arena.room.GameRoom;
import com.andrei1058.stevesus.api.arena.task.GameTask;
import com.andrei1058.stevesus.api.arena.task.TaskProvider;
import com.andrei1058.stevesus.api.locale.Locale;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.arena.ArenaManager;
import com.andrei1058.stevesus.common.api.locale.CommonLocale;
import com.andrei1058.stevesus.language.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class EmptyGarbageTask extends GameTask {

    private final HashMap<UUID, LinkedList<WallLever>> assignedLevers = new HashMap<>();
    private final HashMap<UUID, Integer> playerTotalStages = new HashMap<>();
    private final LinkedList<WallLever> wallLevers = new LinkedList<>();
    private final LinkedList<UUID> currentlyOpenPanel = new LinkedList<>();
    private final int stages;

    public EmptyGarbageTask(String localName, Arena arena, List<WallLever> wallLevers, int stages) {
        super(localName);
        this.wallLevers.addAll(wallLevers);
        this.stages = stages;

        arena.registerGameListener(new GameListener() {
            @Override
            public void onPlayerInteractEntity(Arena arena, Player player, Entity entity) {
                tryOpenGUI(player, entity, arena);
            }

            @Override
            public void onInventoryClose(Arena arena, Player player, Inventory inventory) {
                currentlyOpenPanel.remove(player.getUniqueId());
            }
        });
    }


    @Override
    public TaskProvider getHandler() {
        return EmptyGarbageTaskProvider.getInstance();
    }

    @Override
    public void onInterrupt(Player player, Arena arena) {
        if (hasTask(player)){
            player.closeInventory();
        }
    }

    @Override
    public int getCurrentStage(Player player) {
        if (assignedLevers.containsKey(player.getUniqueId())) {
            return getTotalStages(player) - assignedLevers.get(player.getUniqueId()).size();
        }
        return 0;
    }

    @Override
    public int getCurrentStage(UUID player) {
        if (assignedLevers.containsKey(player)) {
            return getTotalStages(player) - assignedLevers.get(player).size();
        }
        return 0;
    }

    @Override
    public int getTotalStages(Player player) {
        return playerTotalStages.getOrDefault(player.getUniqueId(), 0);
    }

    @Override
    public int getTotalStages(UUID player) {
        return playerTotalStages.getOrDefault(player, 0);
    }

    @Override
    public void assignToPlayer(Player player, Arena arena) {
        if (hasTask(player)) return;
        //todo
        LinkedList<WallLever> assigned = OrderPriority.getLessUsedPanels(stages, this);
        if (assigned.isEmpty()) {
            SteveSus.getInstance().getLogger().warning("Cannot assign clean garbage task to " + player.getName() + " on " + arena.getTemplateWorld() + "(" + arena.getGameId() + "). Bad configuration.");
            return;
        }
        assignedLevers.put(player.getUniqueId(), assigned);
        assigned.getFirst().getGlowingBox().startGlowing(player);
        playerTotalStages.put(player.getUniqueId(), assigned.size());
    }

    @Override
    public Set<UUID> getAssignedPlayers() {
        return assignedLevers.keySet();
    }

    @Override
    public boolean hasTask(Player player) {
        return assignedLevers.containsKey(player.getUniqueId());
    }

    @Override
    public boolean isDoingTask(Player player) {
        return currentlyOpenPanel.contains(player.getUniqueId());
    }

    @Override
    public void enableIndicators() {
        assignedLevers.forEach((player, panels) -> {
            if (panels.size() != 0) {
                panels.getFirst().getGlowingBox().startGlowing(Bukkit.getPlayer(player));
            }
        });
    }

    @Override
    public void disableIndicators() {
        assignedLevers.forEach((player, panels) -> {
            if (panels.size() != 0) {
                panels.getFirst().getGlowingBox().stopGlowing(Bukkit.getPlayer(player));
            }
        });
    }

    public LinkedList<WallLever> getWallLevers() {
        return wallLevers;
    }

    public void fixedOneAndGiveNext(Player player) {
        Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer(player);
        if (arena == null) return;
        if (getCurrentStage(player) != getTotalStages(player)) {
            WallLever currentPanel = assignedLevers.get(player.getUniqueId()).removeFirst();
            if (currentPanel.getDropLocation() != null && arena.getLiveSettings().isVisualTasksEnabled()) {
                TaskChain<?> chain = SteveSus.newChain();
                for (Material material : GarbageGUI.CANDIDATES) {
                    if (material == Material.AIR) continue;
                    chain.sync(() -> currentPanel.getDropLocation().getWorld().dropItemNaturally(currentPanel.getDropLocation(), new ItemStack(material))).delay(15);
                }
                chain.execute();
            }
            currentPanel.getGlowingBox().stopGlowing(player);
            currentlyOpenPanel.remove(player.getUniqueId());
            SteveSus.newChain().delay(20).sync(player::closeInventory).execute();
            // mark done or
            if (assignedLevers.get(player.getUniqueId()).isEmpty()) {
                player.playSound(player.getLocation(), Sound.ENTITY_CAT_PURREOW, 1, 1);
                arena.refreshTaskMeter();
                arena.getGameEndConditions().tickGameEndConditions(arena);
            } else {
                // or assign next
                WallLever panel = assignedLevers.get(player.getUniqueId()).getFirst();
                panel.getGlowingBox().startGlowing(player);
                GameRoom room = arena.getRoom(panel.getGlowingBox().getMagmaCube().getLocation());
                Locale playerLang = LanguageManager.getINSTANCE().getLocale(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                player.sendMessage(playerLang.getMsg(player, EmptyGarbageTaskProvider.NEXT_PANEL).replace("{room}", (room == null ? playerLang.getMsg(null, Message.GAME_ROOM_NO_NAME) : room.getDisplayName(playerLang))));
            }
        }
    }

    private void tryOpenGUI(Player player, Entity entity, Arena arena) {
        if (entity.getType() != EntityType.MAGMA_CUBE) return;
        if (!arena.isTasksAllowedATM()) return;
        if (arena.getCamHandler() != null && arena.getCamHandler().isOnCam(player, arena)) return;
        if (hasTask(player)) {
            // should prevent called twice
            if (currentlyOpenPanel.contains(player.getUniqueId())) return;
            if (getCurrentStage(player) != getTotalStages(player)) {
                WallLever panel = assignedLevers.get(player.getUniqueId()).getFirst();
                if (panel != null && panel.getGlowingBox().getMagmaCube().equals(entity)) {
                    CommonLocale lang = LanguageManager.getINSTANCE().getLocale(player);
                    GarbageGUI gui = new GarbageGUI(lang, this);
                    gui.open(player);
                    currentlyOpenPanel.add(player.getUniqueId());
                }
            }
        }
    }
}
