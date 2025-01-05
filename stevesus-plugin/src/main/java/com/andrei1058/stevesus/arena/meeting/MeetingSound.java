package com.andrei1058.stevesus.arena.meeting;

import co.aikar.taskchain.TaskChain;
import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

public class MeetingSound {

    private MeetingSound() {
    }

    public static void playMusic(Arena arena, int startAfter) {
        TaskChain<?> chain = SteveSus.newChain();
        if (startAfter > 0) {
            chain.delay(startAfter);
        }
        chain.sync(() -> {
            arena.getPlayers().forEach(player -> {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, (float) 0.707107);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1, (float) 0.707107);
            });
        }).delay(16).sync(() -> {
            arena.getPlayers().forEach(player -> {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1, (float) 1.414214);
            });
        }).delay(7).sync(() -> {
            arena.getPlayers().forEach(player -> {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1, (float) 1.681793);
            });
        }).delay(7).sync(() -> {
            arena.getPlayers().forEach(player -> {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1, (float) 1.887749);
            });
        }).delay(16).sync(() -> {
            arena.getPlayers().forEach(player -> {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1, (float) 2);
            });
        }).execute();
    }
}
