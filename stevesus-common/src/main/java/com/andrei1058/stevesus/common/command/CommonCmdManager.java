package com.andrei1058.stevesus.common.command;

public class CommonCmdManager {

    private static CommonCmdManager INSTANCE;

    private SteveSusCmd rootCommand;

    protected CommonCmdManager() {
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
        rootCommand = new SteveSusCmd("au");
        rootCommand.register();
        JoinCmd.register(CommonCmdManager.getINSTANCE().getMainCmd());
        LangCmd.register(CommonCmdManager.getINSTANCE().getMainCmd());
        DebugCmd.register(CommonCmdManager.getINSTANCE().getMainCmd());
    }

    public SteveSusCmd getMainCmd() {
        return rootCommand;
    }
}
