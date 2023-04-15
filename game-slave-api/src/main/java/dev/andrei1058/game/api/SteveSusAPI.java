package dev.andrei1058.game.api;

import com.andrei1058.spigot.commandlib.fast.FastRootCommand;
import dev.andrei1058.game.api.arena.ArenaHandler;
import dev.andrei1058.game.api.glow.GlowingHandler;
import dev.andrei1058.game.api.locale.LocaleManager;
import dev.andrei1058.game.api.prevention.PreventionHandler;
import dev.andrei1058.game.api.server.DisconnectHandler;
import dev.andrei1058.game.api.setup.SetupHandler;
import dev.andrei1058.game.common.api.CommonProvider;
import dev.andrei1058.game.common.api.packet.CommunicationHandler;
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
     * Get glowing manager.
     */
    GlowingHandler getGlowingHandler();

    /**
     * Get util that supports multiple versions.
     */
    VersionUtil getVersionUtil();

    /**
     * Get API instance.
     */
    static SteveSusAPI getInstance() {
        return (SteveSusAPI) Bukkit.getPluginManager().getPlugin("SteveSus");
    }
}
