package dev.andrei1058.game.arena.command;

import com.andrei1058.spigot.commandlib.fast.FastRootCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubCommand;
import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.locale.Message;
import dev.andrei1058.game.api.server.PluginPermission;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.common.api.server.CommonPermission;
import dev.andrei1058.game.language.LanguageManager;
import dev.andrei1058.game.teleporter.TeleporterManager;
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
                .withPermAdditions(s -> s instanceof Player && ArenaManager.getINSTANCE().isSpectating((Player) s))
                .withDescription(s -> LanguageManager.getINSTANCE().getMsg(s, Message.CMD_TELEPORTER_DESC))
                .withDisplayHover(s -> LanguageManager.getINSTANCE().getMsg(s, Message.CMD_TELEPORTER_DESC))
                .withClickAction(ClickEvent.Action.RUN_COMMAND)
                .withExecutor((s, args) -> {
                    GameArena gameArena = ArenaManager.getINSTANCE().getArenaByPlayer((Player) s);
                    assert gameArena != null;
                    TeleporterManager.openToPlayer(((Player) s), gameArena);
                })
        );
    }
}
