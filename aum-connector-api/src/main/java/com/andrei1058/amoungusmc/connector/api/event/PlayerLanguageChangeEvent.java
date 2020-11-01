package com.andrei1058.amoungusmc.connector.api.event;

import com.andrei1058.amoungusmc.common.api.locale.CommonLocale;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerLanguageChangeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final CommonLocale oldTranslation;
    private final CommonLocale newTranslation;

    public PlayerLanguageChangeEvent(Player player, CommonLocale newTranslation, CommonLocale oldTranslation) {
        this.player = player;
        this.newTranslation = newTranslation;
        this.oldTranslation = oldTranslation;
    }

    public Player getPlayer() {
        return player;
    }

    public CommonLocale getNewTranslation() {
        return newTranslation;
    }

    public CommonLocale getOldTranslation() {
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
