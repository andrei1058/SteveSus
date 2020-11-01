package com.andrei1058.stevesus.common.api.gui.slot;

import com.andrei1058.stevesus.common.api.locale.CommonLocale;
import org.bukkit.inventory.ItemStack;

public interface SlotHolder {

    ItemStack getDisplayItem(CommonLocale lang);
}
