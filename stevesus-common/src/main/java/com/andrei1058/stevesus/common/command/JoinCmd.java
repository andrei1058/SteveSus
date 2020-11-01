package com.andrei1058.stevesus.common.command;

import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.api.arena.DisplayableArena;
import com.andrei1058.stevesus.common.api.locale.CommonMessage;
import com.andrei1058.spigot.commandlib.FastRootCommand;
import com.andrei1058.spigot.commandlib.FastSubCommand;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.entity.Player;

public class JoinCmd {

    private JoinCmd() {
    }

    public static void register(FastRootCommand root) {
        root
                .withSubNode(
                        new FastSubCommand("join")
                                .withAliases(new String[]{"j"})
                                .withPermAdditions(s -> s instanceof Player && !(CommonManager.getINSTANCE().getCommonProvider().isInGame((Player) s) || CommonManager.getINSTANCE().getCommonProvider().isInSetupSession(s)))
                                .withDescription((s) -> CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getMsg(s, CommonMessage.CMD_JOIN_DESC))
                                .withDisplayHover((s) -> CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getMsg(s, CommonMessage.CMD_JOIN_DESC))
                                .withClickAction(ClickEvent.Action.SUGGEST_COMMAND)
                                .withExecutor((sender, args) -> {
                                    Player player = (Player) sender;
                                    String template = null;
                                    if (args.length != 0) {
                                        template = args[0];
                                    }
                                    DisplayableArena arena = CommonManager.getINSTANCE().getCommonProvider().requestGame(player, template);
                                    if (arena == null) {
                                        player.sendMessage(CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getMsg(player, CommonMessage.CMD_JOIN_NOT_FOUND));
                                    } else {
                                        arena.joinPlayer(player, false);
                                    }
                                })
                );
    }
}
