package dev.andrei1058.game.api.arena.task;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.arena.ArenaHandler;
import dev.andrei1058.game.api.setup.SetupSession;
import com.google.gson.JsonObject;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

/**
 * A task handler is able to manage a single task.
 * Register your custom task using {@link ArenaHandler#registerGameTask(TaskProvider)}.
 */
@SuppressWarnings("unused")
public abstract class TaskProvider {

    ///////////////       GENERAL

    /**
     * Task's default display name.
     * Automatically saved in language files.
     */
    public abstract String getDefaultDisplayName();

    /**
     * Task's default description string.
     * Automatically saved in language files.
     */
    public abstract String getDefaultDescription();

    /**
     * Get task name.
     * Used for language paths and more.
     */
    public abstract String getIdentifier();

    /**
     * Get plugin provider of this task.
     */
    public abstract Plugin getProvider();

    /**
     * Get task category.
     * Visual tasks are not a category. Use {@link #isVisual()} instead.
     */
    public abstract TaskType getTaskType();

    /**
     * True if this task has some visual effects or map changes that can spoil
     * if the player is a crew mate.
     */
    public abstract boolean isVisual();


    ///////////////       SETUP

    /**
     * Check if this task can be used in the given setup session.
     * You can use this to do some check if you want to allow this task to be added once etc.
     *
     * @param player       requester.
     * @param setupSession setup session. Retrieve world and more from it.
     */
    public abstract boolean canSetup(Player player, SetupSession setupSession);

    /**
     * Triggered when a player wants to set up this task.
     * <p>
     * This should provide inventory items to the player and other means to set task requirements.
     * Other setup commands are blocked white player is setting up a task.
     * <p>
     * IMPORTANT: when task setup is done, mark it as finished (to allow commands usage)
     * via {@link SetupSession#setAllowCommands(boolean)} - set to true.
     * Also make sure to save your data using {@link ArenaHandler#saveTaskData(TaskProvider, SetupSession, String, JsonObject)}.
     *
     * @param player       admin doing setup.
     * @param setupSession setup session.
     * @param localName    name assigned by server owner to this task so he can remember this configuration.
     */
    public abstract void onSetupRequest(Player player, SetupSession setupSession, String localName);

    /**
     * This is called when a world is loaded for being set.
     * Already configured tasks will trigger this method and you should use it
     * to protect custom entities removal or to put some holograms on the map to let
     * the player know the locations he set etc.
     * <p>
     * Can be ignored, especially if you're using meta data on your placed entities or tags etc.
     */
    public abstract void onSetupLoad(SetupSession setupSession, String localName, JsonObject configData);

    /**
     * This is called when a setup session is interrupted.
     * Use it to cancel your tasks etc.
     */
    public abstract void onSetupClose(SetupSession setupSession, String localName, JsonObject configData);

    /**
     * IMPORTANT.
     * This is triggered when a player used the remove command.
     * Use this method to clear your task map modifications etc.
     */
    public abstract void onRemove(SetupSession setupSession, String localName, JsonObject configData);


    ///////////////       GAME USAGE

    /**
     * Initialize this task for the given arena.
     * Called when game world is loaded.
     *
     * @param gameArena         target arena.
     * @param configuration configuration loaded from config.
     * @param localName
     * @return null if cannot initialize. Otherwise return a new instance that can be used per arena.
     */
    @Nullable
    public abstract GameTask onGameInit(GameArena gameArena, JsonObject configuration, String localName);


    ////////////////////////////////
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TaskProvider) {
            return ((TaskProvider) obj).getProvider().equals(getProvider()) && ((TaskProvider) obj).getIdentifier().equals(getIdentifier());
        }
        return false;
    }

    /**
     * Check if given json has required sub elements.
     *
     * @return true if all fine.
     */
    protected static boolean validateElements(JsonObject json, String... child) {
        for (String entry : child) {
            if (json.get(entry) == null) {
                return false;
            }
        }
        return true;
    }
}
