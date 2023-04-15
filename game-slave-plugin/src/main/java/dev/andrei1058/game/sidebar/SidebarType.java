package dev.andrei1058.game.sidebar;

import dev.andrei1058.game.api.arena.Arena;
import dev.andrei1058.game.api.locale.Locale;
import dev.andrei1058.game.api.locale.Message;
import dev.andrei1058.game.language.LanguageManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public enum SidebarType {

    MULTI_ARENA_LOBBY(Message.SCOREBOARD_SIDEBAR_LOBBY),
    WAITING(Message.SCOREBOARD_SIDEBAR_WAITING),
    STARTING(Message.SCOREBOARD_SIDEBAR_STARTING),
    IN_GAME(Message.SCOREBOARD_SIDEBAR_IN_GAME),
    ENDING(Message.SCOREBOARD_SIDEBAR_ENDING),
    SPECTATOR(Message.SCOREBOARD_SIDEBAR_SPECTATOR);

    private final Message contentPath;

    SidebarType(Message contentPath) {
        this.contentPath = contentPath;
    }

    /**
     * Get scoreboard content for current type.
     *
     * @param arena arena if it is in-game scoreboard.
     */
    @Nullable
    public List<String> getContent(@NotNull Locale language, @Nullable Arena arena) {
        List<String> content = null;
        if (arena == null) {
            content = language.getRawList(contentPath.toString());
        } else {
            if (language.hasPath(contentPath.toString() + "-" + arena.getTemplateWorld())) {
                content = language.getRawList(contentPath.toString() + "-" + arena.getTemplateWorld());
            } else if (!language.equals(LanguageManager.getINSTANCE().getDefaultLocale()) && LanguageManager.getINSTANCE().getDefaultLocale().hasPath(contentPath.toString() + "-" + arena.getTemplateWorld())) {
                content = LanguageManager.getINSTANCE().getDefaultLocale().getRawList(contentPath.toString() + "-" + arena.getTemplateWorld());
            } else if (language.hasPath(contentPath.toString())) {
                content = language.getRawList(contentPath.toString());
            }
        }
        return content == null ? null : new ArrayList<>(content);
    }
}