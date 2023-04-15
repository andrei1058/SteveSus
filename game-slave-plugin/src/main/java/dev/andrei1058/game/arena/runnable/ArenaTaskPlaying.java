package dev.andrei1058.game.arena.runnable;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.arena.meeting.MeetingStage;
import dev.andrei1058.game.api.arena.room.GameRoom;
import dev.andrei1058.game.api.arena.sabotage.SabotageBase;
import dev.andrei1058.game.api.arena.sabotage.TimedSabotage;
import dev.andrei1058.game.api.locale.Locale;
import dev.andrei1058.game.api.locale.Message;
import dev.andrei1058.game.api.server.GameSound;
import dev.andrei1058.game.arena.meeting.ExclusionGUI;
import dev.andrei1058.game.language.LanguageManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class ArenaTaskPlaying implements Runnable {

    private final GameArena gameArena;

    public ArenaTaskPlaying(GameArena gameArena) {
        this.gameArena = gameArena;
    }

    public GameArena getArena() {
        return gameArena;
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
                // needs to stay here
                getArena().setCountdown(getArena().getCountdown() - 1);
                if (getArena().getMeetingStage() == MeetingStage.VOTING){
                    if (getArena().getCountdown() <= 5){
                        GameSound.VOTING_ENDS_TICK.playToPlayers(gameArena.getPlayers());
                        GameSound.VOTING_ENDS_TICK.playToPlayers(gameArena.getSpectators());
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
