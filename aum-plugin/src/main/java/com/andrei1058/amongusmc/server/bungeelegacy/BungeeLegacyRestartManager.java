package com.andrei1058.amongusmc.server.bungeelegacy;

import com.andrei1058.amongusmc.AmongUsMc;
import com.andrei1058.amongusmc.api.arena.Arena;
import com.andrei1058.amongusmc.arena.ArenaManager;
import com.andrei1058.amongusmc.config.MainConfig;
import com.andrei1058.amongusmc.server.ServerManager;
import org.bukkit.Bukkit;

public class BungeeLegacyRestartManager {

    private static BungeeLegacyRestartManager INSTANCE;

    private int gamesBeforeRestart = ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.BUNGEE_LEGACY_GAMES_BEFORE_RESTART);

    private BungeeLegacyRestartManager(){}

    public static BungeeLegacyRestartManager getInstance(){
        if (INSTANCE == null){
            INSTANCE = new BungeeLegacyRestartManager();
        }
        return INSTANCE;
    }

    public void performAction(Arena arena) {
        boolean restartServer = gamesBeforeRestart > 0 || gamesBeforeRestart-- == 0;
        if (restartServer) {
            String restartCommand = ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.BUNGEE_LEGACY_RESTART_COMMAND);
            AmongUsMc.getInstance().getLogger().warning("Running restart command as console: " + restartCommand);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), restartCommand);
        } else {
            ArenaManager.getINSTANCE().startArenaFromTemplate(arena.getTemplateWorld());
        }
    }
}
