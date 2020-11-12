package com.andrei1058.stevesus.arena.listener;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.MeetingButton;
import com.andrei1058.stevesus.arena.ArenaManager;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class EntityInteractListener implements Listener {

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (event.isCancelled()) return;
        Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer(event.getPlayer());
        if (arena == null) return;
        event.setCancelled(true);
        if (arena.getMeetingButton() == null) return;
        if (event.getRightClicked().getType() == EntityType.ARMOR_STAND && event.getRightClicked().hasMetadata(MeetingButton.MEETING_BUTTON_META_DATA_KEY)) {
            arena.getMeetingButton().onClick(event.getPlayer(), arena);
        }
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event){
        if (event.isCancelled()) return;
        Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer(event.getPlayer());
        if (arena == null) return;
        event.setCancelled(true);
        if (arena.getMeetingButton() == null) return;
        if (event.getRightClicked().hasMetadata(MeetingButton.MEETING_BUTTON_META_DATA_KEY)) {
            arena.getMeetingButton().onClick(event.getPlayer(), arena);
        }
    }
}
