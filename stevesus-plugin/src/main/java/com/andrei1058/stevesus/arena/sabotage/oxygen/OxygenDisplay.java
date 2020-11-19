package com.andrei1058.stevesus.arena.sabotage.oxygen;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.server.GameSound;
import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.api.gui.BaseGUI;
import com.andrei1058.stevesus.common.api.gui.CustomHolder;
import com.andrei1058.stevesus.common.api.gui.slot.StaticSlot;
import com.andrei1058.stevesus.common.api.locale.CommonLocale;
import com.andrei1058.stevesus.common.gui.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class OxygenDisplay extends BaseGUI {

    private static final List<String> pattern = Arrays.asList("##12345##", "#########", "###abc###", "###def###", "###ghi###", "#########");
    private static final char[] numberSlots = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i'};
    private static final ItemStack[] unpressed = new ItemStack[9];
    private static final ItemStack[] pressed = new ItemStack[9];
    private static final String CLICK_TAG = "oxygenM";

    // assigned monitor
    private final OxygenSabotage.OxygenMonitor assignedMonitor;
    // list of press order
    private final short[][] PIN_PRESSED = new short[5][1];
    // list of pin order
    private final short[][] PIN_ORDER = new short[5][9];

    public OxygenDisplay(CommonLocale lang, OxygenSabotage.OxygenMonitor oxygenSabotage) {
        super(pattern, lang, new OxygenHolder(), "Type PIN:");
        this.assignedMonitor = oxygenSabotage;

        if (unpressed[0] == null) {
            // 9
            unpressed[8] = createItm("d6abc61dcaefbd52d9689c0697c24c7ec4bc1afb56b8b3755e6154b24a5d8ba", ChatColor.GOLD, 9);
            pressed[8] = createItm("dae461a4434196d37296ad5adf6d9d5744a0415dc61c475a6dfa6285814052", ChatColor.GREEN, 9);
            // 8
            unpressed[7] = createItm("abc0fda9fa1d9847a3b146454ad6737ad1be48bdaa94324426eca0918512d", ChatColor.GOLD, 8);
            pressed[7] = createItm("42647ae47b6b51f5a45eb3dcafa9b88f288ede9cebdb52a159e3110e6b8118e", ChatColor.GREEN, 8);
            // 7
            unpressed[6] = createItm("297712ba32496c9e82b20cc7d16e168b035b6f89f3df014324e4d7c365db3fb", ChatColor.GOLD, 7);
            pressed[6] = createItm("d7de70b88368ce23a1ac6d1c4ad9131480f2ee205fd4a85ab2417af7f6bd90", ChatColor.GREEN, 7);
            // 6
            unpressed[5] = createItm("3ab4da2358b7b0e8980d03bdb64399efb4418763aaf89afb0434535637f0a1", ChatColor.GOLD, 6);
            pressed[5] = createItm("24ddb03aa8c584168c63ece337aefb281774377db72337297de258b4cca6fba4", ChatColor.GREEN, 6);
            // 5
            unpressed[4] = createItm("d1fe36c4104247c87ebfd358ae6ca7809b61affd6245fa984069275d1cba763", ChatColor.GOLD, 5);
            pressed[4] = createItm("a2c142af59f29eb35ab29c6a45e33635dcfc2a956dbd4d2e5572b0d38891b354", ChatColor.GREEN, 5);
            // 4
            unpressed[3] = createItm("f2a3d53898141c58d5acbcfc87469a87d48c5c1fc82fb4e72f7015a3648058", ChatColor.GOLD, 4);
            pressed[3] = createItm("f920ecce1c8cde5dbca5938c5384f714e55bee4cca866b7283b95d69eed3d2c", ChatColor.GREEN, 4);
            // 3
            unpressed[2] = createItm("fd9e4cd5e1b9f3c8d6ca5a1bf45d86edd1d51e535dbf855fe9d2f5d4cffcd2", ChatColor.GOLD, 3);
            pressed[2] = createItm("c4226f2eb64abc86b38b61d1497764cba03d178afc33b7b8023cf48b49311", ChatColor.GREEN, 3);
            // 2
            unpressed[1] = createItm("4698add39cf9e4ea92d42fadefdec3be8a7dafa11fb359de752e9f54aecedc9a", ChatColor.GOLD, 2);
            pressed[1] = createItm("5496c162d7c9e1bc8cf363f1bfa6f4b2ee5dec6226c228f52eb65d96a4635c", ChatColor.GREEN, 2);
            // 1
            unpressed[0] = createItm("ca516fbae16058f251aef9a68d3078549f48f6d5b683f19cf5a1745217d72cc", ChatColor.GOLD, 1);
            pressed[0] = createItm("88991697469653c9af8352fdf18d0cc9c67763cfe66175c1556aed33246c7", ChatColor.GREEN, 1);
        }

        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            int assignedNumber = random.nextInt(9);
            withReplacement(numberSlots[assignedNumber], new StaticSlot(unpressed[assignedNumber]));
            PIN_ORDER[i][0] = (short) (assignedNumber + 1);
            int finalI = i;
            withReplacement(("" + (i + 1)).charAt(0), (slot, lang1, filter) -> PIN_PRESSED[finalI][0] == 1 ? pressed[assignedNumber] : unpressed[assignedNumber]);
        }

        // second add to inventory operation
        for (byte i = 0; i < 9; i++) {
            boolean alreadyAdded = false;
            for (byte a = 0; a < 5; a++) {
                if (PIN_ORDER[a][0] == i + 1) {
                    alreadyAdded = true;
                    break;
                }
            }
            if (!alreadyAdded) {
                withReplacement(numberSlots[i], new StaticSlot(unpressed[i]));
            }
        }

        withReplacement('#', new StaticSlot(CommonManager.getINSTANCE().getItemSupport().createItem(ItemUtil.getMaterial("STAINED_GLASS_PANE", "GRAY_STAINED_GLASS_PANE"), 1, (byte) 7)));
    }

    private static ItemStack createItm(String url, ChatColor color, int number) {
        return CommonManager.getINSTANCE().getItemSupport().addTag(ItemUtil.createSkullWithSkin("https://textures.minecraft.net/texture/" + url, color + "" + ChatColor.BOLD + number), CLICK_TAG, String.valueOf(number));
    }

    private void reset() {
        for (byte i = 0; i < 5; i++) {
            PIN_PRESSED[i][0] = 0;
        }
        refresh();
    }

    private void performClick(Player player, int number) {
        int entry = nextEntry();
        if (PIN_ORDER[entry][0] == number) {
            PIN_PRESSED[entry][0] = 1;
            GameSound.SABOTAGE_FIX_PROGRESS.playToPlayer(player);
            refresh();
            if (entry == 4) {
                // done
                GameSound.SABOTAGE_FIX_SUCCESS.playToPlayer(player);
                getAssignedMonitor().onErrorFix(false);
                SteveSus.newChain().delay(1).sync(() -> {
                    for (Player inWorld : player.getWorld().getPlayers()) {
                        if (inWorld.getOpenInventory() != null && inWorld.getOpenInventory().getTopInventory() != null) {
                            if (inWorld.getOpenInventory().getTopInventory().getHolder() instanceof OxygenHolder) {
                                if (((OxygenDisplay) ((OxygenHolder) inWorld.getOpenInventory().getTopInventory().getHolder()).getGui()).getAssignedMonitor().equals(getAssignedMonitor())) {
                                    inWorld.closeInventory();
                                }
                            }
                        }
                    }
                }).execute();
            }
        } else {
            reset();
            GameSound.SABOTAGE_FIX_PROGRESS_RESET.playToPlayer(player);
        }
    }

    private short nextEntry() {
        for (byte i = 0; i < 6; i++) {
            if (PIN_PRESSED[i][0] == 0) {
                return i;
            }
        }
        return 5;
    }

    public OxygenSabotage.OxygenMonitor getAssignedMonitor() {
        return assignedMonitor;
    }

    public static class OxygenHolder implements CustomHolder {

        private OxygenDisplay mainGUI;

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
            mainGUI = (OxygenDisplay) gui;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
