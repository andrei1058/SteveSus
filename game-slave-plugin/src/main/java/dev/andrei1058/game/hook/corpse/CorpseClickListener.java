package dev.andrei1058.game.hook.corpse;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.arena.ArenaManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.golde.bukkit.corpsereborn.CorpseAPI.events.CorpseClickEvent;


public class CorpseClickListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onClick(CorpseClickEvent event) {
        GameArena gameArena = ArenaManager.getINSTANCE().getArenaByPlayer(event.getClicker());
        if (gameArena != null) {
            CorpseManager.CorpseRebornBody deadBody = (CorpseManager.CorpseRebornBody) gameArena.getDeadBodies().stream().filter(body -> body instanceof CorpseManager.CorpseRebornBody)
                    .filter(body -> ((CorpseManager.CorpseRebornBody) body).getData().equals(event.getCorpse())).findFirst().orElse(null);
            if (deadBody != null) {
                gameArena.startMeeting(event.getClicker(), deadBody.getOwnerPlayer());
            }
        }
    }
}
