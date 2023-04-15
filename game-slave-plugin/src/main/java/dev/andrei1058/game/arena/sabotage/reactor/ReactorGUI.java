package dev.andrei1058.game.arena.sabotage.reactor;

import dev.andrei1058.game.SteveSus;
import dev.andrei1058.game.common.api.gui.BaseGUI;
import dev.andrei1058.game.common.api.gui.CustomHolder;
import dev.andrei1058.game.common.api.locale.CommonLocale;
import dev.andrei1058.game.common.gui.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class ReactorGUI extends BaseGUI {

    private static final List<String> pattern = Arrays.asList("###", "###", "###");
    private static ItemStack red;
    private static ItemStack white;
    private static ItemStack blue;


    private final boolean first;
    private int taskId = -1;

    /**
     * @param first true if first reactor, false if second.
     */
    public ReactorGUI(CommonLocale lang, boolean first, boolean fixed) {
        super(pattern, lang, new ReactorInventoryHolder(), lang.getMsg(null, fixed ? ReactorSabotageProvider.GUI_NORMAL : ReactorSabotageProvider.GUI_WAITING));
        this.first = first;
        if (white == null) {
            white = ItemUtil.createItem(ItemUtil.getMaterial("STAINED_GLASS_PANE", "WHITE_STAINED_GLASS_PANE"), (byte) 0, 1, false, null, ChatColor.WHITE + "#", null);
        }
        if (red == null) {
            red = ItemUtil.createItem(ItemUtil.getMaterial("STAINED_GLASS_PANE", "RED_STAINED_GLASS_PANE"), (byte) 14, 1, false, null, ChatColor.RED + "#", null);
        }
        if (blue == null) {
            blue = ItemUtil.createItem(ItemUtil.getMaterial("STAINED_GLASS_PANE", "BLUE_STAINED_GLASS_PANE"), (byte) 3, 1, true, null, ChatColor.RED + "#", null);
        }
        if (fixed){
            withReplacement('#', lang1 -> blue);
        } else {
            withReplacement('#', lang1 -> red);
            final byte[] entry = {0};
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(SteveSus.getInstance(), () -> {
                if (entry[0] == 0){
                    entry[0] = 1;
                    getInventory().setItem(0, white);
                    getInventory().setItem(1, white);
                    getInventory().setItem(2, white);
                    getInventory().setItem(6, red);
                    getInventory().setItem(7, red);
                    getInventory().setItem(8, red);
                } else if (entry[0] == 1){
                    entry[0] = 2;
                    getInventory().setItem(0, red);
                    getInventory().setItem(1, red);
                    getInventory().setItem(2, red);
                    getInventory().setItem(3, white);
                    getInventory().setItem(4, white);
                    getInventory().setItem(5, white);
                } else if (entry[0] == 2){
                    entry[0] = 0;
                    getInventory().setItem(3, red);
                    getInventory().setItem(4, red);
                    getInventory().setItem(5, red);
                    getInventory().setItem(6, white);
                    getInventory().setItem(7, white);
                    getInventory().setItem(8, white);
                }
            }, 1L, 5L);
        }
    }

    public int getTaskId() {
        return taskId;
    }

    public boolean isFirst() {
        return first;
    }

    private static class ReactorInventoryHolder implements CustomHolder {

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
