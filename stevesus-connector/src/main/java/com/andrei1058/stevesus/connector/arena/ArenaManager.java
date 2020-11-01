package com.andrei1058.stevesus.connector.arena;

import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.selector.SelectorManager;
import com.andrei1058.stevesus.connector.SteveSusConnector;
import com.andrei1058.stevesus.connector.common.ArenaSelectorListener;
import com.andrei1058.stevesus.connector.arena.command.GamesCommand;
import com.andrei1058.stevesus.common.api.arena.DisplayableArena;
import com.andrei1058.stevesus.common.api.packet.RawSocket;
import com.andrei1058.stevesus.connector.api.arena.RemoteArena;
import com.andrei1058.stevesus.connector.api.event.GameDropEvent;
import com.andrei1058.stevesus.connector.api.event.GameRegisterEvent;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ArenaManager {

    private static ArenaManager INSTANCE;

    private static final List<DisplayableArena> displayableArenas = new ArrayList<>();

    private static long lastPlayerCountRequest = 0L;
    private static int lastPlayerCount = 0;
    private static long lastSpectatorCountRequest = 0L;
    private static int lastSpectatorCount = 0;
    private static long lastOnlineCountRequest = 0L;
    private static int lastOnlineCount = 0;

    private ArenaManager() {
    }

    public List<DisplayableArena> getArenas() {
        return displayableArenas;
    }


    public static void init() {
        if (INSTANCE == null) {
            INSTANCE = new ArenaManager();
            Bukkit.getPluginManager().registerEvents(new ArenaSelectorListener(), SteveSusConnector.getInstance());

            // register admin games list command
            GamesCommand.register(CommonManager.getINSTANCE().getCommonProvider().getMainCommand());
        }
    }

    public static ArenaManager getInstance() {
        return INSTANCE;
    }

    @Nullable
    public DisplayableArena getFromTag(String tag) {
        return getArenas().stream().filter(a -> a.getTag().equals(tag)).findFirst().orElse(null);
    }

    public void remove(DisplayableArena arena) {
        if (displayableArenas.remove(arena)) {
            SteveSusConnector.debug("Removing DisplayableArena: " + arena.getTag());
            Bukkit.getPluginManager().callEvent(new GameDropEvent(arena));
            SelectorManager.getINSTANCE().refreshArenaSelector();
        }
    }

    @SuppressWarnings("unused")
    public void add(DisplayableArena arena) {
        if (!displayableArenas.contains(arena)) {
            displayableArenas.add(arena);
            SteveSusConnector.debug("Added new DisplayableArena: " + arena.getTag());
            Bukkit.getPluginManager().callEvent(new GameRegisterEvent(arena));
            SelectorManager.getINSTANCE().refreshArenaSelector();
        }
    }

    public @Nullable RemoteArena getArena(RawSocket server, int gameId) {
        return (RemoteArena) getArenas().stream().filter(arena -> arena instanceof RemoteArena).filter(arena -> ((RemoteArena) arena).getServer().equals(server))
                .filter(arena -> arena.getGameId() == gameId).findFirst().orElse(null);
    }

    public int getPlayerCount() {
        if (System.currentTimeMillis() < lastPlayerCountRequest) {
            return lastPlayerCount;
        }
        lastPlayerCount = 0;
        getArenas().forEach(arena -> lastPlayerCount += arena.getCurrentPlayers());
        // 50 should be a server tick
        lastPlayerCountRequest = System.currentTimeMillis() + 50;
        return lastPlayerCount;
    }

    public int getSpectatorCount() {
        if (System.currentTimeMillis() < lastSpectatorCountRequest) {
            return lastSpectatorCount;
        }
        lastSpectatorCount = 0;
        getArenas().forEach(arena -> lastSpectatorCount += arena.getCurrentSpectators());
        // 50 should be a server tick
        lastSpectatorCountRequest = System.currentTimeMillis() + 50;
        return lastSpectatorCount;
    }

    public int getOnlineCount() {
        if (System.currentTimeMillis() < lastOnlineCountRequest) {
            return lastOnlineCount;
        }
        lastOnlineCount = 0;
        getArenas().forEach(arena -> lastOnlineCount += arena.getCurrentSpectators() + arena.getCurrentPlayers());
        // 50 should be a server tick
        lastOnlineCountRequest = System.currentTimeMillis() + 50;
        return lastOnlineCount;
    }
}
