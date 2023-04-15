package com.andrei1058.stevesus.server.bungeelegacy;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.arena.ArenaManager;
import com.andrei1058.stevesus.config.MainConfig;
import com.andrei1058.stevesus.server.ServerManager;
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
            SteveSus.getInstance().getLogger().warning("Running restart command as console: " + restartCommand);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), restartCommand);
        } else {
            ArenaManager.getINSTANCE().startArenaFromTemplate(arena.getTemplateWorld());
        }
    }
}
