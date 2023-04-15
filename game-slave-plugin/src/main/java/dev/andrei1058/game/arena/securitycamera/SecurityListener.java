package dev.andrei1058.game.arena.securitycamera;

import dev.andrei1058.game.SteveSus;
import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.arena.GameListener;
import dev.andrei1058.game.api.arena.meeting.MeetingStage;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.common.api.arena.GameState;
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
    public void onPlayerLeave(GameArena gameArena, Player player, boolean spectator) {
        if (gameArena.getCamHandler() != null){
            gameArena.getCamHandler().stopWatching(player, gameArena);
        }
    }

    @Override
    public void onMeetingStageChange(GameArena gameArena, MeetingStage oldStage, MeetingStage newStage) {
        if (gameArena.getCamHandler() != null){
            List<UUID> players = gameArena.getCamHandler().getPlayersOnCams();
            if (players.isEmpty()) return;

            for (UUID player : players){
                Player onCam = Bukkit.getPlayer(player);
                if (onCam != null){
                    gameArena.getCamHandler().stopWatching(onCam, gameArena);
                }
            }
        }
    }

    @Override
    public void onPlayerToggleSneakEvent(GameArena gameArena, Player player, boolean isSneaking) {
        if (gameArena.getCamHandler() == null) return;
        if (gameArena.getCamHandler().isOnCam(player, gameArena)){
            gameArena.getCamHandler().stopWatching(player, gameArena);
        }
    }

    @Override
    public void onGameStateChange(GameArena gameArena, GameState oldState, GameState newState) {
        if (newState == GameState.ENDING){
            if (gameArena.getCamHandler() != null){
                List<UUID> players = gameArena.getCamHandler().getPlayersOnCams();
                if (players.isEmpty()) return;

                for (UUID player : players){
                    Player onCam = Bukkit.getPlayer(player);
                    if (onCam != null){
                        gameArena.getCamHandler().stopWatching(onCam, gameArena);
                    }
                }
            }
        }
    }

    @Override
    public void onPlayerToggleFly(GameArena gameArena, Player player, boolean isFlying) {
        if (isFlying) return;
        if (gameArena.getCamHandler() == null) return;
        if (gameArena.getCamHandler().isOnCam(player, gameArena)){
            player.setFlying(true);
        }
    }

    @EventHandler
    public void onItemSwap(PlayerItemHeldEvent event){
        GameArena gameArena = ArenaManager.getINSTANCE().getArenaByPlayer(event.getPlayer());
        if (gameArena == null) return;
        if (gameArena.getCamHandler() == null) return;
        if (gameArena.getCamHandler().isOnCam(event.getPlayer(), gameArena)){
            event.setCancelled(true);
            if (event.getNewSlot() > 4){
                gameArena.getCamHandler().nextCam(event.getPlayer(), gameArena);
            } else if (event.getNewSlot() < 4){
                gameArena.getCamHandler().previousCam(event.getPlayer(), gameArena);
            }
        }
    }
}
