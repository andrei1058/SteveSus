package com.andrei1058.amongusmc.command;

import com.andrei1058.amongusmc.api.arena.Arena;
import com.andrei1058.amongusmc.api.locale.Message;
import com.andrei1058.amongusmc.api.server.ServerType;
import com.andrei1058.amongusmc.arena.ArenaManager;
import com.andrei1058.amongusmc.language.LanguageManager;
import com.andrei1058.amongusmc.server.ServerManager;
import com.andrei1058.spigot.commandlib.FastRootCommand;
import com.andrei1058.spigot.commandlib.FastSubCommand;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
