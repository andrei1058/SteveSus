package com.andrei1058.stevesus.common.selector.command;

import com.andrei1058.spigot.commandlib.fast.FastRootCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubCommand;
import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.api.locale.CommonMessage;
import com.andrei1058.stevesus.common.selector.SelectorManager;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.entity.Player;

@SuppressWarnings("UnstableApiUsage")
public class SelectorCommand {

    /**
     * Append gui command to given root.
     *
     * @param root target root.
     */
    public static void append(FastRootCommand root) {
        root.withSubNode(new FastSubCommand("selector")
                .withAliases(new String[]{"gui"})
                .withPermAdditions(s -> s instanceof Player && !(CommonManager.getINSTANCE().getCommonProvider().isInGame((Player) s) || CommonManager.getINSTANCE().getCommonProvider().isInSetupSession(s)))
                .withDescription((s) -> CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getMsg(s, CommonMessage.CMD_SELECTOR_DESC))
                .withDisplayHover((s) -> CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getMsg(s, CommonMessage.CMD_SELECTOR_DESC))
                .withClickAction(ClickEvent.Action.SUGGEST_COMMAND)
                .withExecutor((s, args) -> {
                    String selector = "main";
                    if (args.length >= 1){
                        selector = args[0];
                    }
                    SelectorManager.getINSTANCE().openToPlayer(((Player) s).getPlayer(), CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getLocale(s), selector);
                }));
    }
}
