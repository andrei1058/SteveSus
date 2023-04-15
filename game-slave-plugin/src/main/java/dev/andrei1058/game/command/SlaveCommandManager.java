package dev.andrei1058.game.command;

import dev.andrei1058.game.SteveSus;
import dev.andrei1058.game.command.filter.FilterListener;
import dev.andrei1058.game.common.command.CommonCmdManager;
import dev.andrei1058.game.config.MainConfig;
import dev.andrei1058.game.server.ServerManager;
import org.bukkit.Bukkit;

public class SlaveCommandManager extends CommonCmdManager {

    @Override
    protected void registerCommands() {
        super.registerCommands();

        // register local commands
        if (ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.REGISTER_LEAVE_CMD)){
            LeaveCommand leaveCmd = new LeaveCommand();
            leaveCmd.register();
        }

        // register local listener
        // listener used to check for blocked commands
        Bukkit.getPluginManager().registerEvents(new FilterListener(), SteveSus.getInstance());
    }
}
