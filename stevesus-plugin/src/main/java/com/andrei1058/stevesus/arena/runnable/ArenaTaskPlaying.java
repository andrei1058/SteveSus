package com.andrei1058.stevesus.arena.runnable;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.meeting.MeetingStage;
import com.andrei1058.stevesus.api.arena.room.GameRoom;
import com.andrei1058.stevesus.api.arena.sabotage.SabotageBase;
import com.andrei1058.stevesus.api.arena.sabotage.TimedSabotage;
import com.andrei1058.stevesus.api.locale.Locale;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.api.server.GameSound;
import com.andrei1058.stevesus.arena.meeting.ExclusionGUI;
import com.andrei1058.stevesus.language.LanguageManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class ArenaTaskPlaying implements Runnable {

    private final Arena arena;

    public ArenaTaskPlaying(Arena arena) {
        this.arena = arena;
    }

    public Arena getArena() {
        return arena;
    }

    @Override
    public void run() {
        if (getArena().getMeetingStage() == MeetingStage.NO_MEETING) {
            for (SabotageBase sabotage : getArena().getLoadedSabotages()){
                if (sabotage instanceof TimedSabotage){
                    TimedSabotage timed = (TimedSabotage) sabotage;
                    timed.doTick();
                }
            }
            for (Player player : getArena().getPlayers()){
                GameRoom room = getArena().getPlayerRoom(player);
                Locale playerLang = LanguageManager.getINSTANCE().getLocale(player);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(playerLang.getMsg(player, Message.IN_GAME_ACTION_BAR).replace("{player}", player.getDisplayName()).replace("{room}", (room == null ? playerLang.getMsg(null, Message.GAME_ROOM_NO_NAME) : room.getDisplayName(playerLang)))));
            }
            if (getArena().getSabotageCooldown() != null){
                if (getArena().getSabotageCooldown().isPaused()){
                    getArena().getSabotageCooldown().updateCooldownOnItems();
                }
            }
        } else {
            if (getArena().getMeetingButton() != null) {
                getArena().getMeetingButton().refreshLines(getArena());
            }
            if (getArena().getCountdown() <= 1) {
                switch (getArena().getMeetingStage()) {
                    case TALKING:
                        getArena().setMeetingStage(MeetingStage.VOTING);
                        break;
                    case VOTING:
                        getArena().setMeetingStage(MeetingStage.EXCLUSION_SCREEN);
                        break;
                    default:
                        getArena().setMeetingStage(MeetingStage.NO_MEETING);
                        break;
                }
            } else {
                if (getArena().getMeetingStage() == MeetingStage.VOTING){
                    getArena().setCountdown(getArena().getCountdown() - 1);
                    if (getArena().getCountdown() <= 5){
                        GameSound.VOTING_ENDS_TICK.playToPlayers(arena.getPlayers());
                        GameSound.VOTING_ENDS_TICK.playToPlayers(arena.getSpectators());
                    }
                    getArena().getWorld().getPlayers().forEach(player -> {
                        if (player.getOpenInventory() != null){
                            if (player.getOpenInventory().getTopInventory().getHolder() != null){
                                if (player.getOpenInventory().getTopInventory().getHolder() instanceof ExclusionGUI.ExclusionHolder){
                                    ((ExclusionGUI.ExclusionHolder) player.getOpenInventory().getTopInventory().getHolder()).refresh();
                                }
                            }
                        }
                    });
                }
            }
        }
    }
}
