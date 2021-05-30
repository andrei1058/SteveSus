package com.andrei1058.stevesus.hook.corpse;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.arena.ArenaManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.golde.bukkit.corpsereborn.CorpseAPI.events.CorpseClickEvent;


public class CorpseClickListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onClick(CorpseClickEvent event) {
        Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer(event.getClicker());
        if (arena != null) {
            CorpseManager.CorpseRebornBody deadBody = (CorpseManager.CorpseRebornBody) arena.getDeadBodies().stream().filter(body -> body instanceof CorpseManager.CorpseRebornBody)
                    .filter(body -> ((CorpseManager.CorpseRebornBody) body).getData().equals(event.getCorpse())).findFirst().orElse(null);
            if (deadBody != null) {
                arena.startMeeting(event.getClicker(), deadBody.getOwnerPlayer());
            }
        }
    }
}
