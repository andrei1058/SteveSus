package com.andrei1058.amongusmc.api.world;

import com.andrei1058.amongusmc.api.arena.Arena;
import com.andrei1058.amongusmc.api.setup.SetupSession;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * A World Adapter is used to handle world management.
 */
public interface WorldAdapter {

    /**
     * Get world adapter name.
     *
     * @return adapter name.
     */
    String getAdapterName();

    /**
     * Check if the given world exists in this world container.
     *
     * @param name world name to be checked.
     * @return true if the world exists in this world adapter.
     */
    boolean hasWorld(String name);

    /**
     * Triggered when this world adapter is initialized.
     * Handle your world conversion etc. with this.
     */
    void onAdapterInitialize();

    /**
     * Triggered when an arena world is requested.
     * If you are creating a custom world manager make sure to add the arena to the enable queue list as well.
     * If your world is already loaded and you want to use it do not forget to initialize the arena in this method.
     * If something went wrong remove the arena from the queue {@link com.andrei1058.amongusmc.api.arena.AmongUsArenaManager#removeFromEnableQueue(String)}}.
     *
     * @param arena arena
     */
    void onArenaEnableQueue(String worldName, Arena arena);

    /**
     * Triggered when an arena is restarting.
     * Unload world logic.
     */
    void onArenaRestart(Arena arena);

    /**
     * Triggered when an arena is forced to disable via command etc.
     */
    void onArenaDisable(Arena arena);

    /**
     * Triggered at {@link com.andrei1058.amongusmc.api.setup.SetupHandler#addSession(SetupSession)} and by consequence at {@link com.andrei1058.amongusmc.api.setup.SetupHandler#createSetupSession(Player, String)}.
     *
     * @param worldName    world to be loaded.
     * @param setupSession setup session instance.
     */
    void onSetupSessionStart(String worldName, SetupSession setupSession);

    /**
     * Triggered when setup is done or canceled.
     *
     * @param setupSession setup session.
     */
    void onSetupSessionClose(SetupSession setupSession);

    /**
     * Check if world adapter will automatically import world from bukkit's world container at load/ enable etc.
     *
     * @return true if will import bukkit worlds automatically when required.
     */
    boolean isAutoImport();

    /**
     * Get list of available worlds to be cloned.
     * Include bukkit worlds as well if your adapter is able to import and handle them.
     */
    List<String> getWorlds();

    /**
     * Delete a world by name.
     * Usually used by delete command.
     *
     * @param name world name.
     */
    void deleteWorld(String name);

}
