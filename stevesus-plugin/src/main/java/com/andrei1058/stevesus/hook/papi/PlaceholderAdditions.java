package com.andrei1058.stevesus.hook.papi;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.arena.ArenaHandler;
import com.andrei1058.stevesus.common.hook.papi.provider.AdditionalParser;
import com.andrei1058.stevesus.language.LanguageManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderAdditions implements AdditionalParser {

    @Override
    public @Nullable String parseModulePlaceholders(OfflinePlayer player, @NotNull String identifier) {
        return null;
    }

    @Override
    public @Nullable String parseModulePlaceholders(@Nullable Player player, @NotNull String identifier) {
        if (identifier.startsWith("game_")) {
            if (identifier.length() < 6) {
                return null;
            }
            if (player == null) {
                return null;
            }
            Arena arena = ArenaHandler.getINSTANCE().getArenaByPlayer(player);
            if (arena == null) return null;
            String subRequest = identifier.substring(5);
            switch (subRequest) {
                case "user_count_current":
                    return String.valueOf(arena.getCurrentPlayers() + arena.getCurrentSpectators());
                case "player_count_current":
                    return String.valueOf(arena.getCurrentPlayers());
                case "spectator_count_current":
                    return String.valueOf(arena.getCurrentSpectators());
                case "state_current":
                    return LanguageManager.getINSTANCE().getMsg(player, arena.getGameState().getTranslatePath());
                case "is_spectator":
                    return String.valueOf(arena.isSpectator(player));
                case "is_player":
                    return String.valueOf(arena.isPlayer(player));
            }
        }
        return null;
    }
}
