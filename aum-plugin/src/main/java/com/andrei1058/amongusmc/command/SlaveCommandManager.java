package com.andrei1058.amongusmc.command;

import com.andrei1058.amongusmc.AmongUsMc;
import com.andrei1058.amongusmc.command.filter.FilterListener;
import com.andrei1058.amongusmc.common.command.CommonCmdManager;
import com.andrei1058.amongusmc.config.MainConfig;
import com.andrei1058.amongusmc.server.ServerManager;
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
        Bukkit.getPluginManager().registerEvents(new FilterListener(), AmongUsMc.getInstance());
    }
}
