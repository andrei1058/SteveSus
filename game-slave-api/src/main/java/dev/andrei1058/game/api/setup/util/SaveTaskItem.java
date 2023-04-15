package dev.andrei1058.game.api.setup.util;

import dev.andrei1058.game.api.arena.task.TaskProvider;
import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.gui.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.function.Function;

public class SaveTaskItem {

    private Function<Player, Void> saveLogic;
    private TaskProvider taskProvider;

    public SaveTaskItem(TaskProvider taskProvider, Function<Player, Void> saveLogic) {
        this.saveLogic = saveLogic;
        this.taskProvider = taskProvider;
    }

    public void giveItem(Player player) {
        player.getInventory().setItem(0, ItemUtil.createItem("BOOK", (byte) 0, 1, true, Arrays.asList("SaveTaskItem", "save"), ChatColor.RED + "" + ChatColor.BOLD + "Save and close: " + ChatColor.RESET + taskProvider.getDefaultDisplayName(), null));
    }

    /**
     * @return true if event should be cancelled.
     */
    public boolean onItemInteract(ItemStack itemStack, Player player) {
        if (itemStack == null) return false;
        if (itemStack.getType() == Material.AIR) return false;
        String tag = CommonManager.getINSTANCE().getItemSupport().getTag(itemStack, "SaveTaskItem");
        if (tag == null || tag.isEmpty()) return false;
        if (!tag.equals("save")) return false;
        player.setCooldown(itemStack.getType(), 40);
        saveLogic.apply(player);
        return true;
    }
}
