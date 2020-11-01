package com.andrei1058.stevesus.common.hook.papi.fetcher;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PAPIFallBack implements PAPIHook{

    @Override
    public @NotNull String parsePlaceholders(@NotNull Player player, @NotNull String message) {
        return message;
    }

    @Override
    public @NotNull String parsePlaceholders(@NotNull OfflinePlayer player, @NotNull String message) {
        return message;
    }
}
