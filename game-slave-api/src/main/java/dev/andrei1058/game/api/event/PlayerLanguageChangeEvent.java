package dev.andrei1058.game.api.event;

import dev.andrei1058.game.api.locale.Locale;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerLanguageChangeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Locale oldTranslation;
    private final Locale newTranslation;

    public PlayerLanguageChangeEvent(Player player, Locale newTranslation, Locale oldTranslation) {
        this.player = player;
        this.newTranslation = newTranslation;
        this.oldTranslation = oldTranslation;
    }

    public Player getPlayer() {
        return player;
    }

    public Locale getNewTranslation() {
        return newTranslation;
    }

    public Locale getOldTranslation() {
        return oldTranslation;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
