package com.andrei1058.stevesus.commanditem;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public interface InteractEvent {

    void onInteract(Player player, Cancellable itemStack);
}
