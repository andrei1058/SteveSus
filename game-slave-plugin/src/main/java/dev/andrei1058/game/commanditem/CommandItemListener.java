package dev.andrei1058.game.commanditem;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.arena.GameListener;
import dev.andrei1058.game.api.event.GameStateChangeEvent;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.common.CommonManager;
import dev.andrei1058.game.common.api.arena.GameState;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class CommandItemListener implements Listener {


    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        ItemStack i = e.getCurrentItem();
        if (i == null) return;
        if (i.getType() == Material.AIR) return;
        if (CommonManager.getINSTANCE().getItemSupport().hasTag(i, CommandItemsManager.INTERACT_NBT_TAG_CONSOLE_CMDS) ||
                CommonManager.getINSTANCE().getItemSupport().hasTag(i, CommandItemsManager.INTERACT_NBT_TAG_CONSOLE_CMDS) ||
                CommonManager.getINSTANCE().getItemSupport().hasTag(i, CommandItemsManager.INTERACT_NBT_TAG_PLAYER_INTERACT)){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getPlayer().getInventory().getItemInMainHand() == null) return;
        if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR) return;
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (CommonManager.getINSTANCE().getItemSupport().hasTag(e.getPlayer().getInventory().getItemInMainHand(), CommandItemsManager.INTERACT_NBT_TAG_PLAYER_CMDS)) {
                e.setCancelled(true);
                String CMDs = CommonManager.getINSTANCE().getItemSupport().getTag(e.getPlayer().getInventory().getItemInMainHand(), CommandItemsManager.INTERACT_NBT_TAG_PLAYER_CMDS);
                if (CMDs != null) {
                    Arrays.asList(CMDs.split(",")).forEach(cmd -> Bukkit.dispatchCommand(e.getPlayer(), cmd));
                }
            }
            if (CommonManager.getINSTANCE().getItemSupport().hasTag(e.getPlayer().getInventory().getItemInMainHand(), CommandItemsManager.INTERACT_NBT_TAG_CONSOLE_CMDS)) {
                e.setCancelled(true);
                String CMDs = CommonManager.getINSTANCE().getItemSupport().getTag(e.getPlayer().getInventory().getItemInMainHand(), CommandItemsManager.INTERACT_NBT_TAG_CONSOLE_CMDS);
                if (CMDs != null) {
                    Arrays.asList(CMDs.split(",")).forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
                }
            }
            if (CommonManager.getINSTANCE().getItemSupport().hasTag(e.getPlayer().getInventory().getItemInMainHand(), CommandItemsManager.INTERACT_NBT_TAG_PLAYER_INTERACT)) {
                e.setCancelled(true);
                CommandItemsManager.getINSTANCE().getInteractEvent().onInteract(e.getPlayer(), e);
            }
        }

        GameArena gameArena = ArenaManager.getINSTANCE().getArenaByPlayer(e.getPlayer());
        if (gameArena == null) return;
        final Player player = e.getPlayer();
        boolean hasItemInHand = e.getItem() != null && e.getItem().getType() != Material.AIR;
        for (GameListener gameListener : gameArena.getGameListeners()){
            gameListener.onPlayerInteract(gameArena, player, e, hasItemInHand);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEntityEvent e) {
        if (e.getPlayer().getInventory().getItemInMainHand() == null) return;
        if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR) return;
        if (CommonManager.getINSTANCE().getItemSupport().hasTag(e.getPlayer().getInventory().getItemInMainHand(), CommandItemsManager.INTERACT_NBT_TAG_PLAYER_CMDS)) {
            e.setCancelled(true);
            String CMDs = CommonManager.getINSTANCE().getItemSupport().getTag(e.getPlayer().getInventory().getItemInMainHand(), CommandItemsManager.INTERACT_NBT_TAG_PLAYER_CMDS);
            if (CMDs != null) {
                Arrays.asList(CMDs.split(",")).forEach(cmd -> Bukkit.dispatchCommand(e.getPlayer(), cmd));
            }
        }
        if (CommonManager.getINSTANCE().getItemSupport().hasTag(e.getPlayer().getInventory().getItemInMainHand(), CommandItemsManager.INTERACT_NBT_TAG_CONSOLE_CMDS)) {
            e.setCancelled(true);
            String CMDs = CommonManager.getINSTANCE().getItemSupport().getTag(e.getPlayer().getInventory().getItemInMainHand(), CommandItemsManager.INTERACT_NBT_TAG_CONSOLE_CMDS);
            if (CMDs != null) {
                Arrays.asList(CMDs.split(",")).forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
            }
        }
        if (CommonManager.getINSTANCE().getItemSupport().hasTag(e.getPlayer().getInventory().getItemInMainHand(), CommandItemsManager.INTERACT_NBT_TAG_PLAYER_INTERACT)) {
            e.setCancelled(true);
            CommandItemsManager.getINSTANCE().getInteractEvent().onInteract(e.getPlayer(), e);
        }

    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerArmorStandManipulateEvent e) {
        if (e.getPlayer().getInventory().getItemInMainHand() == null) return;
        if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR) return;
        if (CommonManager.getINSTANCE().getItemSupport().hasTag(e.getPlayer().getInventory().getItemInMainHand(), CommandItemsManager.INTERACT_NBT_TAG_PLAYER_CMDS)) {
            e.setCancelled(true);
            String CMDs = CommonManager.getINSTANCE().getItemSupport().getTag(e.getPlayer().getInventory().getItemInMainHand(), CommandItemsManager.INTERACT_NBT_TAG_PLAYER_CMDS);
            if (CMDs != null) {
                Arrays.asList(CMDs.split(",")).forEach(cmd -> Bukkit.dispatchCommand(e.getPlayer(), cmd));
            }
        }
        if (CommonManager.getINSTANCE().getItemSupport().hasTag(e.getPlayer().getInventory().getItemInMainHand(), CommandItemsManager.INTERACT_NBT_TAG_CONSOLE_CMDS)) {
            e.setCancelled(true);
            String CMDs = CommonManager.getINSTANCE().getItemSupport().getTag(e.getPlayer().getInventory().getItemInMainHand(), CommandItemsManager.INTERACT_NBT_TAG_CONSOLE_CMDS);
            if (CMDs != null) {
                Arrays.asList(CMDs.split(",")).forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
            }
        }
        if (CommonManager.getINSTANCE().getItemSupport().hasTag(e.getPlayer().getInventory().getItemInMainHand(), CommandItemsManager.INTERACT_NBT_TAG_PLAYER_INTERACT)) {
            e.setCancelled(true);
            CommandItemsManager.getINSTANCE().getInteractEvent().onInteract(e.getPlayer(), e);
        }
    }

    @EventHandler
    public void gameJoin(GameStateChangeEvent e) {
        if (e.getNewState() == GameState.STARTING) {
            e.getArena().getPlayers().forEach(player -> {
                InventoryUtil.wipePlayer(player);
                CommandItemsManager.sendCommandItems(player, CommandItemsManager.CATEGORY_STARTING);
            });
        } else if (e.getNewState() == GameState.WAITING) {
            e.getArena().getPlayers().forEach(player -> {
                InventoryUtil.wipePlayer(player);
                CommandItemsManager.sendCommandItems(player, CommandItemsManager.CATEGORY_WAITING);
            });
        }
    }
}
