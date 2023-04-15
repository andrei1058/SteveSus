package com.andrei1058.stevesus.arena.sabotage.listener;

import com.andrei1058.stevesus.api.arena.meeting.MeetingStage;
import com.andrei1058.stevesus.api.event.GameMeetingStageChangeEvent;
import com.andrei1058.stevesus.api.event.GameSabotageActivateEvent;
import com.andrei1058.stevesus.api.event.GameSabotageDeactivateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Restore some details on sabotage fix.
 * Like pause/ unpause sabotage cool down.
 */
public class SabotageListener implements Listener {


    @EventHandler(priority = EventPriority.MONITOR)
    public void onSabotageFix(GameSabotageDeactivateEvent event){
        if (event.isForceDisable()) return;
        // try unpause sabotage cool down on sabotage fix
        if (event.getArena().getSabotageCooldown() != null){
            event.getArena().getSabotageCooldown().tryUnPause();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSabotageStart(GameSabotageActivateEvent event){
        // try pause sabotage cool down
        if (event.getArena().getSabotageCooldown() != null){
            event.getArena().getSabotageCooldown().applyCooldown();
            event.getArena().getSabotageCooldown().tryPause();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMeeting(GameMeetingStageChangeEvent event){
        if (event.getNewStage() == MeetingStage.NO_MEETING){
            // try unpause sabotage cool down on meeting end
            if (event.getArena().getSabotageCooldown() != null){
                event.getArena().getSabotageCooldown().tryUnPause();
            }
        } else {
            if (event.getArena().getSabotageCooldown() != null && !event.getArena().getSabotageCooldown().isPaused()){
                event.getArena().getSabotageCooldown().tryPause();
            }
        }
    }
}
