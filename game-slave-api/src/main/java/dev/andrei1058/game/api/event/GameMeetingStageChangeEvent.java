package dev.andrei1058.game.api.event;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.arena.meeting.MeetingStage;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameMeetingStageChangeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final GameArena gameArena;
    private final MeetingStage oldStage;
    private final MeetingStage newStage;

    public GameMeetingStageChangeEvent(GameArena gameArena, MeetingStage oldStage, MeetingStage newStage) {
        this.gameArena = gameArena;
        this.oldStage = oldStage;
        this.newStage = newStage;
    }

    public MeetingStage getOldStage() {
        return oldStage;
    }

    public MeetingStage getNewStage() {
        return newStage;
    }

    public GameArena getArena() {
        return gameArena;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}