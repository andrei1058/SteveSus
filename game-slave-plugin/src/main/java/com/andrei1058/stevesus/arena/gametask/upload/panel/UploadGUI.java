package com.andrei1058.stevesus.arena.gametask.upload.panel;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.arena.gametask.upload.UploadTask;
import com.andrei1058.stevesus.arena.gametask.upload.UploadTaskProvider;
import com.andrei1058.stevesus.common.api.gui.BaseGUI;
import com.andrei1058.stevesus.common.api.gui.CustomHolder;
import com.andrei1058.stevesus.common.api.gui.slot.RefreshableSlotHolder;
import com.andrei1058.stevesus.common.api.locale.CommonLocale;
import com.andrei1058.stevesus.common.gui.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;

public class UploadGUI extends BaseGUI {

    private static final List<String> pattern = Collections.singletonList("#########");

    private int taskId;
    private final double[] currentTime = new double[1];
    private final double initialTime;
    private final double[] currentPercentile = new double[]{0};
    private final int[] nextGreenSlot = new int[]{0};

    public UploadGUI(CommonLocale lang, WallPanel.PanelType type, UploadTask parentTask, double taskTime, Player player, Arena arena) {
        super(pattern, lang, new UploadHolder(), lang.getMsg(null, type == WallPanel.PanelType.DOWNLOAD ? UploadTaskProvider.DOWNLOAD_PANEL_NAME : UploadTaskProvider.UPLOAD_PANEL_NAME));
        this.initialTime = taskTime;
        this.currentTime[0] = 0;

        ItemStack pending = ItemUtil.createItem(ItemUtil.getMaterial("STAINED_GLASS_PANE", "GRAY_STAINED_GLASS_PANE"), (byte) 7, 1, false, null);
        ItemStack done = ItemUtil.createItem(ItemUtil.getMaterial("STAINED_GLASS_PANE", "GREEN_STAINED_GLASS_PANE"), (byte) 5, 1, false, null);

        DecimalFormat format = new DecimalFormat("##.00");

        withReplacement('#', (slot, lang1, filter) -> {
            ItemStack item = (slot <= nextGreenSlot[0] ? done : pending).clone();
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "" + format.format(currentPercentile[0]) + "%");
            item.setItemMeta(meta);
            return item;
        });

        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(SteveSus.getInstance(), () -> {
            currentTime[0] += 0.25;
            currentPercentile[0] = (int) ((currentTime[0] / initialTime) * 100);
            nextGreenSlot[0] = (int) ((currentPercentile[0] / 100) * 9);
            refresh();
            if (currentTime[0] >= initialTime) {
                //todo done
                parentTask.markPanelFinished(player, arena);
                Bukkit.getScheduler().cancelTask(taskId);
                SteveSus.newChain().delay(10).sync(player::closeInventory).execute();
            }
        }, 0L, 5L);
    }

    public int getTaskId() {
        return taskId;
    }

    public static class UploadHolder implements CustomHolder {

        @Override
        public void onClick(Player player, ItemStack itemStack, ClickType clickType) {

        }

        @Override
        public boolean isStatic() {
            return false;
        }

        @Override
        public BaseGUI getGui() {
            return null;
        }

        @Override
        public void setGui(BaseGUI gui) {

        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
