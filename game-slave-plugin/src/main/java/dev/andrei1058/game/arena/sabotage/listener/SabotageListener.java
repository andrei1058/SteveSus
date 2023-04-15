package dev.andrei1058.game.arena.sabotage.listener;

import dev.andrei1058.game.api.arena.meeting.MeetingStage;
import dev.andrei1058.game.api.event.GameMeetingStageChangeEvent;
import dev.andrei1058.game.api.event.GameSabotageActivateEvent;
import dev.andrei1058.game.api.event.GameSabotageDeactivateEvent;
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
