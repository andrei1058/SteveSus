package com.andrei1058.stevesus.sidebar;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.locale.Locale;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.language.LanguageManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public List<String> getContent(@NotNull Locale language, @Nullable Arena arena) {
        List<String> content = null;
        if (arena != null) {
            if (language.hasPath(contentPath.toString() + "-" + arena.getTemplateWorld())) {
                content = language.getRawList(contentPath.toString() + "-" + arena.getTemplateWorld());
            } else if (!language.equals(LanguageManager.getINSTANCE().getDefaultLocale()) && LanguageManager.getINSTANCE().getDefaultLocale().hasPath(contentPath.toString() + "-" + arena.getTemplateWorld())) {
                content = LanguageManager.getINSTANCE().getDefaultLocale().getRawList(contentPath.toString() + "-" + arena.getTemplateWorld());
            } else if (language.hasPath(contentPath.toString())) {
                content = language.getRawList(contentPath.toString());
            }
        }
        if (content == null) {
            content = LanguageManager.getINSTANCE().getDefaultLocale().getRawList(contentPath.toString());
        }
        return content;
    }
}
