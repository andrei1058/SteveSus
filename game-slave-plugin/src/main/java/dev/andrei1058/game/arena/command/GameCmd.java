package dev.andrei1058.game.arena.command;

import com.andrei1058.spigot.commandlib.ICommandNode;
import com.andrei1058.spigot.commandlib.fast.FastRootCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubRootCommand;
import dev.andrei1058.game.SteveSus;
import dev.andrei1058.game.api.arena.Arena;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.common.api.server.CommonPermission;
import dev.andrei1058.game.language.LanguageManager;
import dev.andrei1058.game.setup.SetupManager;
import dev.andrei1058.game.worldmanager.WorldManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class GameCmd {

    private static final int ARENAS_PER_PAGE = 10;

    private GameCmd() {
    }

    public static void register(FastRootCommand root) {
        FastSubRootCommand game = new FastSubRootCommand("game");
        root.withSubNode(game
                .withClickAction(ClickEvent.Action.RUN_COMMAND)
                .withPermissions(new String[]{CommonPermission.ADMIN.get(), CommonPermission.ALL.get()})
                .withDescription((s) -> "&8- &eAdmin CMDs game related.\n ")
                .withDisplayHover((s) -> "&fList of admin commands related to Arenas.")
                .withPermAdditions(s -> s instanceof ConsoleCommandSender || (s instanceof Player && !SetupManager.getINSTANCE().isInSetup(s)))
                .withPriority(-0.1)
        );


        FastSubCommand list = new FastSubCommand("list");
        game.withSubNode(list
                .withClickAction(ClickEvent.Action.RUN_COMMAND)
                .withDescription((s) -> "&8- &eShow game instances.")
                .withDisplayHover((s) -> "&fActive games: &a" + ArenaManager.getINSTANCE().getArenas().size())
                .withPermissions(game.getPerms())
                .withExecutor((sender, args) -> {
                    int page = 1;
                    if (args.length >= 1) {
                        try {
                            page = Integer.parseInt(args[0]);
                            if (page < 0) {
                                page = 1;
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    List<Arena> arenas = new ArrayList<>(ArenaManager.getINSTANCE().getEnableQueue());
                    arenas.addAll(ArenaManager.getINSTANCE().getArenas());

                    if (arenas.isEmpty()) {
                        sender.sendMessage(ChatColor.RED + "No arenas to display.");
                        return;
                    }

                    int start = (page - 1) * ARENAS_PER_PAGE;
                    if (start >= arenas.size()) {
                        page = 1;
                        start = 0;
                    }
                    int limit = Math.min(arenas.size(), start + ARENAS_PER_PAGE);

                    sender.sendMessage(color(" \n&1|| &3" + SteveSus.getInstance().getName() + "&7 Instantiated games (" + page + "/" + (arenas.size() / ARENAS_PER_PAGE) + "): \n "));

                    arenas.subList(start, limit).forEach(arena -> {
                        String gameState = LanguageManager.getINSTANCE().getMsg(sender, arena.getGameState().getTranslatePath());
                        TextComponent component = new TextComponent(
                                color(
                                        "ID: &e" + arena.getGameId() +
                                                " &fT: &e" + arena.getTemplateWorld() +
                                                " &fP: &e" + (arena.getPlayers().size() + arena.getSpectators().size()) +
                                                " &fS: " + gameState
                                ));

                        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{
                                tc("&fGame ID: &e" + arena.getGameId()),
                                tc("&fTemplate: &e" + arena.getTemplateWorld()),
                                tc("&fWorld: &e" + (arena.getWorld() == null ? "Not loaded yet" : arena.getWorld().getName())),
                                tc("&fState: " + gameState),
                                tc("&fPlayers: &e" + arena.getPlayers().size()),
                                tc("&fSpectators: &e" + arena.getSpectators().size()),
                                tc("&fSpectate P: &e" + (arena.getSpectatePermission().isEmpty() ? "none" : arena.getSpectatePermission()))
                        }));
                        sender.spigot().sendMessage(component);
                    });

                    sender.sendMessage(" ");

                    if (arenas.size() > ARENAS_PER_PAGE * page) {
                        TextComponent msg = tc(ChatColor.GRAY + "Type or click " + ChatColor.GREEN + ICommandNode.getClickCommand(list) + " " + page + 1 + ChatColor.GRAY + " for next page.");
                        msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ICommandNode.getClickCommand(list) + " " + page + 1));
                        sender.spigot().sendMessage(msg);
                    }
                }));

        FastSubCommand create = new FastSubCommand("create");
        game.withSubNode(create
                .withDescription((s) -> "&8- &eCreate a new arena from template.")
                .withDisplayHover(s -> "&8- &eCreate a new arena from template.")
                .withPermissions(game.getPerms())
                .withExecutor((sender, args) -> {
                    if (args.length != 1) {
                        sender.sendMessage(color("&7Usage: &b" + ICommandNode.getClickCommand(create) + "<template>"));
                        return;
                    }
                    if (!ArenaManager.getINSTANCE().getTemplateFile(args[0]).exists()) {
                        sender.sendMessage(color("&7Template not found: &c" + args[0]));
                        sender.sendMessage(color("&7Available templates: &b" + ArenaManager.getINSTANCE().getTemplates()));
                        return;
                    }
                    if (!(ArenaManager.getINSTANCE().validateTemplate(args[0]) || WorldManager.getINSTANCE().getWorldAdapter().hasWorld(args[0]))) {
                        sender.sendMessage(color("&7Cannot start new arena from template: &c" + args[0] + "&8."));
                        sender.sendMessage(color("&7It does not have a valid configuration. Make sure to set all required parts!"));
                        return;
                    }
                    ArenaManager.getINSTANCE().startArenaFromTemplate(args[0]);
                    sender.sendMessage(color("&7Starting arena from template: &b" + args[0] + "&7..."));
                })
                .withTabSuggestions(s -> ArenaManager.getINSTANCE().getTemplates())
        );
    }

    private static String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    private static TextComponent tc(String msg) {
        return new TextComponent(ChatColor.translateAlternateColorCodes('&', msg + "\n"));
    }
}
