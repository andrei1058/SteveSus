package dev.andrei1058.game.api.event;

import dev.andrei1058.game.api.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerEjectEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Arena arena;
    private final Player player;

    public PlayerEjectEvent(Arena arena, Player player) {
        this.arena = arena;
        this.player = player;
    }

    public Arena getArena() {
        return arena;
    }

    /**
     * Null if it's skipped or a tie.
     */
    @Nullable
    public Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}