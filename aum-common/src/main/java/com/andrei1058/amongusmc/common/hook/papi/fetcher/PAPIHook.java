package com.andrei1058.amongusmc.common.hook.papi.fetcher;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface PAPIHook {

    /**
     * Translate placeholders if PAPI is loaded.
     * Otherwise will return the original string.
     */
    @NotNull
    String parsePlaceholders(@NotNull Player player, @NotNull String message);

    /**
     * Translate placeholders if PAPI is loaded.
     * Otherwise will return the original string.
     */
    @NotNull
    String parsePlaceholders(@NotNull OfflinePlayer player, @NotNull String message);

    /**
     * Integration level should help the user have control
     * over server performance. Parsing all plugin messages
     * for placeholders may affect performance and using this
     * enum the owner can reduce integration level or disable it at all.
     */
    enum IntegrationLevel {
        /** All player messages*/
        FULL,
        PARTIAL,
        DISABLED
    }
}
