package com.andrei1058.amongusmc.server.multiarena.command;

import com.andrei1058.amongusmc.config.MainConfig;
import com.andrei1058.amongusmc.server.ServerManager;
import com.andrei1058.amongusmc.server.multiarena.listener.LobbyProtectionListener;
import com.andrei1058.amongusmc.setup.SetupManager;
import com.andrei1058.amoungusmc.common.api.server.CommonPermission;
import com.andrei1058.spigot.commandlib.FastRootCommand;
import com.andrei1058.spigot.commandlib.FastSubCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class BuildCmd {

    private BuildCmd() {
    }

    public static void register(FastRootCommand root) {
        FastSubCommand build = new FastSubCommand("build");
        root.withSubNode(build);
        build.withPermissions(new String[]{CommonPermission.ADMIN.get(), CommonPermission.ALL.get()})
                .withPermAdditions((s) -> (s instanceof Player) && (LobbyProtectionListener.getLobbyWorld() != null && ((Player) s).getWorld().getName().equals(LobbyProtectionListener.getLobbyWorld())))
                .withDescription((s) -> "&8- &eToggle build access.")
                .withDisplayHover((s) -> "&eEnable/ disable building and breaking blocks in the main lobby world.")
                .withExecutor((sender, args) -> {
                    Player p = (Player) sender;
                    if (LobbyProtectionListener.isBuilder(p.getUniqueId())) {
                        p.sendMessage(ChatColor.GREEN + "You can no longer break or place blocks in the lobby world.");
                        LobbyProtectionListener.removeBuilder(p.getUniqueId());
                    } else {
                        p.sendMessage(ChatColor.GREEN + "You can break and place blocks now!");
                        LobbyProtectionListener.addBuilder(p.getUniqueId());
                    }
                })
        .withPriority(-0.21);
    }
}
