package com.andrei1058.amongusmc.connector.common;

import com.andrei1058.amongusmc.common.CommonManager;
import com.andrei1058.amongusmc.common.party.PartyManager;
import com.andrei1058.amongusmc.connector.arena.ArenaManager;
import com.andrei1058.amongusmc.connector.language.LanguageManager;
import com.andrei1058.amongusmc.connector.socket.LobbyCommunicationHandler;
import com.andrei1058.amoungusmc.common.api.CommonProvider;
import com.andrei1058.amoungusmc.common.api.arena.DisplayableArena;
import com.andrei1058.amoungusmc.common.api.locale.CommonLocaleManager;
import com.andrei1058.amoungusmc.common.api.packet.CommunicationHandler;
import com.andrei1058.amoungusmc.common.api.party.PartyHandler;
import com.andrei1058.spigot.commandlib.FastRootCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConnectorCommonProvider implements CommonProvider {

    private static ConnectorCommonProvider INSTANCE;
    private FastRootCommand rootCommand;
    private final CommunicationHandler communicationHandler = new LobbyCommunicationHandler();
    private static boolean showDebugLogs = false;

    private ConnectorCommonProvider() {
        //todo if used next to main plugin unregister its join commands etc and add local games to this list
    }

    public static ConnectorCommonProvider getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ConnectorCommonProvider();
        }
        return INSTANCE;
    }

    @Override
    public List<DisplayableArena> getArenas() {
        return ArenaManager.getInstance().getArenas();
    }

    @Override
    public @Nullable DisplayableArena getFromTag(String tag) {
        return ArenaManager.getInstance().getFromTag(tag);
    }

    @Override
    public boolean isEnableArenaSelector() {
        return true;
    }

    @Override
    public boolean isInGame(Player player) {
        //todo check local games
        return false;
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
        showDebugLogs = toggle;
    }

    @Override
    public boolean isDebuggingLogs() {
        return showDebugLogs;
    }

    @Override
    public boolean isInSetupSession(Player player) {
        return false;
    }

    @Override
    public boolean isInSetupSession(CommandSender sender) {
        return false;
    }

    @Override
    public int getPlayerCount() {
        return ArenaManager.getInstance().getPlayerCount();
    }

    @Override
    public int getSpectatorCount() {
        return ArenaManager.getInstance().getSpectatorCount();
    }

    @Override
    public int getOnlineCount() {
        return ArenaManager.getInstance().getOnlineCount();
    }
}
