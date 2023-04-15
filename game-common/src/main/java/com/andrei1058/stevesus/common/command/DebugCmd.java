package com.andrei1058.stevesus.common.command;

import com.andrei1058.spigot.commandlib.fast.FastRootCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubCommand;
import com.andrei1058.stevesus.common.CommonManager;
import org.bukkit.command.ConsoleCommandSender;

@SuppressWarnings("UnstableApiUsage")
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
