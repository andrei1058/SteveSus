package com.andrei1058.amongusmc.common.command;

import com.andrei1058.amongusmc.common.CommonManager;
import com.andrei1058.amoungusmc.common.api.locale.CommonMessage;
import com.andrei1058.spigot.commandlib.FastRootCommand;
import com.andrei1058.spigot.commandlib.FastSubCommand;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.command.ConsoleCommandSender;

public class DebugCmd {

    private DebugCmd() {
    }

    public static void register(FastRootCommand root) {
        root
                .withSubNode(
                        new FastSubCommand("debug")
                                .withAliases(new String[]{"d"})
                                .withPermAdditions((s) -> s instanceof ConsoleCommandSender)
                                .withDescription((s) -> "&7Toggle debugging logs.")
                                .withExecutor((sender, args) -> {
                                    if (args.length != 1){
                                        sender.sendMessage("Usage: " + root.getName() + " d enable");
                                        sender.sendMessage("Usage: " + root.getName() + " d disable");
                                        return;
                                    }
                                    switch (args[0]){
                                        case "0":
                                        case "d":
                                        case "dis":
                                        case "disable":
                                        case "off":
                                            CommonManager.getINSTANCE().getCommonProvider().showDebuggingLogs(false);
                                            sender.sendMessage("Debug messages are now disabled!");
                                            break;
                                        case "1":
                                        case "e":
                                        case "en":
                                        case "enable":
                                        case "on":
                                            CommonManager.getINSTANCE().getCommonProvider().showDebuggingLogs(true);
                                            sender.sendMessage("Debug messages are now enabled!");
                                            break;
                                    }
                                }));

    }
}
