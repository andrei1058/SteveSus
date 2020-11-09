package com.andrei1058.stevesus.arena.command;

import com.andrei1058.spigot.commandlib.fast.FastRootCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubCommand;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.api.server.PluginPermission;
import com.andrei1058.stevesus.arena.ArenaManager;
import com.andrei1058.stevesus.common.api.arena.GameState;
import com.andrei1058.stevesus.common.api.server.CommonPermission;
import com.andrei1058.stevesus.language.LanguageManager;
import com.andrei1058.stevesus.server.ServerManager;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@SuppressWarnings("UnstableApiUsage")
public class ForceStartCmd {

    private ForceStartCmd() {
    }


    public static void register(FastRootCommand root) {
        FastSubCommand start = new FastSubCommand("start");
        start.withClickAction(ClickEvent.Action.RUN_COMMAND)
                .withAliases(new String[]{"forcestart", "force"})
                .withDescription((s) -> LanguageManager.getINSTANCE().getMsg(s, Message.CMD_FORCE_START_DESC))
                .withDisplayHover((s) -> LanguageManager.getINSTANCE().getMsg(s, Message.CMD_FORCE_START_DESC))
                .withPermAdditions((s) -> {
                    if (s instanceof Player) {
                        if (s.hasPermission(PluginPermission.CMD_FORCE_START.get()) || s.hasPermission(CommonPermission.ALL.get())) {
                            Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer(((Player) s));
                            if (arena != null) {
                                return arena.getGameState() == GameState.WAITING || arena.getGameState() == GameState.STARTING;
                            }
                        }
                    }
                    return false;
                })
                .withExecutor((s, args) -> {
                            Player p = (Player) s;
                            if (args.length == 1 && args[0].equalsIgnoreCase("debug") && p.isOp()) {
                                Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer(p);
                                if (arena != null) {
                                    if (arena.getGameState() != GameState.STARTING) {
                                        arena.switchState(GameState.STARTING);
                                    }
                                    arena.setCountdown(5);
                                    p.sendMessage(ChatColor.RED + "Start debugging..");
                                    ServerManager.getINSTANCE().setDebuggingLogs(true);
                                }
                            } else {
                                Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer(p);
                                assert arena != null;
                                if (!arena.canForceStart()) {
                                    p.sendMessage(LanguageManager.getINSTANCE().getMsg(p, Message.CMD_FORCE_START_FAILED));
                                    return;
                                }

                                if (arena.getGameState() != GameState.STARTING) {
                                    arena.switchState(GameState.STARTING);
                                    arena.setCountdown(15);
                                }
                            }
                        }
                );
        root.withSubNode(start);
    }
}
