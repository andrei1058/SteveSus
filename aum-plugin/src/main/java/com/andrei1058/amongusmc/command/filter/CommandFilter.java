package com.andrei1058.amongusmc.command.filter;

import ch.jalu.configme.properties.ListProperty;
import com.andrei1058.amongusmc.api.server.PluginPermission;
import com.andrei1058.amongusmc.config.MainConfig;
import com.andrei1058.amongusmc.server.ServerManager;
import org.bukkit.entity.Player;

import java.util.LinkedList;


public class CommandFilter {

    private static CommandFilter PRE_GAME;
    private static CommandFilter IN_GAME;

    private final LinkedList<String> filteredStrings = new LinkedList<>();

    /**
     * @param path {@link MainConfig} path to retrieve list.
     */
    public CommandFilter(ListProperty<String> path) {
        ServerManager.getINSTANCE().getConfig().getProperty(path).forEach(this::addFilter);
    }

    /**
     * Register a new filter or add to existing.
     */
    public void addFilter(String filter) {
        if (filter.trim().isEmpty()) return;
        if (!filteredStrings.contains(filter)){
            filteredStrings.add(filter);
        }
    }

    /**
     * Check if the executed command is allowed or not.
     *
     * @return true if event should be cancelled.
     */
    public boolean checkCommand(Player sender, String message) {
        if (message.isEmpty()) return false;
        if (sender.hasPermission(PluginPermission.CHAT_FILTER_BYPASS.get())) return false;
        return filteredStrings.stream().anyMatch(message::startsWith);
    }

    /**
     * Initialize command whitelist for pre-game and in-game.
     */
    public static void init() {
        PRE_GAME = new CommandFilter(MainConfig.ALLOWED_CMD_PRE_GAME);
        IN_GAME = new CommandFilter(MainConfig.ALLOWED_CMD_IN_GAME);
    }

    public static CommandFilter getInGame() {
        return IN_GAME;
    }

    public static CommandFilter getPreGame() {
        return PRE_GAME;
    }
}
