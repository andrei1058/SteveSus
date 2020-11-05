package com.andrei1058.stevesus.setup.command;

import ch.jalu.configme.SettingsManager;
import com.andrei1058.spigot.commandlib.ICommandNode;
import com.andrei1058.spigot.commandlib.fast.FastRootCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubRootCommand;
import com.andrei1058.stevesus.api.prevention.abandon.AbandonCondition;
import com.andrei1058.stevesus.arena.ArenaHandler;
import com.andrei1058.stevesus.config.ArenaConfig;
import com.andrei1058.stevesus.config.properties.OrphanLocationProperty;
import com.andrei1058.stevesus.setup.SetupManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("UnstableApiUsage")
public class AddCommand {

    private AddCommand() {
    }


    public static void append(FastRootCommand rootCommand) {

        FastSubRootCommand root = new FastSubRootCommand("add");
        rootCommand.withSubNode(root
                .withPermAdditions(s -> SetupManager.getINSTANCE().isInSetup(s))
                .withDescription(s -> "&f- Multiple Options.")
                .withDisplayName(s -> "&7" + root.getName() + " ")
                .withDisplayHover(s -> "&eSet multiple options")
        );

        FastSubCommand addWaitingSpawn = new FastSubCommand("waitingSpawn");
        FastSubCommand addMeetingSpawn = new FastSubCommand("meetingSpawn");
        FastSubCommand addSpectatorSpawn = new FastSubCommand("spectatorSpawn");
        FastSubCommand addVent = new FastSubCommand("vent");

        root
                .withSubNode(addWaitingSpawn
                        .withPermAdditions((s) -> SetupManager.getINSTANCE().isInSetup(s))
                        .withDisplayName(s -> "&e" + addWaitingSpawn.getName() + " ")
                        .withDescription(s -> "&e[at your location]")
                        .withDisplayHover((s) -> "&fAdd a waiting spawn at your current location.\n " +
                                "\n&eCurrently added: &b" + ArenaCommands.getCurrentProperty(ArenaConfig.WAITING_LOBBY_LOCATIONS, s).size())
                        .withExecutor((s, args) -> {
                            Player player = (Player) s;
                            SettingsManager config = ArenaHandler.getINSTANCE().getTemplate(player.getWorld().getName(), true);
                            List<Location> waitingLocations = new ArrayList<>(config.getProperty(ArenaConfig.WAITING_LOBBY_LOCATIONS));
                            waitingLocations.add(player.getLocation());
                            config.setProperty(ArenaConfig.WAITING_LOBBY_LOCATIONS, waitingLocations);
                            config.save();
                            s.sendMessage(ChatColor.GRAY + "Waiting spawn location added!");
                        }))
                .withSubNode(addMeetingSpawn
                        .withPermAdditions((s) -> SetupManager.getINSTANCE().isInSetup(s))
                        .withDisplayName(s -> "&7" + addMeetingSpawn.getName() + " ")
                        .withDescription(s -> "&7[at your location]")
                        .withDisplayHover((s) -> "&fAdd a meeting spawn at your current location.\n " +
                                "\n&eCurrently added: &b" + ArenaCommands.getCurrentProperty(ArenaConfig.MEETING_LOCATIONS, s).size())
                        .withExecutor((s, args) -> {
                            Player player = (Player) s;
                            SettingsManager config = ArenaHandler.getINSTANCE().getTemplate(player.getWorld().getName(), true);
                            List<Location> locations = new ArrayList<>(config.getProperty(ArenaConfig.MEETING_LOCATIONS));
                            locations.add(player.getLocation());
                            config.setProperty(ArenaConfig.MEETING_LOCATIONS, locations);
                            config.save();
                            s.sendMessage(ChatColor.GRAY + "Meeting spawn location added!");
                        }))
                .withSubNode(addSpectatorSpawn
                        .withPermAdditions((s) -> SetupManager.getINSTANCE().isInSetup(s))
                        .withDisplayName(s -> "&e" + addSpectatorSpawn.getName() + " ")
                        .withDescription(s -> "&e[at your location]")
                        .withDisplayHover((s) -> "&fAdd a spectator spawn at your current location.\n " +
                                "\n&eCurrently added: &b" + ArenaCommands.getCurrentProperty(ArenaConfig.SPECTATE_LOCATIONS, s).size())
                        .withExecutor((s, args) -> {
                            Player player = (Player) s;
                            SettingsManager config = ArenaHandler.getINSTANCE().getTemplate(player.getWorld().getName(), true);
                            List<Location> locations = new ArrayList<>(config.getProperty(ArenaConfig.SPECTATE_LOCATIONS));
                            locations.add(player.getLocation());
                            config.setProperty(ArenaConfig.SPECTATE_LOCATIONS, locations);
                            config.save();
                            s.sendMessage(ChatColor.GRAY + "Spectator spawn location saved!");
                        }))
                .withSubNode(addVent
                        .withDisplayName(s -> "&7" + addVent.getName() + " ")
                        .withDescription(s -> "&7[at your location]")
                        .withDisplayHover(s -> "&fAdd a vent at your current location.\n&e" + ICommandNode.getClickCommand(addVent) + " [name] [connection1] [conn2] [..]")
                        .withExecutor((sender, args) -> {
                            if (args.length == 0) {
                                sender.sendMessage(color("&cUsage: &7" + ICommandNode.getClickCommand(addVent) + " [name] [ventConnection1] [connection2] [...]"));
                                return;
                            }
                            if (!args[0].matches(AbandonCondition.IDENTIFIER_REGEX)) {
                                sender.sendMessage(color("&cInvalid vent name! Try removing special characters."));
                                return;
                            }
                            Player player = (Player) sender;
                            SettingsManager config = ArenaHandler.getINSTANCE().getTemplate(player.getWorld().getName(), true);
                            List<String> ventList = new ArrayList<>(config.getProperty(ArenaConfig.VENTS));
                            if (ventList.stream().anyMatch(vent -> vent.startsWith(args[0]))) {
                                sender.sendMessage(color("&cA vent with the given game already exists!"));
                                return;
                            }

                            sender.sendMessage(color("&7Added vent: &e" + args[0] + "&7."));

                            StringBuilder connections = new StringBuilder();

                            if (args.length > 1) {
                                Arrays.stream(Arrays.copyOfRange(args, 1, args.length)).forEach(conn -> {
                                    // check if connection vent exist
                                    String ventString = ventList.stream().filter(vent -> vent.startsWith(conn + ";")).findFirst().orElse(null);
                                    if (ventString != null && !(args[0].equals(conn) || (connections.toString().contains(conn + ",") || connections.toString().contains("," + conn)))) {
                                        // create connections string for the new vent
                                        if (connections.length() != 0 && connections.charAt(connections.length() - 1) != ',') {
                                            connections.append(",");
                                        }
                                        connections.append(conn);
                                        // update connections for current connection vent
                                        String[] currentVentData = ventString.split(";");
                                        if (currentVentData.length == 3) {
                                            ventList.remove(ventString);
                                            String newVentData = currentVentData[0] + ";";
                                            newVentData += currentVentData[1].isEmpty() ? args[0] : currentVentData[1] + (currentVentData[1].charAt(currentVentData[1].length() - 1) == ',' ? "" : ",") + args[0];
                                            newVentData += ";" + currentVentData[2];
                                            ventList.add(newVentData);
                                        }
                                        sender.sendMessage(color("&7Connected to: &e" + conn));
                                    } else {
                                        sender.sendMessage(color("&7Cannot connect &a" + args[0] + "&7 to &c" + conn + "&7."));
                                    }
                                });
                            }

                            // save updated vents
                            OrphanLocationProperty orphanLocationProperty = new OrphanLocationProperty();
                            ventList.add(args[0] + ";" + connections.toString() + ";" + orphanLocationProperty.toExportValue(player.getLocation()));
                            config.setProperty(ArenaConfig.VENTS, ventList);
                            config.save();
                        }).withTabSuggestions(s -> getVents(((Player) s).getWorld().getName())))
        ;
    }


    private static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    protected static List<String> getVents(String template) {
        List<String> vents = new ArrayList<>();
        ArenaHandler.getINSTANCE().getTemplate(template, false).getProperty(ArenaConfig.VENTS).forEach(vent -> {
            vents.add(vent.split(";")[0]);
        });
        return vents;
    }
}
