package dev.andrei1058.game.command;

import com.andrei1058.spigot.commandlib.fast.FastRootCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubCommand;
import dev.andrei1058.game.api.arena.Arena;
import dev.andrei1058.game.api.locale.Message;
import dev.andrei1058.game.api.server.ServerType;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.language.LanguageManager;
import dev.andrei1058.game.server.ServerManager;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class LeaveCommand extends FastRootCommand {

    protected LeaveCommand() {
        super("leave");
        withAliases(new String[]{"exit", "hub", "lobby"});
        withPermAdditions((s) -> s instanceof Player);
    }

    @Override
    public void execute(@NotNull CommandSender s, @NotNull String[] args, @NotNull String st) {
        Player p = (Player) s;
        if (ServerManager.getINSTANCE().getServerType() == ServerType.MULTI_ARENA) {
            Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer(p);
            if (arena == null) {
                // move to hub server
                ServerManager.getINSTANCE().getDisconnectHandler().performDisconnect(p);
            } else {
                // if in game move to lobby world
                if (arena.isPlayer(p)) {
                    arena.removePlayer(p, false);
                } else {
                    arena.removeSpectator(p, false);
                }
            }
        } else {
            // move to hub server
            ServerManager.getINSTANCE().getDisconnectHandler().performDisconnect(p);
        }
    }

    public static void register(FastRootCommand root) {
        root
                .withSubNode(
                        new FastSubCommand("leave")
                                .withPermAdditions((s) -> s instanceof Player)
                                .withClickAction(ClickEvent.Action.RUN_COMMAND)
                                .withDescription((s) -> LanguageManager.getINSTANCE().getMsg(s, Message.CMD_LEAVE_DESC))
                                .withDisplayHover((s) -> LanguageManager.getINSTANCE().getMsg(s, Message.CMD_LEAVE_DESC))
                        .withExecutor((s, args)->{
                            Player p = (Player) s;
                            if (ServerManager.getINSTANCE().getServerType() == ServerType.MULTI_ARENA) {
                                Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer(p);
                                if (arena == null) {
                                    // move to hub server
                                    ServerManager.getINSTANCE().getDisconnectHandler().performDisconnect(p);
                                } else {
                                    // if in game move to lobby world
                                    if (arena.isPlayer(p)) {
                                        arena.removePlayer(p, false);
                                    } else {
                                        arena.removeSpectator(p, false);
                                    }
                                }
                            } else {
                                // move to hub server
                                ServerManager.getINSTANCE().getDisconnectHandler().performDisconnect(p);
                            }
                        })
                );
    }
}
