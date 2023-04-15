package com.andrei1058.stevesus.api.event;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.meeting.MeetingStage;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameMeetingStageChangeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Arena arena;
    private final MeetingStage oldStage;
    private final MeetingStage newStage;

    public GameMeetingStageChangeEvent(Arena arena, MeetingStage oldStage, MeetingStage newStage) {
        this.arena = arena;
        this.oldStage = oldStage;
        this.newStage = newStage;
    }

    public MeetingStage getOldStage() {
        return oldStage;
    }

    public MeetingStage getNewStage() {
        return newStage;
    }

    public Arena getArena() {
        return arena;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}