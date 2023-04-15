package dev.andrei1058.game.arena.runnable;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.locale.Message;
import dev.andrei1058.game.language.LanguageManager;
import dev.andrei1058.game.server.ServerManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class ArenaTaskRestarting implements Runnable {

    private final GameArena gameArena;

    public ArenaTaskRestarting(GameArena gameArena) {
        this.gameArena = gameArena;
        gameArena.setCountdown(20);
    }

    public GameArena getArena() {
        return gameArena;
    }

    @Override
    public void run() {
        int currentCountdown;
        getArena().setCountdown(currentCountdown = getArena().getCountdown() - 1);

        if (currentCountdown == 4) {
            if (getArena().getWorld() != null) {
                for (Player player : getArena().getWorld().getPlayers()) {
                    if (getArena().isPlayer(player)) {
                        getArena().removePlayer(player, false);
                    } else if (getArena().isSpectator(player)) {
                        getArena().removeSpectator(player, false);
                    } else {
                        ServerManager.getINSTANCE().getDisconnectHandler().performDisconnect(player);
                    }
                }
            }
        }

        getArena().getPlayers().forEach(player -> player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(LanguageManager.getINSTANCE().getMsg(player,
                Message.ACTION_MESSAGE_STATE_ENDING).replace("{countdown}", String.valueOf(currentCountdown)))));

        if (currentCountdown <= 0) {
            getArena().restart();
        }
    }
}
