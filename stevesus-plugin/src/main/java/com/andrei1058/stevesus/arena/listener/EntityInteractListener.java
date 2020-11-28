package com.andrei1058.stevesus.arena.listener;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.meeting.MeetingButton;
import com.andrei1058.stevesus.api.arena.GameListener;
import com.andrei1058.stevesus.arena.ArenaManager;
import com.andrei1058.stevesus.hook.glowing.GlowingManager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class EntityInteractListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer(event.getPlayer());
        if (arena == null) return;
        event.setCancelled(true);
        // send remove glowing packet because GlowAPI
        // is a bit buggy and sends white glowing if the entity you're interacting
        // with was initialized for someone in the arena
        for (Player player : arena.getPlayers()){
            GlowingManager.sendRemove(event.getRightClicked(), player);
        }
        //
        if (arena.getMeetingButton() != null) {
            if (event.getRightClicked().getType() == EntityType.ARMOR_STAND && event.getRightClicked().hasMetadata(MeetingButton.MEETING_BUTTON_META_DATA_KEY)) {
                arena.getMeetingButton().onClick(event.getPlayer(), arena);
                return;
            }
        }
        for (GameListener gameListener : arena.getGameListeners()){
            gameListener.onPlayerInteractEntity(arena, event.getPlayer(), event.getRightClicked());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer(event.getPlayer());
        if (arena == null) return;
        event.setCancelled(true);
        if (arena.getMeetingButton() != null) {
            if (event.getRightClicked().hasMetadata(MeetingButton.MEETING_BUTTON_META_DATA_KEY)) {
                arena.getMeetingButton().onClick(event.getPlayer(), arena);
            }
        }
        for (GameListener gameListener : arena.getGameListeners()){
            gameListener.onPlayerInteractEntity(arena, event.getPlayer(), event.getRightClicked());
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

    }
}
