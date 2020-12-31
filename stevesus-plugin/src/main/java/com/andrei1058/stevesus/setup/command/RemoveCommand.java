package com.andrei1058.stevesus.setup.command;

import ch.jalu.configme.SettingsManager;
import com.andrei1058.spigot.commandlib.ICommandNode;
import com.andrei1058.spigot.commandlib.fast.FastRootCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubRootCommand;
import com.andrei1058.stevesus.api.setup.SetupSession;
import com.andrei1058.stevesus.arena.ArenaManager;
import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.config.ArenaConfig;
import com.andrei1058.stevesus.setup.SetupManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class RemoveCommand {

    private RemoveCommand() {
    }

    public static void append(FastRootCommand rootCommand) {
        FastSubRootCommand root = new FastSubRootCommand("remove");
        rootCommand.withSubNode(root
                .withPermAdditions(s -> SetupManager.getINSTANCE().isInSetup(s) && s instanceof Player && Objects.requireNonNull(SetupManager.getINSTANCE().getSession((Player) s)).canUseCommands())
                .withDescription(s -> "&f- Remove Options.")
                .withDisplayName(s -> "&e" + root.getName() + " ")
                .withDisplayHover(s -> "&eRemove options")
                .withHeaderContent("&1|| &3" + CommonManager.getINSTANCE().getPlugin().getName() + "&7 by " + Arrays.toString(CommonManager.getINSTANCE().getPlugin().getDescription().getAuthors().toArray()))
                .withHeaderHover("&av" + CommonManager.getINSTANCE().getPlugin().getDescription().getVersion())
        );

        FastSubCommand clearWaitingSpawns = new FastSubCommand("waitingSpawns");
        FastSubCommand clearStartSpawns = new FastSubCommand("startSpawns");
        FastSubCommand clearMeetingSpawns = new FastSubCommand("meetingSpawns");
        FastSubCommand clearSpectatorSpawns = new FastSubCommand("spectatorSpawns");
        FastSubCommand removeTask = new FastSubCommand("task");

        root
                .withSubNode(clearWaitingSpawns
                        .withPermAdditions((s) -> SetupManager.getINSTANCE().isInSetup(s))
                        .withDisplayName(s -> "&7" + clearWaitingSpawns.getName() + " ")
                        .withDisplayHover((s) -> "&fClear waiting spawn locations from config.\n " +
                                "\n&eCurrently set: &b" + ArenaCommands.getCurrentProperty(ArenaConfig.WAITING_LOBBY_LOCATIONS, s).size())
                        .withExecutor((s, args) -> {
                            Player player = (Player) s;
                            SettingsManager config = ArenaManager.getINSTANCE().getTemplate(player.getWorld().getName(), true);
                            config.setProperty(ArenaConfig.WAITING_LOBBY_LOCATIONS, Collections.emptyList());
                            config.save();
                            s.sendMessage(ChatColor.GRAY + "Waiting spawn locations removed!");
                        }))
                .withSubNode(clearStartSpawns
                        .withPermAdditions((s) -> SetupManager.getINSTANCE().isInSetup(s))
                        .withDisplayName(s -> "&e" + clearStartSpawns.getName() + " ")
                        .withDisplayHover((s) -> "&fClear start spawn locations from config.\n " +
                                "\n&eCurrently set: &b" + ArenaCommands.getCurrentProperty(ArenaConfig.START_LOCATIONS, s).size())
                        .withExecutor((s, args) -> {
                            Player player = (Player) s;
                            SettingsManager config = ArenaManager.getINSTANCE().getTemplate(player.getWorld().getName(), true);
                            config.setProperty(ArenaConfig.START_LOCATIONS, Collections.emptyList());
                            config.save();
                            s.sendMessage(ChatColor.GRAY + "Start spawn locations removed!");
                        }))

                .withSubNode(clearMeetingSpawns
                        .withPermAdditions((s) -> SetupManager.getINSTANCE().isInSetup(s))
                        .withDisplayName(s -> "&7" + clearMeetingSpawns.getName() + " ")
                        .withDisplayHover((s) -> "&fClear meeting spawn locations from config.\n " +
                                "\n&eCurrently set: &b" + ArenaCommands.getCurrentProperty(ArenaConfig.MEETING_LOCATIONS, s).size())
                        .withExecutor((s, args) -> {
                            Player player = (Player) s;
                            SettingsManager config = ArenaManager.getINSTANCE().getTemplate(player.getWorld().getName(), true);
                            config.setProperty(ArenaConfig.MEETING_LOCATIONS, Collections.emptyList());
                            config.save();
                            s.sendMessage(ChatColor.GRAY + "Meeting spawn locations removed!");
                        }))

                .withSubNode(clearSpectatorSpawns
                        .withPermAdditions((s) -> SetupManager.getINSTANCE().isInSetup(s))
                        .withDisplayName(s -> "&e" + clearSpectatorSpawns.getName() + " ")
                        .withDisplayHover((s) -> "&fClear spectator spawn locations from config.\n " +
                                "\n&eCurrently set: &b" + ArenaCommands.getCurrentProperty(ArenaConfig.SPECTATE_LOCATIONS, s).size())
                        .withExecutor((s, args) -> {
                            Player player = (Player) s;
                            SettingsManager config = ArenaManager.getINSTANCE().getTemplate(player.getWorld().getName(), true);
                            config.setProperty(ArenaConfig.SPECTATE_LOCATIONS, Collections.emptyList());
                            config.save();
                            s.sendMessage(ChatColor.GRAY + "Spectator spawn locations removed!");
                        }))

                .withSubNode(removeTask
                        .withDisplayName(s -> "&7" + removeTask.getName() + " ")
                        .withDisplayHover((s) -> "&fRemove a task by the name you gave it when you set it up.")
                        .withExecutor((s, args) -> {

                            if (args.length != 1 || !AddCommand.hasTaskWithRememberName(s, args[0])) {
                                s.sendMessage(ChatColor.RED + "Usage: " + ChatColor.GRAY + ICommandNode.getClickCommand(removeTask) + " [theNameYouGaveIt]");
                                s.sendMessage(ChatColor.GREEN + "Tasks you set: ");
                                SettingsManager config = ArenaManager.getINSTANCE().getTemplate(((Player) s).getWorld().getName(), true);
                                config.getProperty(ArenaConfig.TASKS).forEach(task -> {
                                    String[] taskData = task.split(";");
                                    if (taskData.length == 4) {
                                        TextComponent msg = new TextComponent(ChatColor.GRAY + "- " + ChatColor.GREEN + taskData[0] + ChatColor.GRAY + " " + ChatColor.ITALIC + taskData[1] + " " + taskData[2]);
                                        msg.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ICommandNode.getClickCommand(removeTask) + taskData[0]));
                                        s.spigot().sendMessage(msg);
                                    }
                                });
                                return;
                            }

                            SetupSession setupSession = SetupManager.getINSTANCE().getSession((Player) s);
                            assert setupSession != null;
                            ArenaManager.getINSTANCE().deleteTaskData(setupSession, args[0]);
                            s.sendMessage(ChatColor.GRAY + "Task removed: " + ChatColor.YELLOW + args[0]);
                        }))
        ;
    }
}
