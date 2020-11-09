package com.andrei1058.stevesus.server.multiarena.command;

import com.andrei1058.spigot.commandlib.fast.FastRootCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubCommand;
import com.andrei1058.stevesus.arena.ArenaManager;
import com.andrei1058.stevesus.common.api.server.CommonPermission;
import com.andrei1058.stevesus.config.MainConfig;
import com.andrei1058.stevesus.server.ServerManager;
import com.andrei1058.stevesus.server.multiarena.listener.LobbyProtectionListener;
import com.andrei1058.stevesus.setup.SetupManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@SuppressWarnings("UnstableApiUsage")
public class SetLobbyCmd {

    private SetLobbyCmd() {
    }

    public static void register(FastRootCommand root) {
        FastSubCommand setLobby = new FastSubCommand("setLobby");
        root.withSubNode(setLobby);
        setLobby.setPriority(-1.1);
        setLobby.withPermissions(new String[]{CommonPermission.ADMIN.get(), CommonPermission.ALL.get()})
                .withPermAdditions((s) -> (s instanceof Player) && !SetupManager.getINSTANCE().isInSetup(s) && !ArenaManager.getINSTANCE().isInArena((Player) s))
                .withDescription((s) -> "&8- &eSet player spawn point.")
                .withDisplayHover((s) -> "&eSet player spawn point in the main world.")
                .withExecutor((sender, args) -> {
                    ServerManager.getINSTANCE().getConfig().setProperty(MainConfig.MULTI_ARENA_SPAWN_LOC, ((Player)sender).getLocation());
                    ServerManager.getINSTANCE().getConfig().save();
                    LobbyProtectionListener.init(true);
                    sender.sendMessage(ChatColor.GREEN + "Lobby spawn point set!");
                });

    }
}
