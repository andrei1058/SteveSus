package com.andrei1058.amongusmc.common.stats.command;

import com.andrei1058.amongusmc.common.CommonManager;
import com.andrei1058.amongusmc.common.stats.StatsManager;
import com.andrei1058.amoungusmc.common.api.locale.CommonMessage;
import com.andrei1058.spigot.commandlib.FastRootCommand;
import com.andrei1058.spigot.commandlib.FastSubCommand;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.entity.Player;

public class StatsCommand {

    /**
     * Append gui command to given root.
     *
     * @param root target root.
     */
    public static void append(FastRootCommand root) {
        root.withSubNode(new FastSubCommand("stats")
                .withAliases(new String[]{"statistics"})
                .withPermAdditions(s -> (s instanceof Player) && !CommonManager.getINSTANCE().getCommonProvider().isInSetupSession(s))
                .withDescription((s) -> CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getMsg(s, CommonMessage.CMD_STATS_DESC))
                .withDisplayHover((s) -> CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getMsg(s, CommonMessage.CMD_STATS_DESC))
                .withClickAction(ClickEvent.Action.SUGGEST_COMMAND)
                .withExecutor((s, args) -> {
                    String selector = "main";
                    if (args.length >= 1){
                        selector = args[0];
                    }
                    StatsManager.openToPlayer((Player) s, selector);
                }));
    }
}
