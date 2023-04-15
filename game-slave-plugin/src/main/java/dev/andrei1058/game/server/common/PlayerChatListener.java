package dev.andrei1058.game.server.common;

import dev.andrei1058.game.api.arena.Arena;
import dev.andrei1058.game.api.arena.meeting.MeetingStage;
import dev.andrei1058.game.api.arena.team.Team;
import dev.andrei1058.game.api.locale.Message;
import dev.andrei1058.game.api.server.ServerType;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.common.api.arena.GameState;
import dev.andrei1058.game.language.LanguageManager;
import dev.andrei1058.game.server.ServerManager;
import dev.andrei1058.game.server.multiarena.listener.LobbyProtectionListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (e == null) return;
        if (e.isCancelled()) return;
        final Player player = e.getPlayer();
        final Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer(e.getPlayer());

        if (arena == null) {
            if (ServerManager.getINSTANCE().getServerType() == ServerType.MULTI_ARENA && LobbyProtectionListener.getLobbyWorld() != null) {
                if (player.getWorld().getName().equals(LobbyProtectionListener.getLobbyWorld())) {
                    // format lobby
                    e.getRecipients().removeIf(receiver -> !receiver.getWorld().equals(player.getWorld()));
                    String formatted = LanguageManager.getINSTANCE().getMsg(player, Message.CHAT_FORMAT_LOBBY_WORLD);
                    // placeholders: {message} msg, {player} display name, {name} raw name, {rank_prefix} rank, {rank_suffix} rank
                    e.setFormat(formatted.replace("{message}", "%2$s").replace("{player}", player.getDisplayName()).replace("{name}", player.getName()));

                }
            }
        } else {
            Team playerTeam = arena.getPlayerTeam(player);
            e.getRecipients().removeIf(receiver -> !receiver.getWorld().equals(player.getWorld()) || (playerTeam != null && playerTeam.chatFilter(receiver)));
            if (arena.getGameState() == GameState.IN_GAME) {
                if (!(arena.getMeetingStage() == MeetingStage.TALKING || arena.getMeetingStage() == MeetingStage.VOTING)) {
                    player.sendMessage(LanguageManager.getINSTANCE().getMsg(player, Message.TALK_ALLOWED_DURING_MEETINGS));
                    e.setCancelled(true);
                    return;
                }
            }
            Message format;
            switch (arena.getGameState()) {
                case WAITING:
                    format = Message.CHAT_FORMAT_WAITING;
                    break;
                case STARTING:
                    format = Message.CHAT_FORMAT_STARTING;
                    break;
                case IN_GAME:
                    format = Message.CHAT_FORMAT_IN_GAME;
                    break;
                default:
                    format = Message.CHAT_FORMAT_ENDING;
                    break;
            }
            String formatted = LanguageManager.getINSTANCE().getMsg(player, format);
            // placeholders: {message} msg, {player} display name, {name} raw name, {rank_prefix} rank, {rank_suffix} rank
            e.setFormat(formatted.replace("{message}", "%2$s").replace("{player}", player.getDisplayName()).replace("{name}", player.getName()));
        }
    }
}
