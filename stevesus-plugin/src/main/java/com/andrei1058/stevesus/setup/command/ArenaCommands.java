package com.andrei1058.stevesus.setup.command;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.properties.Property;
import com.andrei1058.spigot.commandlib.fast.FastRootCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubCommand;
import com.andrei1058.stevesus.api.server.ServerType;
import com.andrei1058.stevesus.api.setup.SetupSession;
import com.andrei1058.stevesus.arena.ArenaHandler;
import com.andrei1058.stevesus.config.ArenaConfig;
import com.andrei1058.stevesus.server.ServerManager;
import com.andrei1058.stevesus.setup.SetupManager;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class ArenaCommands {

    private ArenaCommands() {
    }

    public static void register(FastRootCommand root) {

        FastSubCommand setClonesAvailableAtOnce = new FastSubCommand("setClonesAvailableAtOnce");
        FastSubCommand toggleLoadAtStartup = new FastSubCommand("toggleLoadAtStartup");
        FastSubCommand setMinPlayers = new FastSubCommand("setMinPlayers");
        FastSubCommand setMaxPlayers = new FastSubCommand("setMaxPlayers");
        FastSubCommand addWaitingSpawn = new FastSubCommand("addWaitingSpawn");
        FastSubCommand clearWaitingSpawns = new FastSubCommand("clearWaitingSpawns");
        FastSubCommand addSpectatorSpawn = new FastSubCommand("addSpectatorSpawn");
        FastSubCommand clearSpectatorSpawns = new FastSubCommand("clearSpectatorSpawns");
        FastSubCommand saveChanges = new FastSubCommand("saveChanges");
        root
                .withSubNode(setClonesAvailableAtOnce
                        .withPermAdditions(s -> SetupManager.getINSTANCE().isInSetup(s) && ServerManager.getINSTANCE().getServerType() != ServerType.BUNGEE_LEGACY)
                        .withDescription(s -> "&e[number]")
                        .withDisplayName(s -> "&e" + setClonesAvailableAtOnce.getName() + " ")
                        .withDisplayHover(s ->
                                "&a&oOptional\n " +
                                        "\n&fHow many active copies with WAITING/STARTING state?" +
                                        "\n&fWhen an arena is marked as started their template" +
                                        "\n&fis cloned until this number is reached.\n" +
                                        " " +
                                        "\n&eCurrent value: &b" + getCurrentProperty(ArenaConfig.CLONES_AVAILABLE_AT_ONCE, s) +
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
                .withSubNode(toggleLoadAtStartup
                        .withClickAction(ClickEvent.Action.RUN_COMMAND)
                        .withPermAdditions(s -> SetupManager.getINSTANCE().isInSetup(s))
                        .withDisplayName(s -> "&7" + toggleLoadAtStartup.getName() + " ")
                        .withDisplayHover(s -> "&a&oOptional\n " +
                                "\n&fToggle whether to create or not games from this" +
                                "\n&ftemplate when the server starts.\n" +
                                " " +
                                "\n&fYou may need to set this to false if this" +
                                "\n&ftemplate is going to be handled by an addon." +
                                "\n&eCurrent value: &b" + getCurrentProperty(ArenaConfig.LOAD_AT_START_UP, s) +
                                "\n&eDefault value: " + ArenaConfig.LOAD_AT_START_UP.getDefaultValue())
                        .withExecutor((s, args) -> {
                            Player player = (Player) s;
                            SettingsManager config = ArenaHandler.getINSTANCE().getTemplate(player.getWorld().getName(), true);
                            boolean current = config.getProperty(ArenaConfig.LOAD_AT_START_UP);
                            config.setProperty(ArenaConfig.LOAD_AT_START_UP, current = !current);
                            config.save();
                            player.sendMessage(ChatColor.GRAY + "Creating games from this template at server start-up is now set to: " + ChatColor.AQUA + current);
                        }))
                .withSubNode(setMinPlayers
                        .withPermAdditions(s -> SetupManager.getINSTANCE().isInSetup(s))
                        .withDisplayName(s -> "&e" + setMinPlayers.getName() + " ")
                        .withDescription(s -> "&e[number]")
                        .withDisplayHover(s -> "&fHow many players are required to start a game?\n " +
                                "\n&eCurrent value: &b" + getCurrentProperty(ArenaConfig.MIN_PLAYERS, s) +
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
                        .withDisplayName(s -> "&7" + setMaxPlayers.getName() + " ")
                        .withDescription(s -> "&7[number]")
                        .withDisplayHover(s -> "&fHow many players are allowed in a game?\n " +
                                "\n&eCurrent value: &b" + getCurrentProperty(ArenaConfig.MAX_PLAYERS, s) +
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

                .withSubNode(addWaitingSpawn
                        .withPermAdditions((s) -> SetupManager.getINSTANCE().isInSetup(s))
                        .withDisplayName(s -> "&e" + addWaitingSpawn.getName() + " ")
                        .withDescription(s -> "&e[at your location]")
                        .withDisplayHover((s) -> "&fAdd a waiting spawn at your current location.\n " +
                                "\n&eCurrently added: &b" + getCurrentProperty(ArenaConfig.WAITING_LOBBY_LOCATIONS, s).size())
                        .withExecutor((s, args) -> {
                            Player player = (Player) s;
                            SettingsManager config = ArenaHandler.getINSTANCE().getTemplate(player.getWorld().getName(), true);
                            List<Location> waitingLocations = new ArrayList<>(config.getProperty(ArenaConfig.WAITING_LOBBY_LOCATIONS));
                            waitingLocations.add(player.getLocation());
                            config.setProperty(ArenaConfig.WAITING_LOBBY_LOCATIONS, waitingLocations);
                            config.save();
                            s.sendMessage(ChatColor.GRAY + "Waiting spawn location added!");
                        }))
                .withSubNode(clearWaitingSpawns
                        .withPermAdditions((s) -> SetupManager.getINSTANCE().isInSetup(s))
                        .withDisplayName(s -> "&7" + clearWaitingSpawns.getName() + " ")
                        .withDisplayHover((s) -> "&fClear waiting spawn locations from config.\n " +
                                "\n&eCurrently set: &b" + getCurrentProperty(ArenaConfig.WAITING_LOBBY_LOCATIONS, s).size())
                        .withExecutor((s, args) -> {
                            Player player = (Player) s;
                            SettingsManager config = ArenaHandler.getINSTANCE().getTemplate(player.getWorld().getName(), true);
                            config.setProperty(ArenaConfig.WAITING_LOBBY_LOCATIONS, Collections.emptyList());
                            config.save();
                            s.sendMessage(ChatColor.GRAY + "Waiting spawn locations removed!");
                        }))
                .withSubNode(addSpectatorSpawn
                        .withPermAdditions((s) -> SetupManager.getINSTANCE().isInSetup(s))
                        .withDisplayName(s -> "&e" + addSpectatorSpawn.getName() + " ")
                        .withDescription(s -> "&e[at your location]")
                        .withDisplayHover((s) -> "&fAdd a spectator spawn at your current location.\n " +
                                "\n&eCurrently added: &b" + getCurrentProperty(ArenaConfig.SPECTATE_LOCATIONS, s).size())
                        .withExecutor((s, args) -> {
                            Player player = (Player) s;
                            SettingsManager config = ArenaHandler.getINSTANCE().getTemplate(player.getWorld().getName(), true);
                            List<Location> locations = new ArrayList<>(config.getProperty(ArenaConfig.SPECTATE_LOCATIONS));
                            locations.add(player.getLocation());
                            config.setProperty(ArenaConfig.SPECTATE_LOCATIONS, locations);
                            config.save();
                            s.sendMessage(ChatColor.GRAY + "Spectator spawn location saved!");
                        }))
                .withSubNode(clearSpectatorSpawns
                        .withPermAdditions((s) -> SetupManager.getINSTANCE().isInSetup(s))
                        .withDisplayName(s -> "&7" + clearSpectatorSpawns.getName()+" ")
                        .withDisplayHover((s) -> "&fClear spectator spawn locations from config.\n " +
                                "\n&eCurrently set: &b" + getCurrentProperty(ArenaConfig.SPECTATE_LOCATIONS, s).size())
                        .withExecutor((s, args) -> {
                            Player player = (Player) s;
                            SettingsManager config = ArenaHandler.getINSTANCE().getTemplate(player.getWorld().getName(), true);
                            config.setProperty(ArenaConfig.SPECTATE_LOCATIONS, Collections.emptyList());
                            config.save();
                            s.sendMessage(ChatColor.GRAY + "Spectator spawn locations removed!");
                        }))


                .withSubNode(saveChanges
                        .withAliases(new String[]{"done", "finish", "close", "save"})
                        .withPermAdditions((s) -> SetupManager.getINSTANCE().isInSetup(s))
                        .withDisplayName(s -> "&e" + saveChanges.getName())
                        .withDisplayHover((s) -> "&fClose setup session ans save changes.")
                        .withExecutor((s, args) -> {
                            SetupSession setupSession = SetupManager.getINSTANCE().getSession((Player) s);
                            SetupManager.getINSTANCE().removeSession(setupSession);
                            //noinspection ConstantConditions
                            s.sendMessage(ChatColor.GRAY + "Changes saved for " + ChatColor.GREEN + setupSession.getWorldName() + ChatColor.GRAY + ".");
                        }))

        ;
    }

    private static <T> T getCurrentProperty(Property<T> property, CommandSender player) {
        if (!(player instanceof Player)) return property.getDefaultValue();
        SettingsManager config = ArenaHandler.getINSTANCE().getTemplate(((Player) player).getWorld().getName(), true);
        return config.getProperty(property);
    }
}
