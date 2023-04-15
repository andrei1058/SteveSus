package dev.andrei1058.game.arena.sabotage.fixlights;

import dev.andrei1058.game.api.server.GameSound;
import dev.andrei1058.game.common.api.gui.BaseGUI;
import dev.andrei1058.game.common.api.gui.CustomHolder;
import dev.andrei1058.game.common.api.locale.CommonLocale;
import dev.andrei1058.game.common.gui.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class FixLightsGUI extends BaseGUI {

    private static final List<String> pattern = Arrays.asList("#########", "#########", "#########");
    private static ItemStack white;
    private static ItemStack red;
    private static ItemStack gray;

    private final LightsSabotage task;
    private byte redThings = 5;

    public FixLightsGUI(CommonLocale lang, LightsSabotage task, Player player) {
        super(pattern, lang, new FixLightsHolder(), lang.getMsg(null, LightsSabotageProvider.NAME_PATH));

        if (red == null) {
            red = ItemUtil.createItem(ItemUtil.getMaterial("CONCRETE", "YELLOW_CONCRETE"), (byte) 4, 1, false, null, ChatColor.WHITE + "#", null);
        }
        if (white == null) {
            white = ItemUtil.createItem(ItemUtil.getMaterial("CONCRETE", "LIME_CONCRETE"), (byte) 5, 1, false, null, ChatColor.RED + "#", null);
        }
        if (gray == null) {
            gray = ItemUtil.createItem(ItemUtil.getMaterial("STAINED_GLASS_PANE", "GRAY_STAINED_GLASS_PANE"), (byte) 7, 1, false, null, ChatColor.GRAY + "#", null);
        }

        withReplacement('#', lang1 -> gray);
        getInventory().setItem(9, red);
        getInventory().setItem(11, red);
        getInventory().setItem(13, red);
        getInventory().setItem(15, red);
        getInventory().setItem(17, red);

        this.task = task;
    }

    private static class FixLightsHolder implements CustomHolder {

        private FixLightsGUI gui;

        @Override
        public void onClick(Player player, ItemStack itemStack, ClickType clickType) {
        }

        @Override
        public void onClick(Player whoClicked, ItemStack currentItem, ClickType click, int slot) {
            if (currentItem.equals(red)){
                gui.redThings--;
                getInventory().setItem(slot, white);
                GameSound.TASK_PROGRESS_PLUS.playToPlayer(whoClicked);
            } else if (currentItem.equals(white)){
                gui.redThings++;
                getInventory().setItem(slot, red);
                GameSound.TASK_PROGRESS_RESET.playToPlayer(whoClicked);
            }
            if (gui.redThings == 0){
                //done
                gui.task.tryDeactivate();
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
            this.gui = (FixLightsGUI) gui;
        }

        @Override
        public Inventory getInventory() {
            return gui.getInventory();
        }
    }
}
