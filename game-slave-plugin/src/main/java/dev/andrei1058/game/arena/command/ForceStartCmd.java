package dev.andrei1058.game.arena.command;

import com.andrei1058.spigot.commandlib.fast.FastRootCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubCommand;
import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.locale.Message;
import dev.andrei1058.game.api.server.PluginPermission;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.common.api.arena.GameState;
import dev.andrei1058.game.common.api.server.CommonPermission;
import dev.andrei1058.game.language.LanguageManager;
import dev.andrei1058.game.server.ServerManager;
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
                            GameArena gameArena = ArenaManager.getINSTANCE().getArenaByPlayer(((Player) s));
                            if (gameArena != null) {
                                return gameArena.getGameState() == GameState.WAITING || gameArena.getGameState() == GameState.STARTING;
                            }
                        }
                    }
                    return false;
                })
                .withExecutor((s, args) -> {
                            Player p = (Player) s;
                            if (args.length == 1 && args[0].equalsIgnoreCase("debug") && p.isOp()) {
                                GameArena gameArena = ArenaManager.getINSTANCE().getArenaByPlayer(p);
                                if (gameArena != null) {
                                    if (gameArena.getGameState() != GameState.STARTING) {
                                        gameArena.switchState(GameState.STARTING);
                                    }
                                    gameArena.setCountdown(5);
                                    p.sendMessage(ChatColor.RED + "Start debugging..");
                                    ServerManager.getINSTANCE().setDebuggingLogs(true);
                                }
                            } else {
                                GameArena gameArena = ArenaManager.getINSTANCE().getArenaByPlayer(p);
                                assert gameArena != null;
                                if (!gameArena.canForceStart()) {
                                    p.sendMessage(LanguageManager.getINSTANCE().getMsg(p, Message.CMD_FORCE_START_FAILED));
                                    return;
                                }

                                if (gameArena.getGameState() != GameState.STARTING) {
                                    gameArena.switchState(GameState.STARTING);
                                    gameArena.setCountdown(15);
                                }
                            }
                        }
                );
        root.withSubNode(start);
    }
}
