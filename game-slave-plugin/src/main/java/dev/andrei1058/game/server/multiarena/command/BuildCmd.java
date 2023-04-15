package dev.andrei1058.game.server.multiarena.command;

import com.andrei1058.spigot.commandlib.fast.FastRootCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubCommand;
import dev.andrei1058.game.common.api.server.CommonPermission;
import dev.andrei1058.game.server.multiarena.listener.LobbyProtectionListener;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@SuppressWarnings("UnstableApiUsage")
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
