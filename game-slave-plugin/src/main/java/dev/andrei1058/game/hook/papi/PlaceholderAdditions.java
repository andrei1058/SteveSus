package dev.andrei1058.game.hook.papi;

import dev.andrei1058.game.api.arena.GameArena;
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
            GameArena gameArena = ArenaManager.getINSTANCE().getArenaByPlayer(player);
            if (gameArena == null) return null;
            String subRequest = identifier.substring(5);
            switch (subRequest) {
                case "user_count_current":
                    return String.valueOf(gameArena.getCurrentPlayers() + gameArena.getCurrentSpectators());
                case "player_count_current":
                    return String.valueOf(gameArena.getCurrentPlayers());
                case "spectator_count_current":
                    return String.valueOf(gameArena.getCurrentSpectators());
                case "state_current":
                    return LanguageManager.getINSTANCE().getMsg(player, gameArena.getGameState().getTranslatePath());
                case "is_spectator":
                    return String.valueOf(gameArena.isSpectator(player));
                case "is_player":
                    return String.valueOf(gameArena.isPlayer(player));
                case "tasks_short":
                    return String.valueOf(gameArena.getLiveSettings().getShortTasks().getCurrentValue());
                case "tasks_long":
                    return String.valueOf(gameArena.getLiveSettings().getLongTasks().getCurrentValue());
                case "tasks_common":
                    return String.valueOf(gameArena.getLiveSettings().getCommonTasks().getCurrentValue());
                case "tasks_visual":
                    return String.valueOf(gameArena.getLiveSettings().isVisualTasksEnabled());
                case "kills":
                    return String.valueOf(gameArena.getStats().getKills(player.getUniqueId()));
                case "sabotages":
                    return String.valueOf(gameArena.getStats().getSabotages(player.getUniqueId()));
                case "sabotages_fixed":
                    return String.valueOf(gameArena.getStats().getFixedSabotages(player.getUniqueId()));
                case "tasks":
                    return String.valueOf(gameArena.getStats().getTasks(player.getUniqueId()));
            }
        }
        return null;
    }
}
