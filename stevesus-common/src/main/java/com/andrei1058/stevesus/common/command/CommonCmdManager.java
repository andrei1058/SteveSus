package com.andrei1058.stevesus.common.command;

import com.andrei1058.spigot.commandlib.CommandLib;
import com.andrei1058.spigot.versionsupport.ChatSupport;
import com.andrei1058.stevesus.common.CommonManager;
import org.bukkit.Bukkit;

public class CommonCmdManager {

    private static CommonCmdManager INSTANCE;

    private SteveSusCmd rootCommand;

    protected CommonCmdManager() {
        // Load chat util
        ChatSupport chatSupport = ChatSupport.SupportBuilder.load();
        if (chatSupport == null) {
            CommonManager.getINSTANCE().getPlugin().getLogger().severe("Server version not supported");
            Bukkit.getPluginManager().disablePlugin(CommonManager.getINSTANCE().getPlugin());
        }
        //

        //noinspection UnstableApiUsage
        CommandLib.init(chatSupport);
    }

    /**
     * Initialize Command Manager.
     */
    public static void onEnable(CommonCmdManager instance) {
        if (INSTANCE != null) return;
        if (instance == null) {
            INSTANCE = new CommonCmdManager();
        } else {
            INSTANCE = instance;
        }
        INSTANCE.registerCommands();
    }

    public static CommonCmdManager getINSTANCE() {
        return INSTANCE;
    }

    // not all commands are registered here
    protected void registerCommands() {
        rootCommand = new SteveSusCmd("ss");
        rootCommand.register();
        JoinCmd.register(CommonCmdManager.getINSTANCE().getMainCmd());
        LangCmd.register(CommonCmdManager.getINSTANCE().getMainCmd());
        DebugCmd.register(CommonCmdManager.getINSTANCE().getMainCmd());
    }

    public SteveSusCmd getMainCmd() {
        return rootCommand;
    }
}
