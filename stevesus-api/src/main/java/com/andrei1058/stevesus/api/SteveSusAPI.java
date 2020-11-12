package com.andrei1058.stevesus.api;

import com.andrei1058.spigot.commandlib.fast.FastRootCommand;
import com.andrei1058.stevesus.api.arena.ArenaHandler;
import com.andrei1058.stevesus.api.locale.LocaleManager;
import com.andrei1058.stevesus.api.prevention.PreventionHandler;
import com.andrei1058.stevesus.api.server.DisconnectHandler;
import com.andrei1058.stevesus.api.setup.SetupHandler;
import com.andrei1058.stevesus.common.api.CommonProvider;
import com.andrei1058.stevesus.common.api.packet.CommunicationHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

@SuppressWarnings("UnstableApiUsage")
public interface SteveSusAPI extends Plugin {

    /**
     * Server Setup Manager.
     *
     * @return setup manager interface.
     */
    SetupHandler getSetupHandler();

    /**
     * Common parts between main mini-game plugin and connector plugin.
     *
     * @return commons interface.
     */
    @SuppressWarnings("unused")
    CommonProvider getCommonProvider();

    /**
     * Get Plugin main command so you can add your sub command here.
     */
    FastRootCommand getMainCommand();

    /**
     * Packets manager for BUNGEE mode servers.
     * This is used to send and receive custom data from remote lobbies.
     */
    CommunicationHandler getPacketsHandler();

    /**
     * This is used to move players to a remote lobby when a game ends or when they do /leave.
     */
    DisconnectHandler getDisconnectHandler();

    void setDisconnectHandler(DisconnectHandler disconnectHandler);

    /**
     * Get abuse and other preventions manager.
     */
    PreventionHandler getPreventionHandler();

    /**
     * Get arena handler.
     */
    ArenaHandler getArenaHandler();

    /**
     * Get locale manager.
     */
    LocaleManager getLocaleHandler();

    /**
     * Get API instance.
     */
    static SteveSusAPI getInstance() {
        return (SteveSusAPI) Bukkit.getPluginManager().getPlugin("SteveSus");
    }
}
