package com.andrei1058.stevesus.arena.gametask.emptygarbage;

import com.andrei1058.stevesus.api.server.GameSound;
import com.andrei1058.stevesus.common.api.gui.BaseGUI;
import com.andrei1058.stevesus.common.api.gui.CustomHolder;
import com.andrei1058.stevesus.common.api.locale.CommonLocale;
import com.andrei1058.stevesus.common.gui.ItemUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GarbageGUI extends BaseGUI {

    private static final List<String> pattern = Arrays.asList("#########", "#########", "#########", "#########", "#########", "#########");
    public static final Material[] CANDIDATES = new Material[]{Material.DEAD_BUSH, Material.SAND, Material.GOLD_INGOT, Material.CLAY_BALL, Material.STRING, Material.BONE, Material.AIR, Material.AIR, Material.AIR};
    private static int clickDelay = 500;
    private int leverSlot = 8;
    private long nextClick = 0;
    private byte remainingLines = 6;
    private EmptyGarbageTask emptyGarbageTask;

    public GarbageGUI(CommonLocale lang, EmptyGarbageTask emptyGarbageTask) {
        super(pattern, lang, new GarbageInventoryHolder(), lang.getMsg(null, EmptyGarbageTaskProvider.GARBAGE_GUI_NAME));
        this.emptyGarbageTask = emptyGarbageTask;

        ItemStack lever = ItemUtil.createItem("LEVER", (byte) 0, 1, true, null, lang.getMsg(null, EmptyGarbageTaskProvider.LEVER_NAME), lang.getMsgList(null, EmptyGarbageTaskProvider.LEVER_LORE));
        getInventory().setItem(8, lever);
        String garbageItemName = lang.getMsg(null, EmptyGarbageTaskProvider.GARBAGE_ITEM_NAME);
        List<String> garbageItemLore = lang.getMsgList(null, EmptyGarbageTaskProvider.GARBAGE_ITEM_LORE);

        List<ItemStack> garbage = new ArrayList<>();
        for (Material candidate : CANDIDATES) {
            ItemStack item = ItemUtil.createItem(candidate, (byte) 0, 1, false, null, garbageItemName, garbageItemLore);
            garbage.add(item);
        }

        for (int i = 1; i < 6; i++) {
            Collections.shuffle(garbage);
            for (int x = 0; x < 9; x++) {
                getInventory().setItem(((i * 9) + x),
                        x < garbage.size() ? garbage.get(x) : new ItemStack(Material.AIR));
            }
        }
    }

    private void performClick(Player whoClicked, int slot) {
        if (slot != leverSlot) return;
        long current = System.currentTimeMillis();
        if (nextClick > current) return;
        remainingLines--;
        // shift items
        for (int itemSlot = getInventory().getSize() - 1; itemSlot > 6 - remainingLines; itemSlot--) {
            if (itemSlot < 9) continue;
            int copySlot = itemSlot - 9;
            ItemStack temp = copySlot / 9 < 6 - remainingLines ? null : getInventory().getItem(copySlot);
            getInventory().setItem(itemSlot, temp);
        }
        //
        if (remainingLines == 1) {
            // done
            emptyGarbageTask.fixedOneAndGiveNext(whoClicked);
        }
        GameSound.TASK_PROGRESS_PLUS.playToPlayer(whoClicked);
        nextClick = current + clickDelay;

    }

    private static class GarbageInventoryHolder implements CustomHolder {

        private GarbageGUI garbageGUI;

        @Override
        public void onClick(Player player, ItemStack itemStack, ClickType clickType) {

        }

        @Override
        public void onClick(Player whoClicked, ItemStack currentItem, ClickType click, int slot) {
            garbageGUI.performClick(whoClicked, slot);
        }

        @Override
        public boolean isStatic() {
            return true;
        }

        @Override
        public BaseGUI getGui() {
            return garbageGUI;
        }

        @Override
        public void setGui(BaseGUI gui) {
            this.garbageGUI = (GarbageGUI) gui;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
