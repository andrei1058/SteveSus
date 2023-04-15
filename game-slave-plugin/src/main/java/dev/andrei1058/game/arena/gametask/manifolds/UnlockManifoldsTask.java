package dev.andrei1058.game.arena.gametask.manifolds;

import com.andrei1058.hologramapi.Hologram;
import com.andrei1058.hologramapi.HologramPage;
import com.andrei1058.hologramapi.content.LineTextContent;
import dev.andrei1058.game.SteveSus;
import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.arena.GameListener;
import dev.andrei1058.game.api.arena.task.GameTask;
import dev.andrei1058.game.api.arena.task.TaskProvider;
import dev.andrei1058.game.api.event.PlayerTaskDoneEvent;
import dev.andrei1058.game.api.glow.GlowColor;
import dev.andrei1058.game.api.glow.GlowingBox;
import dev.andrei1058.game.api.locale.Locale;
import dev.andrei1058.game.api.locale.Message;
import dev.andrei1058.game.common.api.arena.GameState;
import dev.andrei1058.game.language.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class UnlockManifoldsTask extends GameTask {

    // player uuid, finished boolean
    private final HashMap<UUID, Boolean> assignedPlayers = new HashMap<>();
    private final GameArena gameArena;
    private final Block shulkerBlock;
    private final Hologram hologram;
    private final GlowingBox glowingBox;

    public UnlockManifoldsTask(String localName, GameArena gameArena, Location location) {
        super(localName);
        this.gameArena = gameArena;
        LanguageManager.getINSTANCE().getDefaultLocale().setMsg(Message.GAME_TASK_PATH_ + UnlockManifoldsProvider.getInstance().getIdentifier() + "-" + localName, "&0Count to ten");
        location.getBlock().setType(Material.AIR);
        shulkerBlock = location.getBlock();
        shulkerBlock.setType(Material.BLACK_SHULKER_BOX);
        glowingBox = new GlowingBox(location, 2, GlowColor.YELLOW);

        hologram = new Hologram(location.clone().add(0, 2, 0), 2);
        hologram.allowCollisions(false);
        HologramPage page = hologram.getPage(0);
        assert page != null;
        page.setLineContent(0, new LineTextContent(s -> LanguageManager.getINSTANCE().getMsg(s, Message.GAME_TASK_NAME_PATH_.toString() + getHandler().getIdentifier())));
        page.setLineContent(1, new LineTextContent(s -> LanguageManager.getINSTANCE().getMsg(s, Message.GAME_TASK_DESCRIPTION_PATH_.toString() + getHandler().getIdentifier())));
        hologram.hide();

        gameArena.registerGameListener(new GameListener() {
            @Override
            public void onPlayerInteractEntity(GameArena gameArena, Player player, Entity entity) {
                if (entity.equals(getGlowingBox().getMagmaCube())) {
                    tryOpenGUI(player, gameArena);
                }
            }

            @Override
            public void onEntityPunch(GameArena gameArena, Player player, Entity entity) {
                if (entity.equals(getGlowingBox().getMagmaCube())) {
                    tryOpenGUI(player, gameArena);
                }
            }

            @Override
            public void onPlayerInteract(GameArena gameArena, Player player, PlayerInteractEvent event, boolean hasItemInHand) {
                if (event.getClickedBlock() != null){
                    if (event.getClickedBlock().equals(shulkerBlock)){
                        event.setCancelled(true);
                        tryOpenGUI(player, gameArena);
                    }
                }
            }

            @Override
            public void onGameStateChange(GameArena gameArena, GameState oldState, GameState newState) {
                if (newState == GameState.IN_GAME) {

                    // hide hologram for those who do not have this task
                    for (Player player : gameArena.getPlayers()) {
                        if (!hasTask(player)) {
                            hologram.hide(player);
                        }
                    }
                    hologram.show();
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
    public void onInterrupt(Player player, GameArena gameArena) {
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
    public void assignToPlayer(Player player, GameArena gameArena) {
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
        for (Player player : gameArena.getPlayers()) {
            if (hasTask(player) && (getCurrentStage(player) != getTotalStages(player))) {
                getGlowingBox().startGlowing(player);
                hologram.hide(player);
            }
        }
    }

    @Override
    public void disableIndicators() {
        for (Player player : gameArena.getPlayers()) {
            if (hasTask(player)) {
                getGlowingBox().stopGlowing(player);
                hologram.show(player);
            }
        }
    }

    public void markDone(Player player) {
        player.closeInventory();
        assignedPlayers.replace(player.getUniqueId(), true);
        gameArena.refreshTaskMeter();
        gameArena.getGameEndConditions().tickGameEndConditions(gameArena);
        getGlowingBox().stopGlowing(player);
        hologram.hide(player);
        PlayerTaskDoneEvent taskDoneEvent = new PlayerTaskDoneEvent(gameArena, this, player);
        Bukkit.getPluginManager().callEvent(taskDoneEvent);
    }

    private void tryOpenGUI(Player player, GameArena gameArena) {
        if (!gameArena.isTasksAllowedATM()) return;
        if (hasTask(player)) {
            if (gameArena.getCamHandler() != null && gameArena.getCamHandler().isOnCam(player, gameArena)) return;
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
