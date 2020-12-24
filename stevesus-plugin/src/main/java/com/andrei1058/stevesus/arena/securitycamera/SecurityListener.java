package com.andrei1058.stevesus.arena.securitycamera;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.GameListener;
import com.andrei1058.stevesus.api.arena.meeting.MeetingStage;
import com.andrei1058.stevesus.arena.ArenaManager;
import com.andrei1058.stevesus.common.api.arena.GameState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

import java.util.List;
import java.util.UUID;

public class SecurityListener implements GameListener, Listener {

    private static SecurityListener instance;

    // restoring to the default cam handler will require to register this listener again.
    private SecurityListener(){
        Bukkit.getPluginManager().registerEvents(this, SteveSus.getInstance());
    }

    public static SecurityListener getInstance() {
        return instance == null ? instance = new SecurityListener() : instance;
    }

    @Override
    public void onPlayerLeave(Arena arena, Player player, boolean spectator) {
        if (arena.getCamHandler() != null){
            arena.getCamHandler().stopWatching(player, arena);
        }
    }

    @Override
    public void onMeetingStageChange(Arena arena, MeetingStage oldStage, MeetingStage newStage) {
        if (arena.getCamHandler() != null){
            List<UUID> players = arena.getCamHandler().getPlayersOnCams();
            if (players.isEmpty()) return;

            for (UUID player : players){
                Player onCam = Bukkit.getPlayer(player);
                if (onCam != null){
                    arena.getCamHandler().stopWatching(onCam, arena);
                }
            }
        }
    }

    @Override
    public void onPlayerToggleSneakEvent(Arena arena, Player player, boolean isSneaking) {
        if (arena.getCamHandler() == null) return;
        if (arena.getCamHandler().isOnCam(player, arena)){
            arena.getCamHandler().stopWatching(player, arena);
        }
    }

    @Override
    public void onGameStateChange(Arena arena, GameState oldState, GameState newState) {
        if (newState == GameState.ENDING){
            if (arena.getCamHandler() != null){
                List<UUID> players = arena.getCamHandler().getPlayersOnCams();
                if (players.isEmpty()) return;

                for (UUID player : players){
                    Player onCam = Bukkit.getPlayer(player);
                    if (onCam != null){
                        arena.getCamHandler().stopWatching(onCam, arena);
                    }
                }
            }
        }
    }

    @Override
    public void onPlayerToggleFly(Arena arena, Player player, boolean isFlying) {
        if (isFlying) return;
        if (arena.getCamHandler() == null) return;
        if (arena.getCamHandler().isOnCam(player, arena)){
            player.setFlying(true);
        }
    }

    @EventHandler
    public void onItemSwap(PlayerItemHeldEvent event){
        Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer(event.getPlayer());
        if (arena == null) return;
        if (arena.getCamHandler() == null) return;
        if (arena.getCamHandler().isOnCam(event.getPlayer(), arena)){
            event.setCancelled(true);
            if (event.getNewSlot() > 4){
                arena.getCamHandler().nextCam(event.getPlayer(), arena);
            } else if (event.getNewSlot() < 4){
                arena.getCamHandler().previousCam(event.getPlayer(), arena);
            }
        }
    }
}
