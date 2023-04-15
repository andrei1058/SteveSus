package dev.andrei1058.game.api.arena.meeting;

import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.UUID;

/**
 * This is used not to spoil dead users.
 * Users added here will have a fake tab till emergency meeting.
 * (Do not show new ghosts until meetings)
 */
public class TabRevealQueue {

    private LinkedList<UUID> players = new LinkedList<>();


    public void addPlayer(Player player) {
        players.remove(player.getUniqueId());
        players.add(player.getUniqueId());
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
    }

    public boolean isRevealQueued(Player player) {
        return players.contains(player.getUniqueId());
    }

    public void clearQueue() {
        players.clear();
    }
}
