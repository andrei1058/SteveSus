package dev.andrei1058.game.arena.runnable;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.locale.Locale;
import dev.andrei1058.game.api.locale.Message;
import dev.andrei1058.game.api.server.GameSound;
import dev.andrei1058.game.common.api.arena.GameState;
import dev.andrei1058.game.language.LanguageManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

public class ArenaTaskStarting implements Runnable {

    private final GameArena gameArena;

    public ArenaTaskStarting(GameArena gameArena) {
        this.gameArena = gameArena;
    }

    public GameArena getArena() {
        return gameArena;
    }

    @Override
    public void run() {
        int currentSecond;
        getArena().setCountdown(currentSecond = getArena().getCountdown() - 1);
        if (currentSecond == 0) {
            gameArena.getPlayers().forEach(HumanEntity::closeInventory);
            getArena().switchState(GameState.IN_GAME);
            return;
        } else if (currentSecond == 2) {
            // clear before setting new state to prevent holding ghost items
            getArena().getPlayers().forEach(player -> player.getInventory().clear());
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
