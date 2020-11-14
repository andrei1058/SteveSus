package com.andrei1058.stevesus.common.command;

import com.andrei1058.spigot.commandlib.fast.FastRootCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubCommand;
import org.bukkit.entity.Player;

@SuppressWarnings("UnstableApiUsage")
public class InvCloseCmd {

    private InvCloseCmd() {
    }

    public static void register(FastRootCommand root) {
        root
                .withSubNode(new FastSubCommand("closeInv")
                .withPermAdditions(s -> (s instanceof Player) && ((Player)s).getOpenInventory() != null)
                .withExecutor((s, args) -> ((Player)s).closeInventory()));

    }
}
