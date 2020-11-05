package com.andrei1058.stevesus.setup.command;

import ch.jalu.configme.SettingsManager;
import com.andrei1058.spigot.commandlib.fast.FastRootCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubRootCommand;
import com.andrei1058.stevesus.arena.ArenaHandler;
import com.andrei1058.stevesus.config.ArenaConfig;
import com.andrei1058.stevesus.setup.SetupManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collections;

@SuppressWarnings("UnstableApiUsage")
public class RemoveCommand {

    private RemoveCommand() {
    }

    public static void append(FastRootCommand rootCommand) {
        FastSubRootCommand root = new FastSubRootCommand("remove");
        rootCommand.withSubNode(root
                .withPermAdditions(s -> SetupManager.getINSTANCE().isInSetup(s))
                .withDescription(s -> "&f- Remove Options.")
                .withDisplayName(s -> "&e" + root.getName() + " ")
                .withDisplayHover(s -> "&eRemove options")
        );

        FastSubCommand clearWaitingSpawns = new FastSubCommand("waitingSpawns");
        FastSubCommand clearMeetingSpawns = new FastSubCommand("meetingSpawns");
        FastSubCommand clearSpectatorSpawns = new FastSubCommand("spectatorSpawns");

        root
                .withSubNode(clearWaitingSpawns
                        .withPermAdditions((s) -> SetupManager.getINSTANCE().isInSetup(s))
                        .withDisplayName(s -> "&7" + clearWaitingSpawns.getName() + " ")
                        .withDisplayHover((s) -> "&fClear waiting spawn locations from config.\n " +
                                "\n&eCurrently set: &b" + ArenaCommands.getCurrentProperty(ArenaConfig.WAITING_LOBBY_LOCATIONS, s).size())
                        .withExecutor((s, args) -> {
                            Player player = (Player) s;
                            SettingsManager config = ArenaHandler.getINSTANCE().getTemplate(player.getWorld().getName(), true);
                            config.setProperty(ArenaConfig.WAITING_LOBBY_LOCATIONS, Collections.emptyList());
                            config.save();
                            s.sendMessage(ChatColor.GRAY + "Waiting spawn locations removed!");
                        }))

                .withSubNode(clearMeetingSpawns
                        .withPermAdditions((s) -> SetupManager.getINSTANCE().isInSetup(s))
                        .withDisplayName(s -> "&e" + clearMeetingSpawns.getName() + " ")
                        .withDisplayHover((s) -> "&fClear meeting spawn locations from config.\n " +
                                "\n&eCurrently set: &b" + ArenaCommands.getCurrentProperty(ArenaConfig.MEETING_LOCATIONS, s).size())
                        .withExecutor((s, args) -> {
                            Player player = (Player) s;
                            SettingsManager config = ArenaHandler.getINSTANCE().getTemplate(player.getWorld().getName(), true);
                            config.setProperty(ArenaConfig.MEETING_LOCATIONS, Collections.emptyList());
                            config.save();
                            s.sendMessage(ChatColor.GRAY + "Meeting spawn locations removed!");
                        }))

                .withSubNode(clearSpectatorSpawns
                        .withPermAdditions((s) -> SetupManager.getINSTANCE().isInSetup(s))
                        .withDisplayName(s -> "&7" + clearSpectatorSpawns.getName() + " ")
                        .withDisplayHover((s) -> "&fClear spectator spawn locations from config.\n " +
                                "\n&eCurrently set: &b" + ArenaCommands.getCurrentProperty(ArenaConfig.SPECTATE_LOCATIONS, s).size())
                        .withExecutor((s, args) -> {
                            Player player = (Player) s;
                            SettingsManager config = ArenaHandler.getINSTANCE().getTemplate(player.getWorld().getName(), true);
                            config.setProperty(ArenaConfig.SPECTATE_LOCATIONS, Collections.emptyList());
                            config.save();
                            s.sendMessage(ChatColor.GRAY + "Spectator spawn locations removed!");
                        }))
        ;
    }
}
