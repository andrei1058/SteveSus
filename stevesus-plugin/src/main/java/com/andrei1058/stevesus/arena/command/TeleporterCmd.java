package com.andrei1058.stevesus.arena.command;

import com.andrei1058.spigot.commandlib.fast.FastRootCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubCommand;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.api.server.PluginPermission;
import com.andrei1058.stevesus.arena.ArenaHandler;
import com.andrei1058.stevesus.common.api.server.CommonPermission;
import com.andrei1058.stevesus.language.LanguageManager;
import com.andrei1058.stevesus.teleporter.TeleporterManager;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.entity.Player;

/**
 * Teleported is a GUI provided to spectators to teleport to players.
 */
@SuppressWarnings("UnstableApiUsage")
public class TeleporterCmd {

    private TeleporterCmd() {
    }

    public static void register(FastRootCommand root) {
        FastSubCommand teleporter = new FastSubCommand("teleporter");
        root.withSubNode(teleporter
                .withPermissions(new String[]{PluginPermission.CMD_TELEPORTER.get(), PluginPermission.CMD_ADMIN.get(), CommonPermission.ALL.get()})
                .withPermAdditions(s -> s instanceof Player && ArenaHandler.getINSTANCE().isSpectating((Player) s))
                .withDescription(s -> LanguageManager.getINSTANCE().getMsg(s, Message.CMD_TELEPORTER_DESC))
                .withDisplayHover(s -> LanguageManager.getINSTANCE().getMsg(s, Message.CMD_TELEPORTER_DESC))
                .withClickAction(ClickEvent.Action.RUN_COMMAND)
                .withExecutor((s, args) -> {
                    Arena arena = ArenaHandler.getINSTANCE().getArenaByPlayer((Player) s);
                    assert arena != null;
                    TeleporterManager.openToPlayer(((Player) s), arena);
                })
        );
    }
}
