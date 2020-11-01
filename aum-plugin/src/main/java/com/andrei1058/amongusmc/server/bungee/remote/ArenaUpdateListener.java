package com.andrei1058.amongusmc.server.bungee.remote;

import com.andrei1058.amongusmc.AmongUsMc;
import com.andrei1058.amongusmc.api.event.*;
import com.andrei1058.amongusmc.arena.ArenaManager;
import com.andrei1058.amongusmc.server.bungee.packet.ArenaStatusUpdatePacket;
import com.andrei1058.amongusmc.server.bungee.packet.DropGamePacket;
import com.andrei1058.amongusmc.server.bungee.packet.FullDataArenaPacket;
import com.andrei1058.amongusmc.server.bungee.packet.PlayerCountUpdatePacket;
import com.andrei1058.amoungusmc.common.api.packet.DefaultChannel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ArenaUpdateListener implements Listener {

    @EventHandler
    public void onPlayerJoinArena(PlayerGameJoinEvent e) {
        if (e == null) return;
        AmongUsMc.getInstance().getPacketsHandler().sendPacket(DefaultChannel.PLAYER_COUNT_UPDATE.toString(), new PlayerCountUpdatePacket(e.getArena().getGameId(), e.getArena().getCurrentPlayers(), e.getArena().getCurrentSpectators(), (int) e.getArena().getPlayers().stream().filter(p -> ArenaManager.getINSTANCE().hasVipJoin(p)).count()), true);
    }

    @EventHandler
    public void onPlayerLeaveArena(PlayerGameLeaveEvent e) {
        if (e == null) return;
        AmongUsMc.getInstance().getPacketsHandler().sendPacket(DefaultChannel.PLAYER_COUNT_UPDATE.toString(), new PlayerCountUpdatePacket(e.getArena().getGameId(), e.getArena().getCurrentPlayers(), e.getArena().getCurrentSpectators(), (int) e.getArena().getPlayers().stream().filter(p -> ArenaManager.getINSTANCE().hasVipJoin(p)).count()), true);
    }

    @EventHandler
    public void onArenaStatusChange(GameStateChangeEvent e) {
        if (e == null) return;
        AmongUsMc.getInstance().getPacketsHandler().sendPacket(DefaultChannel.ARENA_STATUS_UPDATE.toString(), new ArenaStatusUpdatePacket(e.getArena(), e.getNewState(), e.getOldState()), true);
    }

    @EventHandler
    public void onArenaLoad(GameInitializedEvent e) {
        if (e == null) return;
        AmongUsMc.getInstance().getPacketsHandler().sendPacket(DefaultChannel.ARENA_FULL_DATA.toString(), new FullDataArenaPacket(e.getArena()), true);
    }

    @EventHandler
    public void onDropInstance(GameRestartEvent e) {
        if (e == null) return;
        AmongUsMc.getInstance().getPacketsHandler().sendPacket(DefaultChannel.GAME_DROP.toString(), new DropGamePacket(e.getGameId()), true);
    }

    @EventHandler
    public void onDropInstance(GameDisableEvent e) {
        if (e == null) return;
        AmongUsMc.getInstance().getPacketsHandler().sendPacket(DefaultChannel.GAME_DROP.toString(), new DropGamePacket(e.getGameId()), true);
    }
}
