package dev.andrei1058.game.common.api.arena;

import dev.andrei1058.game.common.api.CommonProvider;
import dev.andrei1058.game.common.api.gui.slot.SlotHolder;
import dev.andrei1058.game.common.api.locale.CommonLocale;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DisplayableArena extends SlotHolder, Comparable<DisplayableArena> {

    /**
     * Get game id.
     *
     * @return game id.
     */
    int getGameId();

    /**
     * Get arena game state.
     *
     * @return arena status.
     */
    GameState getGameState();

    /**
     * Check if the arena is full.
     * Usually used at waiting and starting.
     */
    boolean isFull();

    /**
     * Check if this arena allows spectating.
     *
     * @return true if allows spectating.
     */
    String getSpectatePermission();

    /**
     * Get arena display name.
     *
     * @return arena display name.
     */
    String getDisplayName();

    /**
     * Get max mount of players allowed to join.
     *
     * @return max players.
     */
    int getMaxPlayers();

    /**
     * Get required players to start countdown.
     *
     * @return required players amount to start countdown.
     */
    int getMinPlayers();

    /**
     * Get currently playing.
     */
    int getCurrentPlayers();

    /**
     * Get currently spectating.
     */
    int getCurrentSpectators();

    /**
     * Get template world.
     *
     * @return world name from which it was cloned.
     */
    String getTemplateWorld();

    /**
     * This is how we identify arenas when you click them in a GUI.
     *
     * @return arena identifier.
     */
    String getTag();

    /**
     * Add a player to the game.
     * This must be used only on WAITING and STARTING.
     * Will return false if player is already in a game.
     *
     * @param player      player to be added.
     * @param ignoreParty if it doesn't matter if he is the party owner.
     * @return true if added successfully.
     */
    boolean joinPlayer(Player player, boolean ignoreParty);

    /**
     * Join the game as Spectator.
     *
     * @param player player to be added.
     * @param target start spectating this player (UUID). Null for none.
     * @return if added to spectators successfully.
     */
    boolean joinSpectator(Player player, @Nullable String target);

    default int compareTo(@NotNull DisplayableArena other) {
        if (other.getGameState() == GameState.STARTING && getGameState() == GameState.STARTING) {
            return Integer.compare(other.getCurrentPlayers(), getCurrentPlayers());
        }
        return Integer.compare(other.getGameState().getStateCode(), getGameState().getStateCode());
    }

    /**
     * Make sure to add arena id tag at this key {@link CommonProvider#getDisplayableArenaNBTTagKey()}.
     *
     * @param lang if null will return item stack only without messages.
     */
    ItemStack getDisplayItem(@Nullable CommonLocale lang);

    /**
     * Check if the arena is hosted on this server instance.
     */
    @SuppressWarnings("unused")
    boolean isLocal();
}
