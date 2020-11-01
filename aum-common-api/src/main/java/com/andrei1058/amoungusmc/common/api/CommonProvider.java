package com.andrei1058.amoungusmc.common.api;

import com.andrei1058.amoungusmc.common.api.arena.DisplayableArena;
import com.andrei1058.amoungusmc.common.api.locale.CommonLocaleManager;
import com.andrei1058.amoungusmc.common.api.packet.CommunicationHandler;
import com.andrei1058.amoungusmc.common.api.party.PartyHandler;
import com.andrei1058.spigot.commandlib.FastRootCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Retrieve common data.
 */
public interface CommonProvider {

    /**
     * Get list of available arenas.
     *
     * @return unmodifiable list.
     */
    List<DisplayableArena> getArenas();

    /**
     * Get an arena by its click tag.
     */
    @Nullable
    DisplayableArena getFromTag(String tag);

    /**
     * Check if arena selector is enabled on this server.
     */
    boolean isEnableArenaSelector();

    /**
     * Check if the given player is in game.
     * Used to prevent some command usage like opening game selector.
     */
    boolean isInGame(Player player);

    /**
     * On local it will only provide the arena id.
     * On remote serverName:gameId.
     */
    default String getDisplayableArenaNBTTagKey() {
        return "game-id-1058";
    }

    /**
     * Get Plugin main command so you can add your sub command here.
     */
    FastRootCommand getMainCommand();

    /**
     * Set plugin main command.
     */
    void setMainCommand(FastRootCommand rootCommand);

    /**
     * Used when a player wants to join a certain map.
     *
     * @param player   requester.
     * @param template desired template. Null for no preference. Multiple templates separated by plus. Example: skeld+polus.
     * @return null if no game was found.
     */
    @Nullable
    DisplayableArena requestGame(Player player, @Nullable String template);


    /**
     * Get party manager.
     * Any party related methods should be here.
     */
    PartyHandler getPartyHandler();

    /**
     * Get common language manager.
     * Any language related methods should be here.
     */
    CommonLocaleManager getCommonLocaleManager();

    /**
     * Check if a player has full join feature which will kick someone (if that someone does not have it).
     *
     * @param player payer to be checked.
     * @return tue if has full join kick feature.
     */
    boolean hasVipJoin(Player player);

    /**
     * This is used to send and receive custom data between arena slaves and lobbies.
     */
    CommunicationHandler getPacketsHandler();

    /**
     * Start or disable showing debugging logs in console.
     *
     * @param toggle tue to enable.
     */
    void showDebuggingLogs(boolean toggle);

    /**
     * Check if debug logs are enabled.
     * They can be enabled or disabled using {@link #showDebuggingLogs(boolean)}.
     *
     * @return true if debug logs are enabled.
     */
    boolean isDebuggingLogs();

    /**
     * Check if target is in a setup session.
     *
     * @param player target.
     * @return true if the given user is in a setup session.
     */
    boolean isInSetupSession(Player player);

    /**
     * Check if target is in a setup session.
     *
     * @param sender target.
     * @return true if the given user is in a setup session.
     */
    boolean isInSetupSession(CommandSender sender);

    /**
     * Get amount of players in game globally.
     *
     * @return total players in game globally.
     */
    int getPlayerCount();

    /**
     * Get amount of players spectating.
     *
     * @return amount of players that are spectating globally.
     */
    int getSpectatorCount();

    /**
     * Get amount of users playing or spectating from all arenas.
     *
     * @return amount of users playing or spectating from all arenas.
     */
    int getOnlineCount();
}
