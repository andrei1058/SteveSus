package com.andrei1058.amongusmc.server;

import com.andrei1058.amongusmc.AmongUsMc;
import com.andrei1058.amongusmc.api.server.ServerType;
import com.andrei1058.amongusmc.arena.ArenaManager;
import com.andrei1058.amongusmc.common.CommonManager;
import com.andrei1058.amongusmc.common.party.PartyManager;
import com.andrei1058.amongusmc.selector.ArenaSelectorListener;
import com.andrei1058.amongusmc.server.bungee.packet.ArenaCommunicationHandler;
import com.andrei1058.amongusmc.setup.SetupManager;
import com.andrei1058.amoungusmc.common.api.CommonProvider;
import com.andrei1058.amoungusmc.common.api.arena.DisplayableArena;
import com.andrei1058.amoungusmc.common.api.locale.CommonLocaleManager;
import com.andrei1058.amongusmc.common.selector.SelectorManager;
import com.andrei1058.amongusmc.language.LanguageManager;
import com.andrei1058.amoungusmc.common.api.packet.CommunicationHandler;
import com.andrei1058.amoungusmc.common.api.party.PartyHandler;
import com.andrei1058.spigot.commandlib.FastRootCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ServerCommonProvider implements CommonProvider {

    private static final List<DisplayableArena> displayableArenas = new ArrayList<>();
    private static ServerCommonProvider INSTANCE;
    private FastRootCommand rootCommand;

    // used in bungee mode
    private final CommunicationHandler communicationHandler = new ArenaCommunicationHandler();

    private ServerCommonProvider() {
        if (ServerManager.getINSTANCE().getServerType() == ServerType.MULTI_ARENA) {
            Bukkit.getPluginManager().registerEvents(new ArenaSelectorListener(), AmongUsMc.getInstance());
        }
    }

    @Override
    public List<DisplayableArena> getArenas() {
        return displayableArenas;
    }

    @Override
    public @Nullable DisplayableArena getFromTag(String tag) {
        return getArenas().stream().filter(a -> a.getTag().equals(tag)).findFirst().orElse(null);
    }

    @Override
    public boolean isEnableArenaSelector() {
        return ServerManager.getINSTANCE().getServerType() == ServerType.MULTI_ARENA;
    }

    @Override
    public boolean isInGame(Player player) {
        return ArenaManager.getINSTANCE().isInArena(player);
    }

    @Override
    public FastRootCommand getMainCommand() {
        return rootCommand;
    }

    @Override
    public void setMainCommand(FastRootCommand rootCommand) {
        this.rootCommand = rootCommand;
    }

    @Override
    public @Nullable DisplayableArena requestGame(Player player, @Nullable String template) {
        return CommonManager.getINSTANCE().requestGame(player, template);
    }

    @Override
    public PartyHandler getPartyHandler() {
        return PartyManager.getINSTANCE();
    }

    @Override
    public CommonLocaleManager getCommonLocaleManager() {
        return LanguageManager.getINSTANCE();
    }

    @Override
    public boolean hasVipJoin(Player player) {
        return CommonManager.getINSTANCE().hasVipJoin(player);
    }

    @Override
    public CommunicationHandler getPacketsHandler() {
        return communicationHandler;
    }

    @Override
    public void showDebuggingLogs(boolean toggle) {
        ServerManager.getINSTANCE().setDebuggingLogs(toggle);
    }

    @Override
    public boolean isDebuggingLogs() {
        return ServerManager.getINSTANCE().isDebuggingLogs();
    }

    @Override
    public boolean isInSetupSession(Player player) {
        return SetupManager.getINSTANCE().isInSetup(player);
    }

    @Override
    public boolean isInSetupSession(CommandSender sender) {
        return SetupManager.getINSTANCE().isInSetup(sender);
    }

    @Override
    public int getPlayerCount() {
        return ArenaManager.getINSTANCE().getPlayerCount();
    }

    @Override
    public int getSpectatorCount() {
        return ArenaManager.getINSTANCE().getSpectatorCount();
    }

    @Override
    public int getOnlineCount() {
        return ArenaManager.getINSTANCE().getOnlineCount();
    }

    public void remove(DisplayableArena arena) {
        if (displayableArenas.remove(arena)) {
            SelectorManager.getINSTANCE().refreshArenaSelector();
        }
    }

    public void add(DisplayableArena arena) {
        if (!displayableArenas.contains(arena)) {
            displayableArenas.add(arena);
            SelectorManager.getINSTANCE().refreshArenaSelector();
        }
    }

    public static ServerCommonProvider getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ServerCommonProvider();
        }
        return INSTANCE;
    }
}
