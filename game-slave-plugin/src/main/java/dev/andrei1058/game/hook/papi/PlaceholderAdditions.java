package dev.andrei1058.game.hook.papi;

import dev.andrei1058.game.api.arena.Arena;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.common.hook.papi.provider.AdditionalParser;
import dev.andrei1058.game.language.LanguageManager;
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
            Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer(player);
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
                case "tasks_short":
                    return String.valueOf(arena.getLiveSettings().getShortTasks().getCurrentValue());
                case "tasks_long":
                    return String.valueOf(arena.getLiveSettings().getLongTasks().getCurrentValue());
                case "tasks_common":
                    return String.valueOf(arena.getLiveSettings().getCommonTasks().getCurrentValue());
                case "tasks_visual":
                    return String.valueOf(arena.getLiveSettings().isVisualTasksEnabled());
                case "kills":
                    return String.valueOf(arena.getStats().getKills(player.getUniqueId()));
                case "sabotages":
                    return String.valueOf(arena.getStats().getSabotages(player.getUniqueId()));
                case "sabotages_fixed":
                    return String.valueOf(arena.getStats().getFixedSabotages(player.getUniqueId()));
                case "tasks":
                    return String.valueOf(arena.getStats().getTasks(player.getUniqueId()));
            }
        }
        return null;
    }
}
