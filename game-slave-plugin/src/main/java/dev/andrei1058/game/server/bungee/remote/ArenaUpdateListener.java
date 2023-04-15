package dev.andrei1058.game.server.bungee.remote;

import dev.andrei1058.game.SteveSus;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.api.event.*;
import dev.andrei1058.game.common.api.packet.DefaultChannel;
import dev.andrei1058.game.server.bungee.packet.ArenaStatusUpdatePacket;
import dev.andrei1058.game.server.bungee.packet.DropGamePacket;
import dev.andrei1058.game.server.bungee.packet.FullDataArenaPacket;
import dev.andrei1058.game.server.bungee.packet.PlayerCountUpdatePacket;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ArenaUpdateListener implements Listener {

    @EventHandler
    public void onPlayerJoinArena(PlayerGameJoinEvent e) {
        if (e == null) return;
        SteveSus.getInstance().getPacketsHandler().sendPacket(DefaultChannel.PLAYER_COUNT_UPDATE.toString(), new PlayerCountUpdatePacket(e.getArena().getGameId(), e.getArena().getCurrentPlayers(), e.getArena().getCurrentSpectators(), (int) e.getArena().getPlayers().stream().filter(p -> ArenaManager.getINSTANCE().hasVipJoin(p)).count()), true);
    }

    @EventHandler
    public void onPlayerLeaveArena(PlayerGameLeaveEvent e) {
        if (e == null) return;
        SteveSus.getInstance().getPacketsHandler().sendPacket(DefaultChannel.PLAYER_COUNT_UPDATE.toString(), new PlayerCountUpdatePacket(e.getArena().getGameId(), e.getArena().getCurrentPlayers(), e.getArena().getCurrentSpectators(), (int) e.getArena().getPlayers().stream().filter(p -> ArenaManager.getINSTANCE().hasVipJoin(p)).count()), true);
    }

    @EventHandler
    public void onArenaStatusChange(GameStateChangeEvent e) {
        if (e == null) return;
        SteveSus.getInstance().getPacketsHandler().sendPacket(DefaultChannel.ARENA_STATUS_UPDATE.toString(), new ArenaStatusUpdatePacket(e.getArena(), e.getNewState(), e.getOldState()), true);
    }

    @EventHandler
    public void onArenaLoad(GameInitializedEvent e) {
        if (e == null) return;
        SteveSus.getInstance().getPacketsHandler().sendPacket(DefaultChannel.ARENA_FULL_DATA.toString(), new FullDataArenaPacket(e.getArena()), true);
    }

    @EventHandler
    public void onDropInstance(GameRestartEvent e) {
        if (e == null) return;
        SteveSus.getInstance().getPacketsHandler().sendPacket(DefaultChannel.GAME_DROP.toString(), new DropGamePacket(e.getGameId()), true);
    }

    @EventHandler
    public void onDropInstance(GameDisableEvent e) {
        if (e == null) return;
        SteveSus.getInstance().getPacketsHandler().sendPacket(DefaultChannel.GAME_DROP.toString(), new DropGamePacket(e.getGameId()), true);
    }
}
