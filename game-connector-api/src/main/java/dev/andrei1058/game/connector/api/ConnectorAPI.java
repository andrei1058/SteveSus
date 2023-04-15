package dev.andrei1058.game.connector.api;

import dev.andrei1058.game.common.api.CommonProvider;
import dev.andrei1058.game.common.api.locale.CommonLocaleManager;
import dev.andrei1058.game.common.api.packet.CommunicationHandler;

@SuppressWarnings("unused")
public interface ConnectorAPI {

    /**
     * Common parts between main mini-game plugin and connector plugin.
     *
     * @return commons interface.
     */
    CommonProvider getCommonProvider();

    /**
     * Get server language handler interface.
     */
    CommonLocaleManager getLocaleManager();

    /**
     * Packets manager to communicate with remote arenas.
     * This is used to send and receive custom data from remote arenas.
     */
    CommunicationHandler getPacketsHandler();
}
