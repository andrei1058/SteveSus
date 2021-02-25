package com.andrei1058.stevesus.arena.gametask.startreactor;

import co.aikar.taskchain.TaskChain;
import com.andrei1058.stevesus.SteveSus;
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

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class StartPatternGUI extends BaseGUI {

    private static final List<String> pattern = Arrays.asList("****0++++", "#########", "#aaa#aaa#", "#aaa#aaa#", "#aaa#aaa#", "#########");
    private static final int[] candidates = {19, 20, 21, 28, 29, 30, 37, 38, 39};
    private static ItemStack gray;
    private static ItemStack black;
    private static ItemStack intersect;
    private static ItemStack patternItem;
    private static ItemStack inputItem;
    private static ItemStack panelItem;

    // local
    private final int[] clickPattern = new int[5];
    private byte nextBound = 0;
    private byte nextClick = 0;
    private boolean isPlayingPattern = false;
    private final StartReactorTask task;

    public StartPatternGUI(CommonLocale lang, StartReactorTask task, Player player) {
        super(pattern, lang, new StartPatternHolder(), lang.getMsg(null, Message.GAME_TASK_PATH_.toString() + task.getHandler().getIdentifier() + "-" + task.getLocalName() + "-gui-name"));
        this.task = task;

        if (gray == null) {
            gray = ItemUtil.createItem(ItemUtil.getMaterial("STAINED_GLASS_PANE", "GRAY_STAINED_GLASS_PANE"), (byte) 7, 1, false, null, ChatColor.GRAY + "#", null);
        }
        if (black == null) {
            black = ItemUtil.createItem(ItemUtil.getMaterial("STAINED_GLASS_PANE", "BLACK_STAINED_GLASS_PANE"), (byte) 15, 1, false, null, ChatColor.GRAY + "#", null);
        }
        if (intersect == null) {
            intersect = ItemUtil.createItem(ItemUtil.getMaterial("STAINED_GLASS_PANE", "YELLOW_STAINED_GLASS_PANE"), (byte) 4, 1, true, null, ChatColor.GRAY + "#", null);
        }
        if (patternItem == null) {
            patternItem = ItemUtil.createItem(ItemUtil.getMaterial("STAINED_GLASS_PANE", "BLUE_STAINED_GLASS_PANE"), (byte) 11, 1, false, null, ChatColor.GRAY + "#", null);
        }
        if (inputItem == null) {
            inputItem = ItemUtil.createItem(ItemUtil.getMaterial("STAINED_GLASS_PANE", "LIME_STAINED_GLASS_PANE"), (byte) 5, 1, false, null, ChatColor.GRAY + "#", null);
        }
        if (panelItem == null) {
            panelItem = ItemUtil.createItem(ItemUtil.getMaterial("STAINED_GLASS_PANE", "WHITE_STAINED_GLASS_PANE"), (byte) 0, 1, false, null, ChatColor.GRAY + "#", null);
        }

        withReplacement('#', lang1 -> gray);

        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i, black);
        }

        withReplacement('a', lang1 -> panelItem);

        // create pattern
        Random random = new Random();
        for (int i = 0; i < clickPattern.length; i++) {
            int pos = random.nextInt(candidates.length);
            int picked = candidates[pos];
            clickPattern[i] = picked;
        }

        getInventory().setItem(nextBound, patternItem);
        playPattern(nextBound + 1, player);

    }

    private void reset(Player player) {
        GameSound.TASK_PROGRESS_RESET.playToPlayer(player);
        nextBound = 0;
        nextClick = 0;
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i, black);
        }
        playPattern(nextBound + 1, player);
    }

    private void playPattern(int limit, Player player) {
        TaskChain<?> task = SteveSus.newChain();
        isPlayingPattern = true;
        for (int i = 0; i < limit; i++) {
            int slot = clickPattern[i];
            task.delay(5).sync(() -> {
                if (!this.task.getOpenGUI().contains(player.getUniqueId())) {
                    task.abortChain();
                }
                getInventory().setItem(slot, patternItem);
                GameSound.TASK_PROGRESS_PLUS.playToPlayer(player);
            });
            task.delay(10).sync(() -> getInventory().setItem(slot, panelItem));
        }
        isPlayingPattern = false;
        task.execute();
    }

    private static class StartPatternHolder implements CustomHolder {

        private StartPatternGUI startPatternGUI;

        @Override
        public void onClick(Player player, ItemStack itemStack, ClickType clickType) {
        }

        @Override
        public void onClick(Player whoClicked, ItemStack currentItem, ClickType click, int slot) {
            if (startPatternGUI.isPlayingPattern) return;
            // means is done and the inv is going to be closed in a few ticks
            if (startPatternGUI.nextClick == startPatternGUI.clickPattern.length) return;

            if (startPatternGUI.clickPattern[startPatternGUI.nextClick] + 4 == slot) {
                getInventory().setItem(8 - startPatternGUI.nextClick, inputItem);
                startPatternGUI.nextClick++;
                GameSound.TASK_PROGRESS_PLUS.playToPlayer(whoClicked);
                getInventory().setItem(slot, StartPatternGUI.inputItem);
                SteveSus.newChain().delay(5).sync(() -> getInventory().setItem(slot, StartPatternGUI.panelItem)).execute();
                if (startPatternGUI.nextClick - 1 == startPatternGUI.nextBound) {
                    startPatternGUI.nextBound++;
                    if (startPatternGUI.nextBound == startPatternGUI.clickPattern.length) {
                        // done
                        getInventory().setItem(4, intersect);
                        startPatternGUI.task.complete(whoClicked);
                    } else {
                        startPatternGUI.nextClick = 0;
                        SteveSus.newChain().delay(5).sync(() -> {
                            for (int i = 8; i > 4; i--) {
                                getInventory().setItem(i, black);
                            }
                        });
                        getInventory().setItem(startPatternGUI.nextBound, patternItem);
                        startPatternGUI.playPattern(startPatternGUI.nextBound + 1, whoClicked);
                    }
                }
            } else {
                startPatternGUI.reset(whoClicked);
            }
        }

        @Override
        public boolean isStatic() {
            return true;
        }

        @Override
        public BaseGUI getGui() {
            return startPatternGUI;
        }

        @Override
        public void setGui(BaseGUI gui) {
            this.startPatternGUI = (StartPatternGUI) gui;
        }

        @Override
        public Inventory getInventory() {
            return startPatternGUI.getInventory();
        }
    }
}
