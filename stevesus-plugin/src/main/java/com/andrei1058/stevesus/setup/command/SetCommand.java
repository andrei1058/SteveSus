package com.andrei1058.stevesus.setup.command;

import ch.jalu.configme.SettingsManager;
import com.andrei1058.spigot.commandlib.ICommandNode;
import com.andrei1058.spigot.commandlib.fast.FastRootCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubRootCommand;
import com.andrei1058.stevesus.api.arena.ArenaTime;
import com.andrei1058.stevesus.api.server.ServerType;
import com.andrei1058.stevesus.api.setup.SetupSession;
import com.andrei1058.stevesus.arena.ArenaHandler;
import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.api.arena.GameState;
import com.andrei1058.stevesus.config.ArenaConfig;
import com.andrei1058.stevesus.server.ServerManager;
import com.andrei1058.stevesus.setup.SetupActivity;
import com.andrei1058.stevesus.setup.SetupManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class SetCommand {

    private SetCommand() {
    }

    public static void append(FastRootCommand mainCommand) {

        FastSubRootCommand root = new FastSubRootCommand("set");
        mainCommand.withSubNode(root
                .withPermAdditions(s -> SetupManager.getINSTANCE().isInSetup(s))
                .withDescription(s -> "&f- Single Options.")
                .withDisplayName(s -> "&e" + root.getName() + " ")
                .withDisplayHover(s -> "&eSet single options")
        );

        FastSubCommand setClonesAvailableAtOnce = new FastSubCommand("clonesAvailableAtOnce");
        FastSubCommand setMinPlayers = new FastSubCommand("minPlayers");
        FastSubCommand setMaxPlayers = new FastSubCommand("maxPlayers");
        FastSubCommand setMeetingButton = new FastSubCommand("meetingButton");
        FastSubCommand ventConnection = new FastSubCommand("ventConnection");
        FastSubCommand displayName = new FastSubCommand("displayName");
        FastSubCommand displayItem = new FastSubCommand("displayItem");
        FastSubCommand timeOfTheDay = new FastSubCommand("time");

        root.withSubNode(setClonesAvailableAtOnce
                .withPermAdditions(s -> SetupManager.getINSTANCE().isInSetup(s) && ServerManager.getINSTANCE().getServerType() != ServerType.BUNGEE_LEGACY)
                .withDescription(s -> "&e[number]")
                .withDisplayName(s -> "&e" + setClonesAvailableAtOnce.getName() + " ")
                .withDisplayHover(s ->
                        "&a&oOptional\n " +
                                "\n&fHow many active copies with WAITING/STARTING state?" +
                                "\n&fWhen an arena is marked as started their template" +
                                "\n&fis cloned until this number is reached.\n" +
                                " " +
                                "\n&eCurrent value: &b" + ArenaCommands.getCurrentProperty(ArenaConfig.CLONES_AVAILABLE_AT_ONCE, s) +
                                "\n&eDefault value: " + ArenaConfig.CLONES_AVAILABLE_AT_ONCE.getDefaultValue())
                .withExecutor((s, args) -> {
                    if (args.length != 1) {
                        s.sendMessage(ChatColor.RED + "You have to provide a number.");
                        return;
                    }
                    int input;
                    try {
                        input = Integer.parseInt(args[0]);
                    } catch (Exception ex) {
                        s.sendMessage(ChatColor.RED + "You have to provide a number.");
                        return;
                    }
                    if (input < 0) {
                        s.sendMessage(ChatColor.RED + ":)))) Nice try, but you can't use negative numbers!");
                        return;
                    }

                    s.sendMessage(ChatColor.GRAY + "Arenas available at once from this template was set to " + ChatColor.AQUA + args[0] + ChatColor.GRAY + ".");

                    if (input == 0) {
                        s.sendMessage(ChatColor.RED + "I hope you know you're doing!");
                        s.sendMessage(ChatColor.GRAY + "Since you set it to " + ChatColor.AQUA + args[0] + ChatColor.GRAY + " no clones will be automatically made available.");
                    }
                    Player player = (Player) s;
                    SettingsManager config = ArenaHandler.getINSTANCE().getTemplate(player.getWorld().getName(), true);
                    config.setProperty(ArenaConfig.CLONES_AVAILABLE_AT_ONCE, input);
                    config.save();
                }))
                .withSubNode(setMinPlayers
                        .withPermAdditions(s -> SetupManager.getINSTANCE().isInSetup(s))
                        .withDisplayName(s -> "&7" + setMinPlayers.getName() + " ")
                        .withDescription(s -> "&7[number]")
                        .withDisplayHover(s -> "&fHow many players are required to start a game?\n " +
                                "\n&eCurrent value: &b" + ArenaCommands.getCurrentProperty(ArenaConfig.MIN_PLAYERS, s) +
                                "\n&eDefault value: " + ArenaConfig.MIN_PLAYERS.getDefaultValue())
                        .withExecutor((s, args) -> {
                            if (args.length != 1) {
                                s.sendMessage(ChatColor.RED + "You have to provide a number.");
                                return;
                            }
                            int input;
                            try {
                                input = Integer.parseInt(args[0]);
                            } catch (Exception ex) {
                                s.sendMessage(ChatColor.RED + "You have to provide a number.");
                                return;
                            }
                            if (input < 1) {
                                s.sendMessage(ChatColor.RED + ":)))) Nice try, but you can't use negative numbers or 0!");
                                return;
                            }

                            s.sendMessage(ChatColor.GRAY + "Minimum players was set to " + ChatColor.AQUA + args[0] + ChatColor.GRAY + ".");

                            Player player = (Player) s;
                            SettingsManager config = ArenaHandler.getINSTANCE().getTemplate(player.getWorld().getName(), true);
                            config.setProperty(ArenaConfig.MIN_PLAYERS, input);
                            config.save();
                        }))
                .withSubNode(setMaxPlayers
                        .withPermAdditions(s -> SetupManager.getINSTANCE().isInSetup(s))
                        .withDisplayName(s -> "&e" + setMaxPlayers.getName() + " ")
                        .withDescription(s -> "&e[number]")
                        .withDisplayHover(s -> "&fHow many players are allowed in a game?\n " +
                                "\n&eCurrent value: &b" + ArenaCommands.getCurrentProperty(ArenaConfig.MAX_PLAYERS, s) +
                                "\n&eDefault value: " + ArenaConfig.MAX_PLAYERS.getDefaultValue())
                        .withExecutor((s, args) -> {
                            if (args.length != 1) {
                                s.sendMessage(ChatColor.RED + "You have to provide a number.");
                                return;
                            }
                            int input;
                            try {
                                input = Integer.parseInt(args[0]);
                            } catch (Exception ex) {
                                s.sendMessage(ChatColor.RED + "You have to provide a number.");
                                return;
                            }
                            if (input < 1) {
                                s.sendMessage(ChatColor.RED + ":)))) Nice try, but you can't use negative numbers or 0!");
                                return;
                            }

                            Player player = (Player) s;
                            SettingsManager config = ArenaHandler.getINSTANCE().getTemplate(player.getWorld().getName(), true);

                            if (config.getProperty(ArenaConfig.MIN_PLAYERS) > input) {
                                s.sendMessage(ChatColor.RED + "Players limit cannot be lower than minimum players: " + args[0]);
                                s.sendMessage(ChatColor.GRAY + "Try changing minimum players first!");
                                return;
                            }

                            s.sendMessage(ChatColor.GRAY + "Player limit was set to " + ChatColor.AQUA + args[0] + ChatColor.GRAY + ".");
                            config.setProperty(ArenaConfig.MAX_PLAYERS, input);
                            config.save();
                        }))
                .withSubNode(setMeetingButton
                        .withPermAdditions((s) -> SetupManager.getINSTANCE().isInSetup(s))
                        .withDisplayName(s -> "&7" + setMeetingButton.getName() + " [at your location] " + (ArenaCommands.isSet(ArenaConfig.MEETING_BUTTON_LOC, s) ? "" : "&c(NOT SET)"))
                        .withDisplayHover((s) -> "&fSet meeting button at your current location.\n " +
                                "\n&eIs set: &b" + ArenaCommands.isSet(ArenaConfig.MEETING_BUTTON_LOC, s))
                        .withExecutor((s, args) -> {
                            Player player = (Player) s;
                            SettingsManager config = ArenaHandler.getINSTANCE().getTemplate(player.getWorld().getName(), true);
                            config.setProperty(ArenaConfig.MEETING_BUTTON_LOC, Optional.of(player.getLocation()));
                            config.save();
                            s.sendMessage(ChatColor.GRAY + "Meeting Button location set!");
                            SetupSession setupSession = SetupManager.getINSTANCE().getSession((Player) s);
                            if (setupSession instanceof SetupActivity) {
                                ((SetupActivity) setupSession).reloadButtonHologram();
                            }
                        })
                )
                .withSubNode(ventConnection
                        .withPermAdditions((s) -> SetupManager.getINSTANCE().isInSetup(s))
                        .withDisplayName(s -> "&e" + ventConnection.getName() + " [vent1] [vent2]")
                        .withDisplayHover((s) -> "&fConnect some vents.")
                        .withExecutor((s, args) -> {
                            if (args.length == 0) {
                                s.sendMessage(ChatColor.RED + "Usage: " + ChatColor.GRAY + ICommandNode.getClickCommand(ventConnection) + " [connection1] [vent2] [...]");
                                return;
                            }
                            Player player = (Player) s;
                            SettingsManager config = ArenaHandler.getINSTANCE().getTemplate(player.getWorld().getName(), true);
                            List<String> ventList = new ArrayList<>(config.getProperty(ArenaConfig.VENTS));

                            // Connect existing vents
                            List<String> existingConn = new ArrayList<>(ventList).stream().filter(vent -> Arrays.stream(args).anyMatch(conn -> vent.startsWith(conn + ";"))).collect(Collectors.toList());
                            if (existingConn.size() < 2) {
                                player.sendMessage(ChatColor.RED + "Cannot connect vents. It looks like they do not exist.");
                                return;
                            }

                            // foreach valid vent
                            existingConn.forEach(vent -> {
                                // vent data string
                                String[] ventData = vent.split(";");
                                // if valid arguments length
                                if (ventData.length == 3) {
                                    StringBuilder connectedVents = new StringBuilder(ventData[1]);
                                    // create connections string for the new vent
                                    if (connectedVents.length() != 0 && connectedVents.charAt(connectedVents.length() - 1) != ',') {
                                        connectedVents.append(",");
                                    }
                                    existingConn.forEach(conn -> {
                                        String connName = conn.split(";")[0];
                                        if (!((connectedVents.toString().contains(connName + ",") || connectedVents.toString().contains("," + connName)) || connName.equals(ventData[0]))) {
                                            connectedVents.append(connName).append(",");
                                        }
                                    });
                                    if (connectedVents.toString().endsWith(",")) {
                                        connectedVents.deleteCharAt(connectedVents.toString().length() - 1);
                                    }
                                    String newVentString = ventData[0] + ";" + connectedVents.toString() + ";" + ventData[2];
                                    ventList.remove(vent);
                                    ventList.add(newVentString);
                                    s.sendMessage(ChatColor.GRAY + "Connected " + ChatColor.GRAY + ventData[0] + ChatColor.GRAY + " to " + ChatColor.YELLOW + connectedVents.toString() + ChatColor.GRAY + ".");
                                }
                            });

                            config.setProperty(ArenaConfig.VENTS, ventList);
                            config.save();
                        }).withTabSuggestions(s -> AddCommand.getVents(((Player) s).getWorld().getName()))
                )
                .withSubNode(displayName
                        .withDisplayName(s -> "&7" + displayName.getName() + " [custom name]")
                        .withDisplayHover((s) -> "&fAssign a custom display name to this template.\n \nUse without arguments for no custom name.\n&eCurrent display name: " + ArenaCommands.getCurrentProperty(ArenaConfig.DISPLAY_NAME, s))
                        .withExecutor((s, args) -> {
                            String customName = "";
                            if (args.length != 0) {
                                StringBuilder stringBuilder = new StringBuilder();
                                Arrays.stream(args).forEach(arg -> stringBuilder.append(arg).append(" "));
                                if (stringBuilder.charAt(stringBuilder.length() - 1) == ' ') {
                                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                                }
                                customName = stringBuilder.toString();
                            }
                            SettingsManager config = ArenaHandler.getINSTANCE().getTemplate(((Player) s).getWorld().getName(), true);
                            config.setProperty(ArenaConfig.DISPLAY_NAME, customName);
                            config.save();
                            s.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Display name st to: " + customName + "&7."));
                        })
                )
                .withSubNode(displayItem
                        .withDisplayName(s -> "&e" + displayItem.getName() + " [item in hand]")
                        .withDisplayHover(s -> "&fAdd a custom display item in arena\n&fselector for this template.\n &eYou can have custom items for each game state.\n&fHold an item in your hand.")
                        .withExecutor((sender, args) -> {
                            if (args.length == 0) {
                                sender.sendMessage(ChatColor.RED + "Usage: " + ChatColor.GRAY + ICommandNode.getClickCommand(displayItem) + " [gameState]");
                                sender.sendMessage(ChatColor.GRAY + "You must be holding the item in your hand.");
                                return;
                            }

                            GameState gameState = GameState.getByNickName(args[0]);
                            if (gameState == null) {
                                sender.sendMessage(ChatColor.RED + "Invalid game state!");
                                sender.sendMessage(ChatColor.GRAY + "Available options: waiting, starting, playing, ending.");
                                return;
                            }

                            Player player = (Player) sender;
                            ItemStack item = player.getInventory().getItemInMainHand();
                            if (item == null || item.getType() == Material.AIR) {
                                sender.sendMessage(ChatColor.RED + "You must be holding the item in your hand!");
                                return;
                            }

                            SettingsManager config = ArenaHandler.getINSTANCE().getTemplate(player.getWorld().getName(), true);
                            String material = item.getType().toString();
                            int data = CommonManager.getINSTANCE().getItemSupport().getItemData(item);

                            switch (gameState) {
                                case WAITING:
                                    config.setProperty(ArenaConfig.SELECTOR_WAITING_MATERIAL, material);
                                    config.setProperty(ArenaConfig.SELECTOR_WAITING_DATA, data);
                                    break;
                                case STARTING:
                                    config.setProperty(ArenaConfig.SELECTOR_STARTING_MATERIAL, material);
                                    config.setProperty(ArenaConfig.SELECTOR_STARTING_DATA, data);
                                    break;
                                case IN_GAME:
                                    config.setProperty(ArenaConfig.SELECTOR_PLAYING_MATERIAL, material);
                                    config.setProperty(ArenaConfig.SELECTOR_PLAYING_DATA, data);
                                    break;
                                case ENDING:
                                    config.setProperty(ArenaConfig.SELECTOR_ENDING_MATERIAL, material);
                                    config.setProperty(ArenaConfig.SELECTOR_ENDING_DATA, data);
                                    break;
                            }

                            config.save();
                            player.sendMessage(ChatColor.GRAY + "Display item for " + ChatColor.YELLOW + args[0] + ChatColor.GRAY + " state set!");
                        })
                )
                .withSubNode(timeOfTheDay
                        .withDisplayName(s -> "&7" + timeOfTheDay.getName() + " [day/ night]")
                        .withDisplayHover(s -> "&fSet time of the game for\n&fthe gameplay..\n \n&eCurrently set to: &b" + ArenaCommands.getCurrentProperty(ArenaConfig.MAP_TIME, s))
                        .withExecutor((sender, args) -> {
                            if (args.length != 1 || ArenaTime.getByName(args[0]) == null) {
                                sender.sendMessage(ChatColor.GRAY + "Available choices: DAY, NIGHT.");
                                return;
                            }
                            ArenaTime time = ArenaTime.getByName(args[0]);

                            SettingsManager config = ArenaHandler.getINSTANCE().getTemplate(((Player) sender).getWorld().getName(), true);
                            config.setProperty(ArenaConfig.MAP_TIME, time);
                            config.save();
                            assert time != null;
                            ((Player)sender).getWorld().setTime(time.getStartTick());
                            sender.sendMessage(ChatColor.GRAY + "Gameplay time set to: " + ChatColor.AQUA + time.toString());
                        })
                        .withTabSuggestions(s -> {
                            List<String> types = new ArrayList<>();
                            Arrays.stream(ArenaTime.values()).forEach(type -> types.add(type.name()));
                            return types;
                        })
                )
        ;
    }
}
