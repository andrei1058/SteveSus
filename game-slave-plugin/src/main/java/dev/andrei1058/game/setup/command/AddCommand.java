package dev.andrei1058.game.setup.command;

import ch.jalu.configme.SettingsManager;
import com.andrei1058.spigot.commandlib.ICommandNode;
import com.andrei1058.spigot.commandlib.fast.FastRootCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubRootCommand;
import dev.andrei1058.game.SteveSus;
import dev.andrei1058.game.api.prevention.abandon.AbandonCondition;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.common.CommonManager;
import dev.andrei1058.game.config.ArenaConfig;
import dev.andrei1058.game.config.properties.OrphanLocationProperty;
import dev.andrei1058.game.setup.SetupManager;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class AddCommand {

    private AddCommand() {
    }


    public static void append(FastRootCommand rootCommand) {

        FastSubRootCommand root = new FastSubRootCommand("add");
        rootCommand.withSubNode(root
                .withPermAdditions(s -> SetupManager.getINSTANCE().isInSetup(s) && s instanceof Player && Objects.requireNonNull(SetupManager.getINSTANCE().getSession((Player) s)).canUseCommands())
                .withDescription(s -> "&f- Multiple Options.")
                .withDisplayName(s -> "&7" + root.getName() + " ")
                .withDisplayHover(s -> "&eSet multiple options")
                .withHeaderContent("&1|| &3" + CommonManager.getINSTANCE().getPlugin().getName() + "&7 by " + Arrays.toString(CommonManager.getINSTANCE().getPlugin().getDescription().getAuthors().toArray()))
                .withHeaderHover("&av" + CommonManager.getINSTANCE().getPlugin().getDescription().getVersion())
        );

        FastSubCommand addWaitingSpawn = new FastSubCommand("waitingSpawn");
        FastSubCommand addStartSpawn = new FastSubCommand("startSpawn");
        FastSubCommand addMeetingSpawn = new FastSubCommand("meetingSpawn");
        FastSubCommand addSpectatorSpawn = new FastSubCommand("spectatorSpawn");
        FastSubCommand addVent = new FastSubCommand("vent");
        FastSubRootCommand addTask = new FastSubRootCommand("task");

        root
                .withSubNode(addWaitingSpawn
                        .withPermAdditions((s) -> SetupManager.getINSTANCE().isInSetup(s))
                        .withDisplayName(s -> "&e" + addWaitingSpawn.getName() + " ")
                        .withDescription(s -> "&e[at your location]")
                        .withDisplayHover((s) -> "&fAdd a waiting spawn at your current location.\n " +
                                "\n&eCurrently added: &b" + ArenaCommands.getCurrentProperty(ArenaConfig.WAITING_LOBBY_LOCATIONS, s).size())
                        .withExecutor((s, args) -> {
                            Player player = (Player) s;
                            SettingsManager config = ArenaManager.getINSTANCE().getTemplate(player.getWorld().getName(), true);
                            List<Location> waitingLocations = new ArrayList<>(config.getProperty(ArenaConfig.WAITING_LOBBY_LOCATIONS));
                            waitingLocations.add(player.getLocation());
                            config.setProperty(ArenaConfig.WAITING_LOBBY_LOCATIONS, waitingLocations);
                            config.save();
                            s.sendMessage(ChatColor.GRAY + "Waiting spawn location added!");
                        }))
                .withSubNode(addStartSpawn
                        .withPermAdditions((s) -> SetupManager.getINSTANCE().isInSetup(s))
                        .withDisplayName(s -> "&7" + addStartSpawn.getName() + " ")
                        .withDescription(s -> "&7[at your location]")
                        .withDisplayHover((s) -> "&fAdd a start spawn at your current location.\n " +
                                "\n&eCurrently added: &b" + ArenaCommands.getCurrentProperty(ArenaConfig.START_LOCATIONS, s).size())
                        .withExecutor((s, args) -> {
                            Player player = (Player) s;
                            SettingsManager config = ArenaManager.getINSTANCE().getTemplate(player.getWorld().getName(), true);
                            List<Location> waitingLocations = new ArrayList<>(config.getProperty(ArenaConfig.START_LOCATIONS));
                            waitingLocations.add(player.getLocation());
                            config.setProperty(ArenaConfig.START_LOCATIONS, waitingLocations);
                            config.save();
                            s.sendMessage(ChatColor.GRAY + "Start spawn location added!");
                        }))
                .withSubNode(addMeetingSpawn
                        .withPermAdditions((s) -> SetupManager.getINSTANCE().isInSetup(s))
                        .withDisplayName(s -> "&e" + addMeetingSpawn.getName() + " ")
                        .withDescription(s -> "&e[at your location]")
                        .withDisplayHover((s) -> "&fAdd a meeting spawn at your current location.\n " +
                                "\n&eCurrently added: &b" + ArenaCommands.getCurrentProperty(ArenaConfig.MEETING_LOCATIONS, s).size())
                        .withExecutor((s, args) -> {
                            Player player = (Player) s;
                            SettingsManager config = ArenaManager.getINSTANCE().getTemplate(player.getWorld().getName(), true);
                            List<Location> locations = new ArrayList<>(config.getProperty(ArenaConfig.MEETING_LOCATIONS));
                            locations.add(player.getLocation());
                            config.setProperty(ArenaConfig.MEETING_LOCATIONS, locations);
                            config.save();
                            s.sendMessage(ChatColor.GRAY + "Meeting spawn location added!");
                        }))
                .withSubNode(addSpectatorSpawn
                        .withPermAdditions((s) -> SetupManager.getINSTANCE().isInSetup(s))
                        .withDisplayName(s -> "&7" + addSpectatorSpawn.getName() + " ")
                        .withDescription(s -> "&7[at your location]")
                        .withDisplayHover((s) -> "&fAdd a spectator spawn at your current location.\n " +
                                "\n&eCurrently added: &b" + ArenaCommands.getCurrentProperty(ArenaConfig.SPECTATE_LOCATIONS, s).size())
                        .withExecutor((s, args) -> {
                            Player player = (Player) s;
                            SettingsManager config = ArenaManager.getINSTANCE().getTemplate(player.getWorld().getName(), true);
                            List<Location> locations = new ArrayList<>(config.getProperty(ArenaConfig.SPECTATE_LOCATIONS));
                            locations.add(player.getLocation());
                            config.setProperty(ArenaConfig.SPECTATE_LOCATIONS, locations);
                            config.save();
                            s.sendMessage(ChatColor.GRAY + "Spectator spawn location saved!");
                        }))
                .withSubNode(addVent
                        .withDisplayName(s -> "&e" + addVent.getName() + " ")
                        .withDescription(s -> "&e[at your location]")
                        .withDisplayHover(s -> "&fAdd a vent at your current location.\n&bThe item in your hand is used as display item.\n&e" + ICommandNode.getClickCommand(addVent) + " [name] [connection1] [conn2] [..]")
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
                            SettingsManager config = ArenaManager.getINSTANCE().getTemplate(player.getWorld().getName(), true);
                            List<String> ventList = new ArrayList<>(config.getProperty(ArenaConfig.VENTS));
                            if (ventList.stream().anyMatch(vent -> vent.startsWith(args[0]))) {
                                sender.sendMessage(color("&cA vent with the given game already exists!"));
                                return;
                            }

                            String material = "BEDROCK";
                            byte data = 0;

                            ItemStack item = player.getInventory().getItemInMainHand();
                            if (item != null && item.getType() != Material.AIR) {
                                material = item.getType().toString();
                                data = CommonManager.getINSTANCE().getItemSupport().getItemData(item);
                            }

                            sender.sendMessage(color("&7Added vent: &e" + args[0] + "&7 with display item: &e" + material + ":" + data + "&7."));

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
                            ventList.add(args[0] + ";" + connections.toString() + ";" + orphanLocationProperty.toExportValue(player.getLocation()) + ";" + material + "," + data);
                            config.setProperty(ArenaConfig.VENTS, ventList);
                            config.save();
                        }).withTabSuggestions(s -> getVents(((Player) s).getWorld().getName()))
                )

                .withSubNode(addTask
                        .withDisplayName(s -> "&7" + addTask.getName() + " ")
                        .withDescription(s -> "&7[provider] [name]")
                        .withDisplayHover(s -> "&fAdd a task.\n" +
                                "&e" + ICommandNode.getClickCommand(addVent) + " [provider] [task] [localIdentifier]\n " +
                                "\n&fYou can usually add a task multiple times.\n " +
                                "\n" + ChatColor.YELLOW + "[provider] " + ChatColor.GRAY + "is the plugin which provides the task. (Like " + SteveSus.getInstance().getName() + ")." +
                                "\n" + ChatColor.YELLOW + "[task] " + ChatColor.GRAY + "is the task name from provider." +
                                "\n" + ChatColor.YELLOW + "[localIdentifier] " + ChatColor.GRAY.toString() + "is some name that helps you remember this configuration, " +
                                "\n" + ChatColor.GRAY.toString() + "because you can set a task multiple times and this name is used eventually later" +
                                "\n" + ChatColor.GRAY.toString() + "if you want to remove this configuration.")
                        .withClickAction(ClickEvent.Action.SUGGEST_COMMAND)
                )
                //gray
                .withSubNode(new AddRoomCommand())
        ;
    }


    private static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    protected static List<String> getVents(String template) {
        List<String> vents = new ArrayList<>();
        ArenaManager.getINSTANCE().getTemplate(template, true).getProperty(ArenaConfig.VENTS).forEach(vent -> {
            vents.add(vent.split(";")[0]);
        });
        return vents;
    }

    protected static List<String> getAvailableTasks() {
        List<String> tasks = new ArrayList<>();
        ArenaManager.getINSTANCE().getRegisteredTasks().forEach(task -> tasks.add(task.getProvider().getName() + " " + task.getIdentifier()));
        return tasks;
    }

    public static boolean hasTaskWithRememberName(CommandSender player, String nameToCheck) {
        if (!(player instanceof Player)) return false;
        return ArenaManager.getINSTANCE().getTemplate(((Player) player).getWorld().getName(), true).getProperty(ArenaConfig.TASKS).stream().anyMatch(task -> {
            String[] taskData = task.split(";");
            if (taskData.length != 0) {
                return taskData[0].equals(nameToCheck);
            }
            return false;
        });
    }

    public static boolean hasTaskWithRememberName(String world, String nameToCheck) {
        return ArenaManager.getINSTANCE().getTemplate(world, false).getProperty(ArenaConfig.TASKS).stream().anyMatch(task -> {
            String[] taskData = task.split(";");
            if (taskData.length != 0) {
                return taskData[0].equals(nameToCheck);
            }
            return false;
        });
    }
}
