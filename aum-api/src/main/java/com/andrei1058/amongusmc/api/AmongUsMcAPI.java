package com.andrei1058.amongusmc.api;

import com.andrei1058.amongusmc.api.prevention.PreventionHandler;
import com.andrei1058.amongusmc.api.server.DisconnectHandler;
import com.andrei1058.amoungusmc.common.api.packet.CommunicationHandler;
import com.andrei1058.amongusmc.api.setup.SetupHandler;
import com.andrei1058.amoungusmc.common.api.CommonProvider;
import com.andrei1058.spigot.commandlib.FastRootCommand;
import org.bukkit.Bukkit;

public interface AmongUsMcAPI {

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
     * Get API instance.
     */
    static AmongUsMcAPI getInstance() {
        return (AmongUsMcAPI) Bukkit.getServicesManager().getRegistration(AmongUsMcAPI.class);
    }
}
