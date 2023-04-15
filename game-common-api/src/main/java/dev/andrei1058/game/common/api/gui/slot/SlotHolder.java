package dev.andrei1058.game.common.api.gui.slot;

import dev.andrei1058.game.common.api.locale.CommonLocale;
import org.bukkit.inventory.ItemStack;

public interface SlotHolder {

    ItemStack getDisplayItem(CommonLocale lang);
}
