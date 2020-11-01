package com.andrei1058.amongusmc.server.multiarena.command;

import com.andrei1058.amongusmc.arena.ArenaManager;
import com.andrei1058.amongusmc.config.MainConfig;
import com.andrei1058.amongusmc.server.ServerManager;
import com.andrei1058.amongusmc.server.multiarena.listener.LobbyProtectionListener;
import com.andrei1058.amongusmc.setup.SetupManager;
import com.andrei1058.amoungusmc.common.api.server.CommonPermission;
import com.andrei1058.spigot.commandlib.FastRootCommand;
import com.andrei1058.spigot.commandlib.FastSubCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

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
