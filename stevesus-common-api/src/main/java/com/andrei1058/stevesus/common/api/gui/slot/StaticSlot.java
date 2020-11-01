package com.andrei1058.stevesus.common.api.gui.slot;

import com.andrei1058.stevesus.common.api.locale.CommonLocale;
import org.bukkit.inventory.ItemStack;

public class StaticSlot implements SlotHolder {

    private final ItemStack item;

    public StaticSlot(ItemStack itemStack){
        this.item = itemStack;
    }

    @Override
    public ItemStack getDisplayItem(CommonLocale lang) {
        return item;
    }
}
