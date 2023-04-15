package dev.andrei1058.game.common.api.gui.slot;

import dev.andrei1058.game.common.api.locale.CommonLocale;
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
