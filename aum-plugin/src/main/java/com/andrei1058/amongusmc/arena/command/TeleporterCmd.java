package com.andrei1058.amongusmc.arena.command;

import com.andrei1058.amongusmc.api.arena.Arena;
import com.andrei1058.amongusmc.api.locale.Message;
import com.andrei1058.amongusmc.api.server.PluginPermission;
import com.andrei1058.amongusmc.arena.ArenaManager;
import com.andrei1058.amongusmc.language.LanguageManager;
import com.andrei1058.amongusmc.teleporter.TeleporterManager;
import com.andrei1058.amoungusmc.common.api.server.CommonPermission;
import com.andrei1058.spigot.commandlib.FastRootCommand;
import com.andrei1058.spigot.commandlib.FastSubCommand;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.entity.Player;

/**
 * Teleported is a GUI provided to spectators to teleport to players.
 */
public class TeleporterCmd {

    private TeleporterCmd() {
    }

    public static void register(FastRootCommand root) {
        FastSubCommand teleporter = new FastSubCommand("teleporter");
        root.withSubNode(teleporter
                .withPermissions(new String[]{PluginPermission.CMD_TELEPORTER.get(), PluginPermission.CMD_ADMIN.get(), CommonPermission.ALL.get()})
                .withPermAdditions(s -> s instanceof Player && ArenaManager.getINSTANCE().isSpectating((Player) s))
                .withDescription(s -> LanguageManager.getINSTANCE().getMsg(s, Message.CMD_TELEPORTER_DESC))
                .withDisplayHover(s -> LanguageManager.getINSTANCE().getMsg(s, Message.CMD_TELEPORTER_DESC))
                .withClickAction(ClickEvent.Action.RUN_COMMAND)
                .withExecutor((s, args) -> {
                    Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer((Player) s);
                    assert arena != null;
                    TeleporterManager.openToPlayer(((Player) s), arena);
                })
        );
    }
}
