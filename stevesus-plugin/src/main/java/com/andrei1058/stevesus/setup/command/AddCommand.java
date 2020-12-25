package com.andrei1058.stevesus.setup.command;

import ch.jalu.configme.SettingsManager;
import com.andrei1058.spigot.commandlib.ICommandNode;
import com.andrei1058.spigot.commandlib.fast.FastRootCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubRootCommand;
import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.task.TaskProvider;
import com.andrei1058.stevesus.api.prevention.abandon.AbandonCondition;
import com.andrei1058.stevesus.api.setup.SetupSession;
import com.andrei1058.stevesus.arena.ArenaManager;
import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.config.ArenaConfig;
import com.andrei1058.stevesus.config.properties.OrphanLocationProperty;
import com.andrei1058.stevesus.setup.SetupManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
        );

        FastSubCommand addWaitingSpawn = new FastSubCommand("waitingSpawn");
        FastSubCommand addStartSpawn = new FastSubCommand("startSpawn");
        FastSubCommand addMeetingSpawn = new FastSubCommand("meetingSpawn");
        FastSubCommand addSpectatorSpawn = new FastSubCommand("spectatorSpawn");
        FastSubCommand addVent = new FastSubCommand("vent");
        FastSubCommand addTask = new FastSubCommand("task");

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
                        .withDescription(s -> "&7[name]")
                        .withDisplayHover(s -> "&fAdd a task.\n&e" + ICommandNode.getClickCommand(addVent) + " [provider] [task] [localIdentifier]\n \n&fYou can usually add a task multiple times.")
                        .withExecutor((sender, args) -> {
                            if (args.length != 3) {
                                sender.sendMessage(" ");
                                String command = ICommandNode.getClickCommand(addTask);
                                TextComponent usage = new TextComponent(ChatColor.RED + "Usage: " + ChatColor.GRAY + command);
                                usage.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
                                TextComponent provider = new TextComponent(" [provider]");
                                provider.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent(ChatColor.YELLOW + "[provider] " + ChatColor.GRAY + "is the plugin which provides the task. (Like " + SteveSus.getInstance().getName() + ").")}));
                                usage.addExtra(provider);
                                TextComponent task = new TextComponent(" [task]");
                                task.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent(ChatColor.YELLOW + "[task] " + ChatColor.GRAY + "is the task name from provider.")}));
                                usage.addExtra(task);
                                TextComponent localIdentifier = new TextComponent(" [localIdentifier]");
                                localIdentifier.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent(ChatColor.YELLOW + "[localIdentifier] " + ChatColor.GRAY + "is some name that helps you remember this configuration, because you can set a task multiple times and this name is used eventually later if you want to remove this configuration.")}));
                                usage.addExtra(localIdentifier);
                                sender.spigot().sendMessage(usage);
                                sender.sendMessage(" ");
                                sender.sendMessage(ChatColor.GRAY + "Available tasks: ");
                                ArenaManager.getINSTANCE().getRegisteredTasks().forEach(taskHandler -> {
                                    TextComponent textComponent = new TextComponent(ChatColor.GOLD + "- " + ChatColor.GRAY + taskHandler.getProvider().getName() + " " + ChatColor.YELLOW + taskHandler.getIdentifier());
                                    textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent(ChatColor.WHITE + "Click to use " + ChatColor.translateAlternateColorCodes('&', taskHandler.getDefaultDisplayName()))}));
                                    textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command + " " + taskHandler.getProvider().getName() + " " + taskHandler.getIdentifier() + " "));
                                    sender.spigot().sendMessage(textComponent);
                                });
                                return;
                            }

                            TaskProvider task = ArenaManager.getINSTANCE().getTask(args[0], args[1]);
                            if (task == null) {
                                sender.sendMessage(ChatColor.RED + "Invalid provider or task identifier.");
                                return;
                            }
                            Player player = (Player) sender;
                            SetupSession setupSession = SetupManager.getINSTANCE().getSession(player);
                            if (!task.canSetup(player, setupSession)) {
                                player.sendMessage(ChatColor.RED + "You're not allowed to set this task on this map. (This task might allow to be set a single time per template).");
                                return;
                            }
                            if (!args[0].matches(AbandonCondition.IDENTIFIER_REGEX)) {
                                player.sendMessage(ChatColor.RED + args[2] + ChatColor.GRAY + " cannot be used. Try removing special characters.");
                                return;
                            }

                            if (hasTaskWithRememberName(player, args[2])) {
                                player.sendMessage(ChatColor.RED + args[2] + ChatColor.GRAY + " already exists. Please give it another name (it's used to recognize it if you want to remove it eventually).");
                                return;
                            }
                            player.sendMessage(ChatColor.GRAY + "Disabling commands usage...");
                            assert setupSession != null;
                            setupSession.setAllowCommands(false);
                            task.onSetupRequest(player, setupSession, args[2]);
                        })
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
