package dev.andrei1058.game.arena.gametask.primeshields;

import dev.andrei1058.game.SteveSus;
import dev.andrei1058.game.api.arena.Arena;
import dev.andrei1058.game.api.arena.GameListener;
import dev.andrei1058.game.api.arena.task.GameTask;
import dev.andrei1058.game.api.arena.task.TaskProvider;
import dev.andrei1058.game.api.event.PlayerTaskDoneEvent;
import dev.andrei1058.game.api.glow.GlowColor;
import dev.andrei1058.game.api.glow.GlowingBox;
import dev.andrei1058.game.api.locale.Locale;
import dev.andrei1058.game.api.server.GameSound;
import dev.andrei1058.game.api.server.PlayerCoolDown;
import dev.andrei1058.game.language.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.*;

public class PrimeShieldsTask extends GameTask {

    private final HashMap<UUID, Integer> assignedPlayers = new HashMap<>();
    private final GlowingBox glowingBox;
    private final Arena arena;
    private final LinkedList<UUID> openGUI = new LinkedList<>();

    public PrimeShieldsTask(String localName, Location location, Arena arena) {
        super(localName);
        this.arena = arena;
        this.glowingBox = new GlowingBox(location.add(0.5, 0, 0.5), 2, GlowColor.DARK_GRAY);

        arena.registerGameListener(new GameListener() {
            @Override
            public void onPlayerInteractEntity(Arena arena, Player player, Entity entity) {
                if (entity.getType() != EntityType.MAGMA_CUBE) return;
                if (!entity.equals(glowingBox.getMagmaCube())) return;
                PlayerCoolDown coolDown = PlayerCoolDown.getOrCreatePlayerData(player);
                if (coolDown.hasCoolDown("magmaCube")) return;
                coolDown.updateCoolDown("magmaCube", 1);
                if (!arena.isTasksAllowedATM()) return;
                if (arena.getCamHandler() != null && arena.getCamHandler().isOnCam(player, arena)) return;

                Locale lang = LanguageManager.getINSTANCE().getLocale(player);
                PrimeShieldsGUI gui = new PrimeShieldsGUI(lang, PrimeShieldsTask.this, player);
                openGUI.add(player.getUniqueId());
                gui.open(player);
            }

            @Override
            public void onInventoryClose(Arena arena, Player player, Inventory inventory) {
                openGUI.remove(player.getUniqueId());
            }
        });
    }

    @Override
    public TaskProvider getHandler() {
        return PrimeShieldsTaskProvider.getInstance();
    }

    @Override
    public void onInterrupt(Player player, Arena arena) {

    }

    @Override
    public int getCurrentStage(UUID player) {
        return assignedPlayers.getOrDefault(player, 0);
    }

    @Override
    public int getTotalStages(UUID player) {
        return 1;
    }

    @Override
    public void assignToPlayer(Player player, Arena arena) {
        if (hasTask(player)) return;
        glowingBox.startGlowing(player);
        assignedPlayers.put(player.getUniqueId(), 0);
    }

    @Override
    public Set<UUID> getAssignedPlayers() {
        return assignedPlayers.keySet();
    }

    @Override
    public boolean hasTask(Player player) {
        return assignedPlayers.containsKey(player.getUniqueId());
    }

    @Override
    public boolean isDoingTask(Player player) {
        return openGUI.contains(player.getUniqueId());
    }

    @Override
    public void enableIndicators() {
        for (Map.Entry<UUID, Integer> entry : assignedPlayers.entrySet()) {
            if (entry.getValue() != this.getTotalStages(entry.getKey())) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null) {
                    glowingBox.startGlowing(player);
                }
            }
        }
    }

    @Override
    public void disableIndicators() {
        for (Map.Entry<UUID, Integer> entry : assignedPlayers.entrySet()) {
            if (entry.getValue() != this.getTotalStages(entry.getKey())) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null) {
                    glowingBox.stopGlowing(player);
                }
            }
        }
    }

    public void complete(Player whoClicked) {
        if (hasTask(whoClicked)) {
            assignedPlayers.replace(whoClicked.getUniqueId(), 1);
            arena.refreshTaskMeter();
            arena.getGameEndConditions().tickGameEndConditions(arena);
            SteveSus.newChain().delay(15).sync(whoClicked::closeInventory).execute();
            GameSound.TASK_PROGRESS_DONE.playToPlayer(whoClicked);
            openGUI.remove(whoClicked.getUniqueId());
            glowingBox.stopGlowing(whoClicked);
            PlayerTaskDoneEvent taskDoneEvent = new PlayerTaskDoneEvent(arena, this, whoClicked);
            Bukkit.getPluginManager().callEvent(taskDoneEvent);
        }
    }
}
