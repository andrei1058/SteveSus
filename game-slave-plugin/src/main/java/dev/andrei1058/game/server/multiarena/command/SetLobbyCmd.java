package dev.andrei1058.game.server.multiarena.command;

import com.andrei1058.spigot.commandlib.fast.FastRootCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubCommand;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.common.api.server.CommonPermission;
import dev.andrei1058.game.config.MainConfig;
import dev.andrei1058.game.server.ServerManager;
import dev.andrei1058.game.server.multiarena.listener.LobbyProtectionListener;
import dev.andrei1058.game.setup.SetupManager;
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
