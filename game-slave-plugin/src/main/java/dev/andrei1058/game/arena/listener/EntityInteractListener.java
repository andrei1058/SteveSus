package dev.andrei1058.game.arena.listener;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.arena.meeting.MeetingButton;
import dev.andrei1058.game.api.arena.GameListener;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.hook.glowing.GlowingManager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class EntityInteractListener implements Listener {

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        GameArena gameArena = ArenaManager.getINSTANCE().getArenaByPlayer(event.getPlayer());
        if (gameArena == null) return;
        event.setCancelled(true);
        // send remove glowing packet because GlowAPI
        // is a bit buggy and sends white glowing if the entity you're interacting
        // with was initialized for someone in the arena
        for (Player player : gameArena.getPlayers()){
            GlowingManager.sendRemove(event.getRightClicked(), player);
        }
        //
        if (gameArena.getMeetingButton() != null) {
            if (event.getRightClicked().getType() == EntityType.ARMOR_STAND && event.getRightClicked().hasMetadata(MeetingButton.MEETING_BUTTON_META_DATA_KEY)) {
                gameArena.getMeetingButton().onClick(event.getPlayer(), gameArena);
                return;
            }
        }
        for (GameListener gameListener : gameArena.getGameListeners()){
            gameListener.onPlayerInteractEntity(gameArena, event.getPlayer(), event.getRightClicked());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        GameArena gameArena = ArenaManager.getINSTANCE().getArenaByPlayer(event.getPlayer());
        if (gameArena == null) return;
        event.setCancelled(true);
        if (gameArena.getMeetingButton() != null) {
            if (event.getRightClicked().hasMetadata(MeetingButton.MEETING_BUTTON_META_DATA_KEY)) {
                gameArena.getMeetingButton().onClick(event.getPlayer(), gameArena);
            }
        }
        for (GameListener gameListener : gameArena.getGameListeners()){
            gameListener.onPlayerInteractEntity(gameArena, event.getPlayer(), event.getRightClicked());
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

    }
}
