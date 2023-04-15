package com.andrei1058.stevesus.command;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.command.filter.FilterListener;
import com.andrei1058.stevesus.common.command.CommonCmdManager;
import com.andrei1058.stevesus.config.MainConfig;
import com.andrei1058.stevesus.server.ServerManager;
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
