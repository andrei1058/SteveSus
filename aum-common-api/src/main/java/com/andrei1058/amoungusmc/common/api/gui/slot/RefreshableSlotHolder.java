package com.andrei1058.amoungusmc.common.api.gui.slot;

import com.andrei1058.amoungusmc.common.api.locale.CommonLocale;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface RefreshableSlotHolder {

    @Nullable
    ItemStack getSlotItem(int slot, CommonLocale lang, String filter);
}
