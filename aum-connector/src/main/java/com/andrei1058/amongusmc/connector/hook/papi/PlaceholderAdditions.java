package com.andrei1058.amongusmc.connector.hook.papi;

import com.andrei1058.amongusmc.common.hook.papi.provider.AdditionalParser;
import com.andrei1058.amongusmc.connector.arena.ArenaManager;
import com.andrei1058.amoungusmc.connector.api.arena.RemoteArena;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderAdditions implements AdditionalParser {

    @Override
    public @Nullable String parseModulePlaceholders(@Nullable OfflinePlayer player, @NotNull String identifier) {
        return parseNoPlayer(identifier);
    }

    @Override
    public @Nullable String parseModulePlaceholders(@Nullable Player player, @NotNull String identifier) {
        return parseNoPlayer(identifier);
    }

    protected static String parseNoPlayer(String identifier) {
        if (identifier.startsWith("game_count_node_")) {
            if (identifier.length() < 17) {
                return null;
            }
            String slave = identifier.substring(16);
            return String.valueOf((int) ArenaManager.getInstance().getArenas().stream().filter(arena -> arena instanceof RemoteArena)
                    .filter(remote -> ((RemoteArena) remote).getServer() != null && ((RemoteArena) remote).getServer().getName().equals(slave)).count());
        }
        return null;
    }
}
