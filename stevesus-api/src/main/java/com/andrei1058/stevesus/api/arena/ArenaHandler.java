package com.andrei1058.stevesus.api.arena;

import com.andrei1058.stevesus.api.arena.task.TaskProvider;
import com.andrei1058.stevesus.api.arena.team.PlayerColorAssigner;
import com.andrei1058.stevesus.api.setup.SetupSession;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;

import java.io.File;
import java.util.List;

public interface ArenaHandler {

    /**
     * Get a game id to assign to your custom arena if not using standard register method.
     *
     * @return next game id.
     */
    int getNextGameId();

    /**
     * Get a game by id.
     *
     * @param id game id.
     * @return null if not found.
     */
    @Nullable
    Arena getArenaById(int id);

    /**
     * Load and start a new game from the given template.
     * A template is an arena configuration.
     *
     * @param worldName start world name.
     */
    @SuppressWarnings("unused")
    void startArenaFromTemplate(String worldName);

    /**
     * Add an arena to the arenas list.
     * Do that only when your arena is ready to be used.
     * If your arena needs to wait for worlds to be loaded use {@link #addToEnableQueue(Arena)}.
     * Use this to register your custom arena to be accessible via gui, commands etc.
     * Internal arenas are added automatically if started by {@link #startArenaFromTemplate(String).}
     * Triggers {@link com.andrei1058.stevesus.api.event.GameInitializedEvent}.
     *
     * @param arena your arena.
     * @return true if added successfully.
     */
    boolean addArena(Arena arena);

    /**
     * Use this to add your arena to the enable queue.
     * Usually used when needs to wait the world to be loaded.
     * This will automatically cal {@link #addArena(Arena)} when the world is loaded.
     * This will trigger {@link com.andrei1058.stevesus.api.world.WorldAdapter#onArenaEnableQueue(String, Arena)}
     *
     * @param arena arena to be added to the enable queue.
     * @return true if successfully added to the queue.
     */
    boolean addToEnableQueue(Arena arena);

    /**
     * Remove an arena from the enable queue.
     * This is automatically called at bukkit-world-load-event if you started
     * your arena using {@link ArenaHandler#startArenaFromTemplate(String)} or if you used {@link ArenaHandler#addToEnableQueue(Arena)} before.
     *
     * @param gameWorld arena associated to this world to be removed from the queue.
     */
    void removeFromEnableQueue(String gameWorld);

    /**
     * Get arena from the enable queue by its game world name.
     *
     * @return arena.
     */
    @Nullable
    Arena getFromEnableQueue(String gameWorld);

    /**
     * Remove an arena from the arenas list.
     * This must be used when the arena is disabled, restarted etc.
     *
     * @param arena arena instance.
     */
    void removeArena(Arena arena);

    /**
     * Get directory where arena configuration is saved.
     *
     * @return arena templates directory.
     */
    File getTemplatesDirectory();

    /**
     * Get a world configuration file path.
     *
     * @param worldName world.
     * @return file.
     */
    File getTemplateFile(String worldName);

    /**
     * Get a list of available map configurations.
     *
     * @return a list of arena configurations.
     */
    List<String> getTemplates();

    /**
     * Check if a player has full join feature which will kick someone (if that someone does not have it).
     *
     * @param player payer to be checked.
     * @return tue if has full join kick feature.
     */
    boolean hasVipJoin(Player player);

    /**
     * Check if a player is playing or spectating a game.
     *
     * @param player player to be checked.
     * @return true if is in an arena.
     */
    boolean isInArena(Player player);

    /**
     * Check if a player spectating a game.
     *
     * @param player player to be checked.
     * @return true if is SPECTATING an arena.
     */
    boolean isSpectating(Player player);

    /**
     * Get a list of active arenas.
     *
     * @return unmodifiable arena list.
     */
    List<Arena> getArenas();

    /**
     * Get list of arena in enable queue.
     *
     * @return unmodifiable list of arenas in enable queue.
     */
    List<Arena> getEnableQueue();

    /**
     * Get the arena where a user is playing or spectating.
     *
     * @param player target player.
     * @return arena or null if not found.
     */
    @Nullable
    Arena getArenaByPlayer(Player player);

    /**
     * Get the arena by world name.
     *
     * @param worldName world name.
     * @return arena or null if not found.
     */
    @Nullable
    Arena getArenaByWorld(@NotNull String worldName);

    /**
     * It is very important to declare in which arena is the player.
     * So if you're using custom {@link Arena} make sure to declare this when a player or spectator is added to your arena.
     * This will simply add the player to a hash map and nothing else, but keeping track of player's arena is trivial.
     *
     * @param player player to be declared.
     * @param arena  target arena. Null if player is no longer in any arena.
     */
    void setArenaByPlayer(Player player, @Nullable Arena arena);

    /**
     * Check an arena configuration.
     *
     * @param templateName config to be verified.
     * @return true if configuration is complete and can be used to create games.
     */
    boolean validateTemplate(String templateName);

    /**
     * Disable an arena.
     * Will remove it from the arenas list.
     * Will trigger {@link com.andrei1058.stevesus.api.world.WorldAdapter#onArenaDisable(Arena)}.
     * Will trigger {@link Arena#disable()}.
     */
    void disableArena(Arena arena);

    /**
     * Get amount of users playing and spectating.
     *
     * @return amount of users playing and spectating.
     */
    int getOnlineCount();

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
     * Register a custom task.
     *
     * @param taskProvider custom task manager.
     */
    boolean registerGameTask(TaskProvider taskProvider);

    /**
     * Get unmodifiable list of registered tasks.
     */
    List<TaskProvider> getRegisteredTasks();

    /**
     * Get a game task.
     *
     * @param provider provider plugin name.
     * @param task     task identifier.
     */
    @Nullable
    TaskProvider getTask(String provider, String task);

    /**
     * Save a task data. Should be used when a player finished setting up a task.
     *
     * @param task         task instance.
     * @param setupSession setup session instance.
     * @param givenName    local identifier used by server owner to identify this configuration.
     */
    void saveTaskData(TaskProvider task, SetupSession setupSession, String givenName, JSONObject taskConfiguration);

    /**
     * Delete task related data.
     *
     * @param setupSession setup session instance.
     * @param givenName    local identifier used by server owner to identify this configuration.
     */
    void deleteTaskData(SetupSession setupSession, String givenName);

    /**
     * Get default player color assigner.
     */
    @Nullable
    PlayerColorAssigner<PlayerColorAssigner.PlayerColor> getDefaultPlayerColorAssigner();

    /**
     * Every arena without a specified color assigner will use this.
     */
    void setDefaultPlayerColorAssigner(@Nullable PlayerColorAssigner<PlayerColorAssigner.PlayerColor> defaultPlayerColorAssigner);
}
