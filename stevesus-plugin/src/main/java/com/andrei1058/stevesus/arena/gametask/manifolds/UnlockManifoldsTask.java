package com.andrei1058.stevesus.arena.gametask.manifolds;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.GameListener;
import com.andrei1058.stevesus.api.arena.task.GameTask;
import com.andrei1058.stevesus.api.arena.task.TaskProvider;
import com.andrei1058.stevesus.api.event.PlayerTaskDoneEvent;
import com.andrei1058.stevesus.api.glow.GlowColor;
import com.andrei1058.stevesus.api.glow.GlowingBox;
import com.andrei1058.stevesus.api.hook.hologram.HologramI;
import com.andrei1058.stevesus.api.hook.hologram.HologramManager;
import com.andrei1058.stevesus.api.locale.Locale;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.common.api.arena.GameState;
import com.andrei1058.stevesus.language.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class UnlockManifoldsTask extends GameTask {

    // player uuid, finished boolean
    private final HashMap<UUID, Boolean> assignedPlayers = new HashMap<>();
    private final Arena arena;
    private final Block shulkerBlock;
    private @Nullable HologramI hologram = null;
    private final GlowingBox glowingBox;

    public UnlockManifoldsTask(String localName, Arena arena, Location location) {
        super(localName);
        this.arena = arena;
        LanguageManager.getINSTANCE().getDefaultLocale().setMsg(Message.GAME_TASK_PATH_ + UnlockManifoldsProvider.getInstance().getIdentifier() + "-" + localName, "&0Count to ten");
        location.getBlock().setType(Material.AIR);
        shulkerBlock = location.getBlock();
        shulkerBlock.setType(Material.BLACK_SHULKER_BOX);
        glowingBox = new GlowingBox(location, 2, GlowColor.YELLOW);

        var holoProvider = HologramManager.getInstance().getProvider();

        if (null != holoProvider) {
            hologram = holoProvider.spawnHologram(location.clone().add(0, 2, 0));
            hologram.setPageContent(Arrays.asList(
                    player -> LanguageManager.getINSTANCE().getMsg(player, Message.GAME_TASK_NAME_PATH_ + getHandler().getIdentifier()),
                    player -> LanguageManager.getINSTANCE().getMsg(player, Message.GAME_TASK_DESCRIPTION_PATH_ + getHandler().getIdentifier())
            ));
            hologram.hideToAll();
        }


        arena.registerGameListener(new GameListener() {
            @Override
            public void onPlayerInteractEntity(Arena arena, Player player, Entity entity) {
                if (entity.equals(getGlowingBox().getMagmaCube())) {
                    tryOpenGUI(player, arena);
                }
            }

            @Override
            public void onEntityPunch(Arena arena, Player player, Entity entity) {
                if (entity.equals(getGlowingBox().getMagmaCube())) {
                    tryOpenGUI(player, arena);
                }
            }

            @Override
            public void onPlayerInteract(Arena arena, Player player, PlayerInteractEvent event, boolean hasItemInHand) {
                if (event.getClickedBlock() != null){
                    if (event.getClickedBlock().equals(shulkerBlock)){
                        event.setCancelled(true);
                        tryOpenGUI(player, arena);
                    }
                }
            }

            @Override
            public void onGameStateChange(Arena arena, GameState oldState, GameState newState) {
                if (newState == GameState.IN_GAME) {
                    if (null == hologram) {
                        return;
                    }
                    // hide hologram for those who do not have this task
                    for (Player player : arena.getPlayers()) {
                        if (!hasTask(player)) {
                            hologram.hideFromPlayer(player);
                        }
                    }
                    hologram.unHide();
                }
            }
        });
    }

    public GlowingBox getGlowingBox() {
        return glowingBox;
    }

    @Override
    public TaskProvider getHandler() {
        return UnlockManifoldsProvider.getInstance();
    }

    @Override
    public void onInterrupt(Player player, Arena arena) {
        if (isDoingTask(player)) {
            player.closeInventory();
        }
    }

    @Override
    public int getCurrentStage(Player player) {
        return assignedPlayers.getOrDefault(player.getUniqueId(), false) ? 1 : 0;
    }

    @Override
    public int getCurrentStage(UUID player) {
        return assignedPlayers.getOrDefault(player, false) ? 1 : 0;
    }

    @Override
    public int getTotalStages(Player player) {
        return 1;
    }

    @Override
    public int getTotalStages(UUID player) {
        return 1;
    }

    @Override
    public void assignToPlayer(Player player, Arena arena) {
        assignedPlayers.remove(player.getUniqueId());
        assignedPlayers.put(player.getUniqueId(), false);
        getGlowingBox().startGlowing(player);
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
        if (player.getOpenInventory() == null) {
            return false;
        }
        if (player.getOpenInventory().getTopInventory() == null) {
            return false;
        }
        return player.getOpenInventory().getTopInventory().getHolder().getClass().getSimpleName().equals(UnlockGUI.UnlockManifoldsHandler.class.getSimpleName());
    }

    @Override
    public void enableIndicators() {
        if (null == hologram) {
            return;
        }

        for (Player player : arena.getPlayers()) {
            if (hasTask(player) && (getCurrentStage(player) != getTotalStages(player))) {
                getGlowingBox().startGlowing(player);
                hologram.hideFromPlayer(player);
            }
        }
    }

    @Override
    public void disableIndicators() {
        if (null == hologram) {
            return;
        }

        for (Player player : arena.getPlayers()) {
            if (hasTask(player)) {
                getGlowingBox().stopGlowing(player);
                hologram.showToPlayer(player);
            }
        }
    }

    public void markDone(@NotNull Player player) {
        player.closeInventory();
        assignedPlayers.replace(player.getUniqueId(), true);
        arena.refreshTaskMeter();
        arena.getGameEndConditions().tickGameEndConditions(arena);
        getGlowingBox().stopGlowing(player);
        if (null != hologram) {
            hologram.hideFromPlayer(player);
        }
        PlayerTaskDoneEvent taskDoneEvent = new PlayerTaskDoneEvent(arena, this, player);
        Bukkit.getPluginManager().callEvent(taskDoneEvent);
    }

    private void tryOpenGUI(Player player, @NotNull Arena arena) {
        if (!arena.isTasksAllowedATM()) return;
        if (hasTask(player)) {
            if (arena.getCamHandler() != null && arena.getCamHandler().isOnCam(player, arena)) return;
            // should prevent called twice
            if (isDoingTask(player)) return;
            if (getCurrentStage(player) != getTotalStages(player)) {
                SteveSus.newChain().async(() -> {
                    Locale playerLang = LanguageManager.getINSTANCE().getLocale(player);
                    UnlockGUI gui = new UnlockGUI(playerLang, getLocalName(), this);
                    SteveSus.newChain().sync(() -> gui.open(player)).execute();
                }).execute();
            }
        }
    }
}
