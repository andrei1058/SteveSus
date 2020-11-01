package com.andrei1058.stevesus.commanditem;

import com.andrei1058.stevesus.api.event.GameStateChangeEvent;
import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.api.arena.GameState;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;

public class CommandItemListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getPlayer().getInventory().getItemInMainHand() == null) return;
        if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR) return;
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (CommonManager.getINSTANCE().getItemSupport().hasTag(e.getPlayer().getInventory().getItemInMainHand(), JoinItemsManager.INTERACT_NBT_TAG_PLAYER_CMDS)) {
                String CMDs = CommonManager.getINSTANCE().getItemSupport().getTag(e.getPlayer().getInventory().getItemInMainHand(), JoinItemsManager.INTERACT_NBT_TAG_PLAYER_CMDS);
                if (CMDs != null) {
                    Arrays.asList(CMDs.split(",")).forEach(cmd -> Bukkit.dispatchCommand(e.getPlayer(), cmd));
                }
            } else if (CommonManager.getINSTANCE().getItemSupport().hasTag(e.getPlayer().getInventory().getItemInMainHand(), JoinItemsManager.INTERACT_NBT_TAG_CONSOLE_CMDS)) {
                String CMDs = CommonManager.getINSTANCE().getItemSupport().getTag(e.getPlayer().getInventory().getItemInMainHand(), JoinItemsManager.INTERACT_NBT_TAG_CONSOLE_CMDS);
                if (CMDs != null) {
                    Arrays.asList(CMDs.split(",")).forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
                }
            }
        }
    }

    @EventHandler
    public void gameJoin(GameStateChangeEvent e){
        if (e.getNewState() == GameState.STARTING){
            e.getArena().getPlayers().forEach(player -> {
                InventoryUtil.wipePlayer(player);
                JoinItemsManager.sendCommandItems(player, JoinItemsManager.CATEGORY_STARTING);
            });
        } else if (e.getNewState() == GameState.WAITING){
            e.getArena().getPlayers().forEach(player -> {
                InventoryUtil.wipePlayer(player);
                JoinItemsManager.sendCommandItems(player, JoinItemsManager.CATEGORY_WAITING);
            });
        }
    }
}
