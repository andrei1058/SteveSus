package dev.andrei1058.game.server.bungeelegacy;

import dev.andrei1058.game.SteveSus;
import dev.andrei1058.game.api.arena.Arena;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.config.MainConfig;
import dev.andrei1058.game.server.ServerManager;
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
