package com.andrei1058.amoungusmc.connector.api;

import com.andrei1058.amoungusmc.common.api.CommonProvider;
import com.andrei1058.amoungusmc.common.api.locale.CommonLocaleManager;
import com.andrei1058.amoungusmc.common.api.packet.CommunicationHandler;

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
