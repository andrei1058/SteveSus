package com.andrei1058.stevesus.commanditem;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface InteractEvent {

    void onInteract(Player player, ItemStack itemStack);
}
