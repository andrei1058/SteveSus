package dev.andrei1058.game.api.event;

import dev.andrei1058.game.api.arena.GameArena;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameInitializedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private GameArena gameArena;
    private String template;
    private String cloneWorldName;

    /**
     * Triggered when an arena is declared as ready.
     *
     * @param gameArena          arena.
     * @param template       original world name.
     * @param cloneWorldName cloned world name.
     */
    public GameInitializedEvent(GameArena gameArena, String template, String cloneWorldName) {
        this.gameArena = gameArena;
        this.template = template;
        this.cloneWorldName = cloneWorldName;
    }

    public GameArena getArena() {
        return gameArena;
    }

    public String getTemplate() {
        return template;
    }

    public String getCloneWorldName() {
        return cloneWorldName;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
