package dev.andrei1058.game.common.api.gui.slot;

import dev.andrei1058.game.common.api.locale.CommonLocale;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface RefreshableSlotHolder {

    @Nullable
    ItemStack getSlotItem(int slot, CommonLocale lang, String filter);
}
