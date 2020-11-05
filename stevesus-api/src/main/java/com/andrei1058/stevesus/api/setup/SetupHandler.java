package com.andrei1058.stevesus.api.setup;

import com.andrei1058.spigot.commandlib.fast.FastSubRootCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Server Setup Manager.
 */
public interface SetupHandler {

    /**
     * Get a list of active setup sessions.
     * Keep in mind that this list cannot be modified directly.
     *
     * @return active sessions.
     */
    List<SetupSession> getSetupSessions();

    /**
     * Add a setup session to the active sessions list.
     * Do not forget to start the session.
     *
     * @param setupSession new session.
     */
    void addSession(SetupSession setupSession);

    /**
     * Remove a setup session from the active sessions list.
     * Do not forget to close the session.
     *
     * @param setupSession session to be removed.
     */
    void removeSession(SetupSession setupSession);

    /**
     * Check if the given player is setting up an arena.
     *
     * @param player player ot be checked.
     * @return true if the player is setting up an arena.
     */
    boolean isInSetup(Player player);

    /**
     * Check if the command sender is setting up an arena.
     *
     * @param sender sender to be checked.
     * @return true if sender is a player and is doing a setup.
     */
    boolean isInSetup(CommandSender sender);

    /**
     * Check if a world is already in a setup session.
     *
     * @param name world to be checked.
     * @return true if world is already used in a setup session.
     */
    boolean isWorldInUse(String name);

    /**
     * Create an internal setup session.
     *
     * @param player player admin.
     * @param world  world name.
     * @return true if setup started successfully.
     */
    boolean createSetupSession(Player player, String world);

    /**
     * Get setup session by world name.
     *
     * @param worldName world name.
     * @return session.
     */
    @Nullable
    SetupSession getSession(String worldName);

    /**
     * Get setup session by player.
     *
     * @param player player..
     * @return session.
     */
    @Nullable
    SetupSession getSession(Player player);

    /**
     * Get "set" sub command active in setup session.
     * Useful if you need to add your custom setup commands.
     */
    @SuppressWarnings("UnstableApiUsage")
    FastSubRootCommand getSetCommand();

    /**
     * Get "add" sub command active in setup session.
     * Useful if you need to add your custom setup commands.
     */
    @SuppressWarnings("UnstableApiUsage")
    FastSubRootCommand getAddCommand();

    /**
     * Get "remove" sub command active in setup session.
     * Useful if you need to add your custom setup commands.
     */
    @SuppressWarnings("UnstableApiUsage")
    FastSubRootCommand getRemoveCommand();
}
