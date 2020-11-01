package com.andrei1058.stevesus.common.selector;

import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.api.arena.GameState;
import com.andrei1058.stevesus.common.api.locale.CommonLocale;
import com.andrei1058.stevesus.common.api.gui.slot.RefreshableSlotHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArenaSlot implements RefreshableSlotHolder {

    private final List<String> templatesFilter = new ArrayList<>();
    private final List<GameState> statusFilter = new ArrayList<>();

    public ArenaSlot(@Nullable String templatesFilter, @Nullable String statusFilter) {
        if (templatesFilter != null && !templatesFilter.contains("none")) {
            this.templatesFilter.addAll(Arrays.asList(templatesFilter.trim().split(",")));
        }
        if (statusFilter != null && !statusFilter.contains("none")) {
            for (String parse : statusFilter.trim().split(",")) {
                GameState filtered = GameState.getByNickName(parse);
                if (filtered == null) {
                    CommonManager.getINSTANCE().getPlugin().getLogger().warning("Invalid game-state filter: " + statusFilter);
                } else {
                    this.statusFilter.add(filtered);
                }

            }
        }
    }

    public List<GameState> getStatusFilter() {
        return statusFilter;
    }

    public List<String> getTemplatesFilter() {
        return templatesFilter;
    }

    @Override
    public ItemStack getSlotItem(int slot, CommonLocale lang, @Nullable String template) {
        throw new IllegalStateException("Do not call getSlotItem on ArenaSlot. They're meant for ArenaGUI only.");
    }
}
