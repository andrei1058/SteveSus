package dev.andrei1058.game.arena.listener;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.arena.ArenaManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CreatureSpawnListener implements Listener {

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (e.isCancelled()) return;
        if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM || e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) return;
        GameArena gameArena = ArenaManager.getINSTANCE().getArenaByWorld(e.getEntity().getWorld().getName());
        if (gameArena != null){
            e.setCancelled(true);
        }
    }
}
