package com.andrei1058.stevesus.server.common;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.meeting.MeetingStage;
import com.andrei1058.stevesus.api.arena.team.Team;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.api.server.ServerType;
import com.andrei1058.stevesus.arena.ArenaManager;
import com.andrei1058.stevesus.common.api.arena.GameState;
import com.andrei1058.stevesus.language.LanguageManager;
import com.andrei1058.stevesus.server.ServerManager;
import com.andrei1058.stevesus.server.multiarena.listener.LobbyProtectionListener;
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
