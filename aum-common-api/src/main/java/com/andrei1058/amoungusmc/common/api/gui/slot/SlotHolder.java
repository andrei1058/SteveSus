package com.andrei1058.amoungusmc.common.api.gui.slot;

import com.andrei1058.amoungusmc.common.api.locale.CommonLocale;
import org.bukkit.inventory.ItemStack;

public interface SlotHolder {

    ItemStack getDisplayItem(CommonLocale lang);
}
