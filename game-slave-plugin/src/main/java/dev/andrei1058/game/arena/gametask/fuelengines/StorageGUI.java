package dev.andrei1058.game.arena.gametask.fuelengines;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.locale.Message;
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

import java.util.Arrays;
import java.util.List;

public class StorageGUI extends BaseGUI {

    private static final List<String> pattern = Arrays.asList("#########", "##***####", "##***####", "##***####", "##***##1#", "#########");
    private final ItemStack item1;
    private final ItemStack item2;
    private static ItemStack gray;
    private final ItemStack button;

    private final FuelEnginesTask task;
    private final GameArena gameArena;
    private long nextAllowed = 0;

    public StorageGUI(CommonLocale lang, FuelEnginesTask task, GameArena gameArena) {
        super(pattern, lang, new StorageHolder(), lang.getMsg(null, Message.GAME_TASK_PATH_.toString() + task.getHandler().getIdentifier() + "-" + task.getLocalName() + "gui-name1"));
        item1 = ItemUtil.createItem(ItemUtil.getMaterial("STAINED_GLASS_PANE", "LIME_STAINED_GLASS_PANE"), (byte) 5, 1, false, null, lang.getMsg(null, Message.GAME_TASK_PATH_.toString() + task.getHandler().getIdentifier() + "-" + task.getLocalName() + "-fuel"), null);
        item2 = ItemUtil.createItem(ItemUtil.getMaterial("STAINED_GLASS_PANE", "GREEN_STAINED_GLASS"), (byte) 13, 1, false, null, lang.getMsg(null, Message.GAME_TASK_PATH_.toString() + task.getHandler().getIdentifier() + "-" + task.getLocalName() + "-fuel"), null);
        button = ItemUtil.createItem("COAL", (byte) 0, 1, true, null, lang.getMsg(null, Message.GAME_TASK_PATH_.toString() + task.getHandler().getIdentifier() + "-" + task.getLocalName() + "-button"), null);
        if (gray == null) {
            gray = ItemUtil.createItem(ItemUtil.getMaterial("STAINED_GLASS_PANE", "GRAY_STAINED_GLASS_PANE"), (byte) 7, 1, false, null, ChatColor.GRAY + "#", null);
        }
        this.task = task;
        this.gameArena = gameArena;
        withReplacement('#', lang1 -> gray);
        withReplacement('1', lang1 -> button);
    }

    byte stage = 0;

    public static class StorageHolder implements CustomHolder {

        private StorageGUI storageGUI;

        @Override
        public void onClick(Player player, ItemStack itemStack, ClickType clickType) {

        }

        @Override
        public void onClick(Player whoClicked, ItemStack currentItem, ClickType click, int slot) {
            if (slot == 43) {
                if (storageGUI.nextAllowed > System.currentTimeMillis()) {
                    return;
                }
                storageGUI.nextAllowed = System.currentTimeMillis() + 400;
                switch (storageGUI.stage) {
                    case 0:
                        getInventory().setItem(38, storageGUI.item1);
                        getInventory().setItem(39, storageGUI.item1);
                        getInventory().setItem(40, storageGUI.item1);
                        break;
                    case 1:
                        getInventory().setItem(38, storageGUI.item2);
                        getInventory().setItem(39, storageGUI.item2);
                        getInventory().setItem(40, storageGUI.item2);
                        break;
                    case 2:
                        getInventory().setItem(29, storageGUI.item1);
                        getInventory().setItem(30, storageGUI.item1);
                        getInventory().setItem(31, storageGUI.item1);
                        break;
                    case 3:
                        getInventory().setItem(29, storageGUI.item2);
                        getInventory().setItem(30, storageGUI.item2);
                        getInventory().setItem(31, storageGUI.item2);
                        break;
                    case 4:
                        getInventory().setItem(20, storageGUI.item1);
                        getInventory().setItem(21, storageGUI.item1);
                        getInventory().setItem(22, storageGUI.item1);
                        break;
                    case 5:
                        getInventory().setItem(20, storageGUI.item2);
                        getInventory().setItem(21, storageGUI.item2);
                        getInventory().setItem(22, storageGUI.item2);
                        break;
                    case 6:
                        getInventory().setItem(11, storageGUI.item1);
                        getInventory().setItem(12, storageGUI.item1);
                        getInventory().setItem(13, storageGUI.item1);
                        break;
                    case 7:
                        getInventory().setItem(11, storageGUI.item2);
                        getInventory().setItem(12, storageGUI.item2);
                        getInventory().setItem(13, storageGUI.item2);
                        break;
                }
                storageGUI.stage++;
                if (storageGUI.stage == 8) {
                    storageGUI.task.addProgress(whoClicked, storageGUI.gameArena);
                }
                GameSound.TASK_PROGRESS_PLUS.playToPlayer(whoClicked);
            }
        }

        @Override
        public boolean isStatic() {
            return false;
        }

        @Override
        public BaseGUI getGui() {
            return storageGUI;
        }

        @Override
        public void setGui(BaseGUI gui) {
            this.storageGUI = (StorageGUI) gui;
        }

        @Override
        public Inventory getInventory() {
            return storageGUI.getInventory();
        }
    }
}
