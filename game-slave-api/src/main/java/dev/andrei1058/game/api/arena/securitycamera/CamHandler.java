package dev.andrei1058.game.api.arena.securitycamera;

import dev.andrei1058.game.api.arena.GameArena;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
public interface CamHandler {

    /**
     * Start watching a security camera.
     */
    @SuppressWarnings("UnusedReturnValue")
    boolean startWatching(Player player, GameArena gameArena, SecurityCam cam);

    /**
     * Stop watching a security camera.
     */
    void stopWatching(Player player, GameArena gameArena);

    /**
     * Check if the given player is on the given cam.
     */
    boolean isOnCam(Player player, GameArena gameArena, SecurityCam cam);

    /**
     * Check if the given player is on a cam.
     */
    boolean isOnCam(Player player, GameArena gameArena);

    /**
     * Get security on cam.
     */
    @Nullable
    SecurityCam getPlayerCam(Player player);

    List<SecurityCam> getCams();

    List<SecurityMonitor> getMonitors();

    /**
     * Get list of users watching on cameras.
     */
    List<UUID> getPlayersOnCams();

    /**
     * true if cams usage should be disabled.
     */
    void setSabotaged(boolean toggle);

    /**
     * Check if cams usage is disabled.
     */
    boolean isSabotaged();

    /**
     * Get player clones spawned at the security monitors.
     */
    Collection<Player> getClones();

    @Nullable Player getClone(UUID player);

    void nextCam(Player player, GameArena gameArena);

    void previousCam(Player player, GameArena gameArena);
}
