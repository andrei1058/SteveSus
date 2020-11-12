package com.andrei1058.stevesus.arena.runnable;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.MeetingStage;
import org.bukkit.Bukkit;

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
        if (getArena().getMeetingStage() != MeetingStage.NO_MEETING) {
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
                getArena().setCountdown(getArena().getCountdown() - 1);
            }
        }
    }
}
