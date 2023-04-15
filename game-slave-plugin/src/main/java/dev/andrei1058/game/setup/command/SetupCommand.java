package dev.andrei1058.game.setup.command;

import com.andrei1058.spigot.commandlib.fast.FastSubCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubRootCommand;
import dev.andrei1058.game.SteveSus;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.common.api.server.CommonPermission;
import dev.andrei1058.game.setup.SetupManager;
import dev.andrei1058.game.worldmanager.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class SetupCommand extends FastSubRootCommand {

    public SetupCommand(String name) {
        super(name);
        withAliases(new String[]{"s"});
        withPermissions(new String[]{CommonPermission.ALL.get(), CommonPermission.ADMIN.get()})
                .withPermAdditions((s) -> !SetupManager.getINSTANCE().isInSetup(s) && (s instanceof ConsoleCommandSender || (((s) instanceof Player) && !ArenaManager.getINSTANCE().isInArena((Player) s))))
                .withHeaderContent("&1|| &3" + SteveSus.getInstance().getName() + " &7 - Setup Commands")
                .withHeaderHover("&7By " + Arrays.toString(SteveSus.getInstance().getDescription().getAuthors().toArray()) + "\n&av" + SteveSus.getInstance().getDescription().getVersion())
                .withDescription((s) -> "&8- &eSetup commands.").withDisplayHover((s) -> "&fCreate or edit an existing arena.");
        //todo add git version

        // create sub command
        withSubNode(new FastSubCommand("create").withDescription((s) -> " <arenaName> &eCreate a new arena.")
                .withDisplayHover((s) -> "&fCreate a new arena where &e<worldName> &fis the arena name and the world to be cloned.\n&fIf the world does not exist a void map will be created.")
                .withPermAdditions((s) -> !SetupManager.getINSTANCE().isInSetup(s) && (s instanceof Player))
                .withTabSuggestions((sender) -> WorldManager.getINSTANCE().getWorldAdapter().getWorlds().stream().filter(map -> !ArenaManager.getINSTANCE().getTemplateFile(map).exists()).collect(Collectors.toList()))
                .withExecutor((sender, args) -> {
                    if (args.length != 1) {
                        sender.sendMessage(ChatColor.GRAY + "Setup a new arena with the given name.");
                        List<String> existing = WorldManager.getINSTANCE().getWorldAdapter().getWorlds().stream().filter(map -> !ArenaManager.getINSTANCE().getTemplateFile(map).exists()).collect(Collectors.toList());
                        sender.sendMessage(ChatColor.GRAY + "Available worlds: " + ChatColor.GREEN + existing.toString());
                        return;
                    }
                    String world = args[0].toLowerCase();
                    if (SetupManager.getINSTANCE().isWorldInUse(world)) {
                        sender.sendMessage(ChatColor.RED + "This world is already used in another setup session!");
                        return;
                    }
                    if (WorldManager.getINSTANCE().getWorldAdapter().hasWorld(world)) {
                        if (ArenaManager.getINSTANCE().getTemplateFile(world).exists()) {
                            sender.sendMessage(ChatColor.GREEN + world + ChatColor.GRAY + " already exists! Use the edit command instead.");
                            return;
                        }
                        sender.sendMessage(ChatColor.GRAY + "Loading " + ChatColor.GREEN + world + ChatColor.GRAY + " from " + ChatColor.GREEN +
                                WorldManager.getINSTANCE().getWorldAdapter().getAdapterName() + ChatColor.GRAY + "'s world container.");
                    } else {
                        sender.sendMessage(ChatColor.GRAY + "Creating a void map for " + ChatColor.GREEN + world + ChatColor.GRAY + "...");
                    }
                    SetupManager.getINSTANCE().createSetupSession((Player) sender, world);
                }));

        // edit sub command
        withSubNode(new FastSubCommand("edit").withDescription((s) -> " <arenaName> &eEdit an existing arena.")
                .withPermAdditions((s) -> !SetupManager.getINSTANCE().isInSetup(s) && (s instanceof Player))
                .withDisplayHover((s) -> "&fEdit an existing arena.")
                .withTabSuggestions((s) -> WorldManager.getINSTANCE().getWorldAdapter().getWorlds().stream().filter(map -> ArenaManager.getINSTANCE().getTemplateFile(map).exists()).collect(Collectors.toList()))
                .withExecutor((sender, args) -> {
                    if (args.length != 1) {
                        sender.sendMessage(ChatColor.GRAY + "Edit an arena with the given name.");
                        List<String> existing = WorldManager.getINSTANCE().getWorldAdapter().getWorlds().stream().filter(map -> ArenaManager.getINSTANCE().getTemplateFile(map).exists()).collect(Collectors.toList());
                        sender.sendMessage(ChatColor.GRAY + "Available arenas: " + ChatColor.GREEN + existing.toString());
                        return;
                    }
                    String world = args[0].toLowerCase();
                    if (SetupManager.getINSTANCE().isWorldInUse(world)) {
                        sender.sendMessage(ChatColor.RED + "This world is already used in another setup session!");
                        return;
                    }
                    if (!WorldManager.getINSTANCE().getWorldAdapter().hasWorld(world)) {
                        sender.sendMessage(ChatColor.RED + world + ChatColor.GRAY + " world does not exist!");
                        return;
                    }
                    if (!ArenaManager.getINSTANCE().getTemplateFile(world).exists()) {
                        sender.sendMessage(ChatColor.GREEN + world + ChatColor.GRAY + " does not exist! Use the create command instead.");
                        return;
                    }
                    sender.sendMessage(ChatColor.GRAY + "Loading " + ChatColor.GREEN + world + ChatColor.GRAY + " from " + ChatColor.GREEN +
                            WorldManager.getINSTANCE().getWorldAdapter().getAdapterName() + ChatColor.GRAY + "'s world container.");
                    SetupManager.getINSTANCE().createSetupSession((Player) sender, world);
                }));

        // delete sub command
        withSubNode(new FastSubCommand("delete").withDescription((s) -> "&eDelete an arena.")
                .withPermAdditions((s) -> !SetupManager.getINSTANCE().isInSetup(s))
                .withDisplayHover((s) -> "&cDelete an arena.")
                .withExecutor((sender, args) -> {
                    if (args.length != 1) {
                        sender.sendMessage(ChatColor.GRAY + " <arenaName> &eDelete an existing arena with the given name.");
                        List<String> existing = WorldManager.getINSTANCE().getWorldAdapter().getWorlds();
                        for (String worldConfig : ArenaManager.getINSTANCE().getTemplates()) {
                            if (!existing.contains(worldConfig)) {
                                existing.add(worldConfig);
                            }
                        }
                        sender.sendMessage(ChatColor.GRAY + "Available arenas: " + ChatColor.RED + existing.toString());
                        return;
                    }
                    String world = args[0].toLowerCase();
                    if (SetupManager.getINSTANCE().isWorldInUse(world)) {
                        sender.sendMessage(ChatColor.RED + "Cannot delete because it is in use in a setup session!");
                        return;
                    }
                    if (Bukkit.getWorld(world) != null && !Bukkit.getWorld(world).getPlayers().isEmpty()) {
                        sender.sendMessage(ChatColor.RED + "Cannot delete because there are players on that world!");
                        return;
                    }

                    boolean deleted = false;
                    if (WorldManager.getINSTANCE().getWorldAdapter().hasWorld(world)) {
                        WorldManager.getINSTANCE().getWorldAdapter().deleteWorld(world);
                        sender.sendMessage(ChatColor.GREEN + world + ChatColor.GRAY + " world deleted from " + ChatColor.GREEN + WorldManager.getINSTANCE().getWorldAdapter().getAdapterName() + ChatColor.GRAY + "'s world container.");
                        deleted = true;
                    }
                    File template = ArenaManager.getINSTANCE().getTemplateFile(world);
                    if (template.exists()) {
                        if (template.delete()) {
                            sender.sendMessage(ChatColor.GREEN + template.getName() + ChatColor.GRAY + " deleted from " + ArenaManager.getINSTANCE().getTemplatesDirectory().getPath());
                        } else {
                            sender.sendMessage(ChatColor.GRAY + "Could not delete " + ChatColor.RED + template.getName() + ChatColor.GRAY + " from " + ArenaManager.getINSTANCE().getTemplatesDirectory().getPath());
                        }
                        deleted = true;
                    }
                    if (deleted) return;
                    sender.sendMessage(ChatColor.RED + world + ChatColor.GRAY + " does not exist!");
                }));

        //todo create clone command
    }
}
