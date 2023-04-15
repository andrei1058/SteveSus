package dev.andrei1058.game.server.common;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.LinkedList;

public class ServerQuitListener implements Listener {

    private static final LinkedList<InternalQuitListener> internalQuitListeners = new LinkedList<>();

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        e.setQuitMessage("");

        final Player p = e.getPlayer();
        internalQuitListeners.forEach(listener -> listener.onQuit(p));
    }

    /**
     * For a better performance add quit listeners here.
     * Do not register other listeners.
     */
    public interface InternalQuitListener {
        void onQuit(Player player);
    }

    /**
     * For a better performance add quit listeners here.
     * Do not register other listeners.
     */
    public static void registerInternalQuit(InternalQuitListener listener){
        internalQuitListeners.add(listener);
    }
}
