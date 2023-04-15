package dev.andrei1058.game.arena.meeting;

import co.aikar.taskchain.TaskChain;
import dev.andrei1058.game.SteveSus;
import dev.andrei1058.game.api.arena.GameArena;
import org.bukkit.Sound;

public class MeetingSound {

    private MeetingSound() {
    }

    public static void playMusic(GameArena gameArena, int startAfter) {
        TaskChain<?> chain = SteveSus.newChain();
        if (startAfter > 0) {
            chain.delay(startAfter);
        }
        chain.sync(() -> {
            gameArena.getPlayers().forEach(player -> {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, (float) 0.707107);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_HARP, 1, (float) 0.707107);
            });
        }).delay(16).sync(() -> {
            gameArena.getPlayers().forEach(player -> {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_HARP, 1, (float) 1.414214);
            });
        }).delay(7).sync(() -> {
            gameArena.getPlayers().forEach(player -> {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_HARP, 1, (float) 1.681793);
            });
        }).delay(7).sync(() -> {
            gameArena.getPlayers().forEach(player -> {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_HARP, 1, (float) 1.887749);
            });
        }).delay(16).sync(() -> {
            gameArena.getPlayers().forEach(player -> {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_HARP, 1, (float) 2);
            });
        }).execute();
    }
}
