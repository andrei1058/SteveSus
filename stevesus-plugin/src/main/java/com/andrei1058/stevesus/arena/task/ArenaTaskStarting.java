package com.andrei1058.stevesus.arena.task;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.locale.Locale;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.api.server.GameSound;
import com.andrei1058.stevesus.common.api.arena.GameState;
import com.andrei1058.stevesus.language.LanguageManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

public class ArenaTaskStarting implements Runnable {

    private final Arena arena;

    public ArenaTaskStarting(Arena arena) {
        this.arena = arena;
    }

    public Arena getArena() {
        return arena;
    }

    @Override
    public void run() {
        int currentSecond;
        getArena().setCountdown(currentSecond = getArena().getCountdown() - 1);
        if (currentSecond == 0) {
            arena.getPlayers().forEach(HumanEntity::closeInventory);
            getArena().switchState(GameState.IN_GAME);
            return;
        }

        for (Player player : getArena().getPlayers()) {
            GameSound.CountdownSoundCache.playForSecond(getArena(), currentSecond);
            Locale playerLocale = LanguageManager.getINSTANCE().getLocale(player);
            String title = playerLocale.getMsg(player, Message.COUNT_DOWN_TITLE_PATH.toString() + currentSecond);
            if (title.isEmpty()) title = " ";
            String subTitle = playerLocale.getMsg(player, Message.COUNT_DOWN_SUBTITLE_PATH.toString() + currentSecond);
            if (subTitle.isEmpty()) subTitle = " ";
            player.sendTitle(title, subTitle, 0, 20, 0);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(playerLocale.getMsg(player, Message.ACTION_MESSAGE_STATE_STARTING).replace("{countdown}", String.valueOf(currentSecond))));
        }
    }
}
