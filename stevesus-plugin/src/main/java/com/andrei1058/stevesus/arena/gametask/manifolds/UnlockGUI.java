package com.andrei1058.stevesus.arena.gametask.manifolds;

import com.andrei1058.stevesus.api.locale.Locale;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.api.server.GameSound;
import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.api.gui.BaseGUI;
import com.andrei1058.stevesus.common.api.gui.CustomHolder;
import com.andrei1058.stevesus.common.api.gui.slot.StaticSlot;
import com.andrei1058.stevesus.common.gui.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UnlockGUI extends BaseGUI {

    private static final List<String> pattern = Arrays.asList("##01234##", "##56789##");
    private static final ItemStack[] unpressed = new ItemStack[10];
    private static final ItemStack[] pressed = new ItemStack[10];
    private static final String CLICK_TAG = "UNLOCKm";

    private final int[] currentNumber = {0};
    private final UnlockManifoldsTask manifoldsTask;

    public UnlockGUI(Locale lang, String localName, UnlockManifoldsTask manifoldsTask) {
        super(pattern, lang, new UnlockManifoldsHandler(), lang.getMsg(null, Message.GAME_TASK_PATH_ + UnlockManifoldsProvider.getInstance().getIdentifier() + "-" + localName));

        if (unpressed[0] == null) {
            // 10
            unpressed[9] = createItm("7af3fd473a648b847ccda1d2074479bb7672771dc435223468ed9ff7b76cb3", ChatColor.WHITE, 10);
            pressed[9] = createItm("2b095449f9ca3d138a1e658af6436547525769e96e391ab8cc8dc8d5b6d9b9", ChatColor.BLUE, 10);
            // 9
            unpressed[8] = createItm("d6abc61dcaefbd52d9689c0697c24c7ec4bc1afb56b8b3755e6154b24a5d8ba", ChatColor.WHITE, 9);
            pressed[8] = createItm("137fdef8295df3fb6dea21c6c8e451f1f7fd657a61cc6423a3ce42c2fb961b6c", ChatColor.BLUE, 9);
            // 8
            unpressed[7] = createItm("abc0fda9fa1d9847a3b146454ad6737ad1be48bdaa94324426eca0918512d", ChatColor.WHITE, 8);
            pressed[7] = createItm("b6b56cb0b48d9c9edd1984afe1713b98eea96d29be5b2b58da4444a1018e95d", ChatColor.BLUE, 8);
            // 7
            unpressed[6] = createItm("297712ba32496c9e82b20cc7d16e168b035b6f89f3df014324e4d7c365db3fb", ChatColor.WHITE, 7);
            pressed[6] = createItm("e21d562c5a51b64229fc626f25c421f6ac38cf7839e1c6c47168fac3742ccdf8", ChatColor.BLUE, 7);
            // 6
            unpressed[5] = createItm("3ab4da2358b7b0e8980d03bdb64399efb4418763aaf89afb0434535637f0a1", ChatColor.WHITE, 6);
            pressed[5] = createItm("eae7cb37ffa6866317672924301b1b29633e6f23f2525513dbf729bd2d066", ChatColor.BLUE, 6);
            // 5
            unpressed[4] = createItm("d1fe36c4104247c87ebfd358ae6ca7809b61affd6245fa984069275d1cba763", ChatColor.WHITE, 5);
            pressed[4] = createItm("9f2a14dbf9588126c43cd2111eb41f1de6d8c281b6519194364b9965fc456e", ChatColor.BLUE, 5);
            // 4
            unpressed[3] = createItm("f2a3d53898141c58d5acbcfc87469a87d48c5c1fc82fb4e72f7015a3648058", ChatColor.WHITE, 4);
            pressed[3] = createItm("f03d45521c27fdd2f2b1139a1a17d6495e8f47d9f123493d4dd8aa06aff40ce", ChatColor.BLUE, 4);
            // 3
            unpressed[2] = createItm("fd9e4cd5e1b9f3c8d6ca5a1bf45d86edd1d51e535dbf855fe9d2f5d4cffcd2", ChatColor.WHITE, 3);
            pressed[2] = createItm("69e38c81436f3da120672efb162d2f4ea874ab0ce545ae323777f5e573c254a", ChatColor.BLUE, 3);
            // 2
            unpressed[1] = createItm("4698add39cf9e4ea92d42fadefdec3be8a7dafa11fb359de752e9f54aecedc9a", ChatColor.WHITE, 2);
            pressed[1] = createItm("2b3513aa4117a3a329e1f9a43d2a8c51cd722aadd4e8af2feda67b33b64c298", ChatColor.BLUE, 2);
            // 1
            unpressed[0] = createItm("ca516fbae16058f251aef9a68d3078549f48f6d5b683f19cf5a1745217d72cc", ChatColor.WHITE, 1);
            pressed[0] = createItm("bd21b0bafb89721cac494ff2ef52a54a18339858e4dca99a413c42d9f88e0f6", ChatColor.BLUE, 1);
        }

        List<Character> order = Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9');
        List<Character> numbers = Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9');
        Collections.shuffle(numbers);
        int entry = 0;
        for (char c : order) {
            int number = Integer.parseInt(numbers.get(entry) + "");
            int finalNumber = number + 1;
            withReplacement(c, (slot, lang1, filter) -> currentNumber[0] >= finalNumber ? pressed[number] : unpressed[number]);
            entry++;
        }

        withReplacement('#', new StaticSlot(CommonManager.getINSTANCE().getItemSupport().createItem(ItemUtil.getMaterial("STAINED_GLASS_PANE", "GRAY_STAINED_GLASS_PANE"), 1, (byte) 7)));
        this.manifoldsTask = manifoldsTask;
    }


    private static ItemStack createItm(String url, ChatColor color, int number) {
        return CommonManager.getINSTANCE().getItemSupport().addTag(ItemUtil.createSkullWithSkin("https://textures.minecraft.net/texture/" + url, color + "" + ChatColor.BOLD + number), CLICK_TAG, String.valueOf(number));
    }

    public void reset() {
        currentNumber[0] = 0;
        refresh();
    }

    private void performClick(Player player, int number) {
        if (currentNumber[0] != number - 1) {
            GameSound.TASK_PROGRESS_RESET.playToPlayer(player);
            reset();
            return;
        }
        if (number == pressed.length) {
            currentNumber[0] = number - 1;
            refresh();
            GameSound.TASK_PROGRESS_DONE.playToPlayer(player);
            manifoldsTask.markDone(player);
        } else {
            GameSound.TASK_PROGRESS_PLUS.playToPlayer(player);
            currentNumber[0] = number;
            refresh();
        }
    }

    public static class UnlockManifoldsHandler implements CustomHolder {

        private UnlockGUI mainGUI;

        @Override
        public void onClick(Player player, ItemStack itemStack, ClickType clickType) {
            String tag = CommonManager.getINSTANCE().getItemSupport().getTag(itemStack, CLICK_TAG);
            if (tag == null) return;
            int number = Integer.parseInt(tag);
            mainGUI.performClick(player, number);
        }

        @Override
        public boolean isStatic() {
            return false;
        }

        @Override
        public BaseGUI getGui() {
            return mainGUI;
        }

        @Override
        public void setGui(BaseGUI gui) {
            mainGUI = (UnlockGUI) gui;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
