package com.andrei1058.stevesus.api.arena.team;

import com.andrei1058.stevesus.api.SteveSusAPI;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.locale.Message;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("UnusedReturnValue")
public class PlayerColorAssigner<T extends PlayerColorAssigner.PlayerColor> {

    private final HashMap<UUID, T> playerColor = new HashMap<>();
    private final LinkedList<T> colorOptions = new LinkedList<>();

    /**
     * Check if the given player has an assigned color.
     */
    public boolean hasColor(@NotNull Player player) {
        return hasColor(player.getUniqueId());
    }

    /**
     * Check if the given player has an assigned color.
     */
    public boolean hasColor(@NotNull UUID player) {
        return playerColor.containsKey(player);
    }

    /**
     * @param playerColor      null to remove existing value.
     * @param ignoreColorInUse true if you want to assign this color even if tis already assigned to someone else.
     * @return always true if you ignored color in use, otherwise will return false if the color it's been already chosen.
     */
    public boolean setPlayerColor(@NotNull Player player, @Nullable T playerColor, @NotNull Arena playerArena, boolean ignoreColorInUse) {
        return setPlayerColor(player.getUniqueId(), playerColor, playerArena, ignoreColorInUse);
    }

    /**
     * @param playerColor      null to remove existing value.
     * @param ignoreColorInUse true if you want to assign this color even if tis already assigned to someone else.
     * @return always true if you ignored color in use, otherwise will return false if the color it's been already chosen.
     */
    public boolean setPlayerColor(@Nullable UUID player, @Nullable T playerColor, @NotNull Arena playerArena, boolean ignoreColorInUse) {
        if (playerColor == null) {
            this.playerColor.remove(player);
        } else if (!ignoreColorInUse && isColorInUse(playerColor, playerArena)) {
            return false;
        }
        this.playerColor.put(player, playerColor);
        return true;
    }

    /**
     * This doesn't mean you can't use it twice, but will tell you if it is in use.
     */
    public boolean isColorInUse(@NotNull T playerColor, @NotNull Arena arena) {
        return arena.getPlayers().stream().anyMatch(player -> {
            T color = getPlayerColor(player);
            return color != null && color.equals(playerColor);
        });
    }

    /**
     * IMPORTANT: Colors are assigned after team assignment.
     * No need to use {@link #setPlayerColor(UUID, PlayerColor, Arena, boolean)} after this.
     *
     * @param ignoreAllInUse true if you want to assign a random color even if it's already in use, when no free options left.
     * @return null if no colors left to assign. Ignoring colors in use will never return a null.
     */
    public @Nullable T assignPlayerColor(@NotNull Player player, @NotNull Arena playerArena, boolean ignoreAllInUse) {
        if (getAvailableColorOptions().isEmpty()) {
            throw new IllegalStateException("No colors available");
        }
        boolean allColorsInUse = getAvailableColorOptions().stream().allMatch(color -> isColorInUse(color, playerArena));
        if (allColorsInUse && !ignoreAllInUse) {
            return null;
        }

        T colorResult;
        if (allColorsInUse) {
            // get less used
            final List<T> color = new ArrayList<>();
            T colorTemp = getAvailableColorOptions().get(0);
            color.add(getAvailableColorOptions().get(0));
            int[] usages = {getUsages(colorTemp, playerArena)};
            getAvailableColorOptions().forEach(availableColor -> {
                int currentUsages = getUsages(availableColor, playerArena);
                if (currentUsages < usages[0]) {
                    color.clear();
                    color.add(availableColor);
                    usages[0] = currentUsages;
                }
            });
            colorResult = color.get(0);
        } else {
            // get first available or null
            colorResult = getAvailableColorOptions().stream().filter(currentColor -> !isColorInUse(currentColor, playerArena)).findFirst().orElse(null);
        }
        if (colorResult != null) {
            setPlayerColor(player, colorResult, playerArena, true);
        }
        return colorResult;
    }

    /**
     * Get assigned color.
     */
    public @Nullable T getPlayerColor(Player player) {
        return getPlayerColor(player.getUniqueId());
    }

    /**
     * Get assigned color.
     */
    public @Nullable T getPlayerColor(UUID player) {
        return playerColor.get(player);
    }

    /**
     * Colors are ordered by added order.
     */
    public void addColorOption(T colorOption) {
        if (!this.colorOptions.contains(colorOption)) {
            this.colorOptions.add(colorOption);
            SteveSusAPI.getInstance().getLocaleHandler().getDefaultLocale().addDefault(Message.COLOR_NAME_PATH_.toString() + colorOption.getUniqueIdentifier(),
                    ChatColor.valueOf(colorOption.toString()) + colorOption.getDefaultDisplayName());
        }
    }

    public LinkedList<T> getAvailableColorOptions() {
        return colorOptions;
    }

    /**
     * Clear cached data for the given arena.
     * Usually used at restarts etc.
     */
    @SuppressWarnings("unused")
    public void clearArenaData(Arena arena) {
    }

    /**
     * Restore player data like display name etc on arena leave or when this assigner gets replaced.
     */
    public void restorePlayer(Player player) {
        if (hasColor(player)) {
            playerColor.remove(player.getUniqueId());
            //player.setPlayerListName(null);
            player.setDisplayName(null);
        }
    }

    /**
     * Get how many times a color is used in the given arena.
     */
    public int getUsages(T color, Arena arena) {
        return (int) arena.getPlayers().stream().filter(inGame -> {
            T currentColor = getPlayerColor(inGame);
            if (currentColor == null) {
                return false;
            }
            return currentColor.equals(color);
        }).count();
    }

    public interface PlayerColor {
        /**
         * Apply color and costume etc to the given player.
         */
        void apply(Player player, Arena arena);

        /**
         * Get player head.
         * Used to display player head in voting GUI, eventually in a color selector menu and maybe somewhere else.
         * Do not add name and lore because it will be handled elsewhere.
         */
        @NotNull ItemStack getPlayerHead(Player player, Arena arena);

        /**
         * Color identifier.
         * It is used as well as part of translation path (Ex: {@link com.andrei1058.stevesus.api.locale.Message#COLOR_NAME_PATH_} + legacy-color-red) and maybe more
         */
        @NotNull String getUniqueIdentifier();

        /**
         * Color's default display name which will be automatically saved to language files
         */
        @NotNull String getDefaultDisplayName();

        /**
         * Get color display name. Usually used in TAB.
         */
        @NotNull String getDisplayColor(Player player);
    }
}
