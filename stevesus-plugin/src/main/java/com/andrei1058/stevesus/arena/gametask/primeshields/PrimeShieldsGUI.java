package com.andrei1058.stevesus.arena.gametask.primeshields;

import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.api.server.GameSound;
import com.andrei1058.stevesus.common.api.gui.BaseGUI;
import com.andrei1058.stevesus.common.api.gui.CustomHolder;
import com.andrei1058.stevesus.common.api.locale.CommonLocale;
import com.andrei1058.stevesus.common.gui.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PrimeShieldsGUI extends BaseGUI {

    private static final List<String> pattern = Arrays.asList("###", "###", "###");
    private static ItemStack white;
    private static ItemStack red;

    private final PrimeShieldsTask task;
    private byte redThings = 4;

    public PrimeShieldsGUI(CommonLocale lang, PrimeShieldsTask task, Player player) {
        super(pattern, lang, new PrimeShieldsHolder(), lang.getMsg(null, Message.GAME_TASK_NAME_PATH_.toString() + task.getHandler().getIdentifier()));

        if (white == null) {
            white = ItemUtil.createItem(ItemUtil.getMaterial("STAINED_GLASS_PANE", "WHITE_STAINED_GLASS_PANE"), (byte) 0, 1, false, null, ChatColor.WHITE + "#", null);
        }
        if (red == null) {
            red = ItemUtil.createItem(ItemUtil.getMaterial("STAINED_GLASS_PANE", "RED_STAINED_GLASS_PANE"), (byte) 14, 1, false, null, ChatColor.RED + "#", null);
        }

        for (int i = 0; i < getInventory().getSize(); i++) {
            getInventory().setItem(i, white);
        }

        // distinct
        Random randNum = new Random();
        Set<Integer> set = new LinkedHashSet<>();
        while (set.size() < redThings) {
            set.add(randNum.nextInt(getInventory().getSize()));
        }

        for (Integer i : set) {
            getInventory().setItem(i, red);
        }

        this.task = task;
    }

    private static class PrimeShieldsHolder implements CustomHolder {

        private PrimeShieldsGUI gui;

        @Override
        public void onClick(Player player, ItemStack itemStack, ClickType clickType) {
        }

        @Override
        public void onClick(Player whoClicked, ItemStack currentItem, ClickType click, int slot) {
            if (currentItem.equals(red)){
                gui.redThings--;
                getInventory().setItem(slot, white);
                GameSound.TASK_PROGRESS_PLUS.playToPlayer(whoClicked);
            } else {
                gui.redThings++;
                getInventory().setItem(slot, red);
                GameSound.TASK_PROGRESS_RESET.playToPlayer(whoClicked);
            }
            if (gui.redThings == 0){
                //done
                gui.task.complete(whoClicked);
            }
        }

        @Override
        public boolean isStatic() {
            return true;
        }

        @Override
        public BaseGUI getGui() {
            return gui;
        }

        @Override
        public void setGui(BaseGUI gui) {
            this.gui = (PrimeShieldsGUI) gui;
        }

        @Override
        public Inventory getInventory() {
            return gui.getInventory();
        }
    }
}
