package com.andrei1058.amongusmc.common.hook.papi.provider;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface AdditionalParser {

    /**
     * Fetch additional placeholders from other plugin implementations. Like mini-game-side or connector-side.
     *
     * @param identifier second half identifier. Ex: part after 'bw1058_' -> 'arena_count'.
     * @param player     player requester.
     * @return null if no result for the given identifier.
     */
    @Nullable
    String parseModulePlaceholders(@Nullable OfflinePlayer player, @NotNull String identifier);

    /**
     * Fetch additional placeholders from other plugin implementations. Like mini-game-side or connector-side.
     *
     * @param identifier second half identifier. Ex: part after 'bw1058_' -> 'arena_count'.
     * @param player     player requester.
     * @return null if no result for the given identifier.
     */
    @Nullable
    String parseModulePlaceholders(@Nullable Player player, @NotNull String identifier);
}
