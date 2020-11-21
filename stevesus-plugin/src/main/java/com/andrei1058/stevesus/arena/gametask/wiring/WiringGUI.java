package com.andrei1058.stevesus.arena.gametask.wiring;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.api.gui.BaseGUI;
import com.andrei1058.stevesus.common.api.gui.CustomHolder;
import com.andrei1058.stevesus.common.api.gui.slot.StaticSlot;
import com.andrei1058.stevesus.common.api.locale.CommonLocale;
import com.andrei1058.stevesus.common.gui.ItemUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class WiringGUI extends BaseGUI {

    private static ItemStack[] items = null;


    private static final List<String>[] pattern = new List[]{
            Arrays.asList("#########", "#1#####1#", "#########"),
            Arrays.asList("#########", "#1#####1#", "#2#####2#", "#########"),
            Arrays.asList("#########", "#1#####1#", "#2#####2#", "#3#####3#", "#########"),
            Arrays.asList("#########", "#1#####1#", "#2#####2#", "#3#####3#", "#4#####4#", "#########"),
            Arrays.asList("#1#####1#", "#2#####2#", "#3#####3#", "#4#####4#", "#5#####5#"),
            Arrays.asList("#1#####1#", "#2#####2#", "#3#####3#", "#4#####4#", "#5#####5#", "#6#####6#"),
    };

    public static List<String> getPattern(int wires) {
        if (wires < 1) {
            wires = 1;
        } else if (wires > 6) {
            wires = 6;
        }
        return pattern[wires - 1];
    }

    private FixWiring fixWiring;
    private final List<Character> fixedWires = new ArrayList<>();
    private char lastClickedWire;
    private byte lastSlot = -1;
    private byte amountToFix;

    public WiringGUI(List<String> pattern, CommonLocale lang, FixWiring fixWiring, int wires) {
        super(pattern, lang, new WiringHolder(), lang.getMsg(null, FixWiringProvider.PANEL_NAME));
        this.fixWiring = fixWiring;

        if (wires < 0) {
            wires = 1;
        } else if (wires > 6) {
            wires = 6;
        }

        this.amountToFix = (byte) wires;

        if (items == null) {
            items = new ItemStack[6];
            items[0] = ItemUtil.createItem(ItemUtil.getMaterial("CONCRETE", "BLUE_CONCRETE"), (byte) 11, 1, false, Arrays.asList("wire", "1"), "&9A", null);
            items[1] = ItemUtil.createItem(ItemUtil.getMaterial("CONCRETE", "ORANGE_CONCRETE"), (byte) 1, 1, false, Arrays.asList("wire", "2"), "&6B", null);
            items[2] = ItemUtil.createItem(ItemUtil.getMaterial("CONCRETE", "LIME_CONCRETE"), (byte) 5, 1, false, Arrays.asList("wire", "3"), "&aC", null);
            items[3] = ItemUtil.createItem(ItemUtil.getMaterial("CONCRETE", "RED_CONCRETE"), (byte) 14, 1, false, Arrays.asList("wire", "4"), "&4D", null);
            items[4] = ItemUtil.createItem(ItemUtil.getMaterial("CONCRETE", "PINK_CONCRETE"), (byte) 6, 1, false, Arrays.asList("wire", "5"), "&dE", null);
            items[5] = ItemUtil.createItem(ItemUtil.getMaterial("CONCRETE", "BLACK_CONCRETE"), (byte) 15, 1, false, Arrays.asList("wire", "6"), "&0F", null);
        }

        List<Integer> options = new LinkedList<>();
        for (int i = 0; i < wires; i++){
            options.add(i);
        }
        Collections.shuffle(options);

        for (int i = 1; i < wires + 1; i++) {
            withReplacement(("" + i).charAt(0), new StaticSlot(items[options.get(i - 1)]));
        }
    }

    private void processClick(Player player, ItemStack itemStack, int slot) {
        String tag = CommonManager.getINSTANCE().getItemSupport().getTag(itemStack, "wire");
        if (tag == null) {
            player.playSound(player.getLocation(), Sound.ENTITY_CAT_HISS, 1, 1);
            return;
        }
        char wire = tag.charAt(0);
        // cancel if clicked on a fixed wire
        if (fixedWires.contains(wire)) {
            player.playSound(player.getLocation(), Sound.ENTITY_CAT_HISS, 1, 1);
            return;
        }

        // if has no target
        if (player.getOpenInventory().getCursor() == null || player.getOpenInventory().getCursor().getType() == Material.AIR) {
            player.getOpenInventory().setCursor(itemStack);
            lastSlot = (byte) slot;
            lastClickedWire = wire;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            return;
        }

        // retrieve clicked row
        if (lastSlot != slot && wire == lastClickedWire) {
            // connect with item stacks
            int start = Math.min(lastSlot, slot);
            int end = Math.max(lastSlot, slot);
            for (int i = start + 1; i < end; i++) {
                getInventory().setItem(i, itemStack);
            }
            lastSlot = -1;
            lastClickedWire = ' ';
            //next
            player.getOpenInventory().setCursor(null);
            fixedWires.add(wire);
            if (amountToFix == fixedWires.size()) {
                // mark as fixed
                fixWiring.fixedOneAndGiveNext(player);
                // close with delay
                SteveSus.newChain().delay(20).sync(player::closeInventory).execute();
            } else {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            }
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_CAT_HISS, 1, 1);
        }
    }

    public FixWiring getFixWiring() {
        return fixWiring;
    }

    public static class WiringHolder implements CustomHolder {

        private WiringGUI wiringGUI;

        @Override
        public void onClick(Player player, ItemStack itemStack, ClickType clickType, int slot) {
            ((WiringGUI) getGui()).processClick(player, itemStack, slot);
        }

        @Override
        public void onClick(Player player, ItemStack itemStack, ClickType clickType) {

        }

        @Override
        public boolean isStatic() {
            return false;
        }

        @Override
        public BaseGUI getGui() {
            return wiringGUI;
        }

        @Override
        public void setGui(BaseGUI gui) {
            wiringGUI = (WiringGUI) gui;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
