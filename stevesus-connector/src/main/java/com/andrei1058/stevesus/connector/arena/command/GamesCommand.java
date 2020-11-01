package com.andrei1058.stevesus.connector.arena.command;

import com.andrei1058.spigot.commandlib.ICommandNode;
import com.andrei1058.spigot.commandlib.fast.FastRootCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubCommand;
import com.andrei1058.stevesus.common.api.arena.DisplayableArena;
import com.andrei1058.stevesus.common.api.server.CommonPermission;
import com.andrei1058.stevesus.connector.SteveSusConnector;
import com.andrei1058.stevesus.connector.arena.ArenaManager;
import com.andrei1058.stevesus.connector.language.LanguageManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class GamesCommand {

    private static final int ARENAS_PER_PAGE = 10;

    private GamesCommand() {
    }

    public static void register(FastRootCommand root) {
        FastSubCommand games = new FastSubCommand("games");
        root
                .withSubNode(games
                        .withClickAction(ClickEvent.Action.RUN_COMMAND)
                        .withDescription((s) -> "&8- &eShow game instances.")
                        .withDisplayHover((s) -> "&fActive games: &a" + ArenaManager.getInstance().getArenas().size())
                        .withPermissions(new String[]{CommonPermission.ALL.get()})
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
                            List<DisplayableArena> arenas = new ArrayList<>(ArenaManager.getInstance().getArenas());

                            sender.sendMessage(color(" \n&1|| &3" + SteveSusConnector.getInstance().getName() + "&7 Instantiated games: \n "));

                            if (arenas.isEmpty()) {
                                sender.sendMessage(ChatColor.RED + "No arenas to display.");
                                return;
                            }

                            int start = (page - 1) * ARENAS_PER_PAGE;
                            if (start >= arenas.size()){
                                page = 0;
                                start = (page - 1) * ARENAS_PER_PAGE;
                            }
                            int limit = Math.min(arenas.size(), start + ARENAS_PER_PAGE);

                            arenas.subList(start, limit).forEach(arena -> {
                                String gameState = LanguageManager.getINSTANCE().getMsg(sender, arena.getGameState().getTranslatePath());
                                TextComponent component = new TextComponent(
                                        color(
                                                "Tag: &e" + arena.getTag() +
                                                        " &fT: &e" + arena.getTemplateWorld() +
                                                        " &fP: &e" + (arena.getCurrentPlayers() + arena.getCurrentSpectators()) +
                                                        " &fS: " + gameState
                                        ));

                                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{
                                        tc("&fGame ID: &e" + arena.getTag()),
                                        tc("&fTemplate: &e" + arena.getTemplateWorld()),
                                        tc("&fLocal: &e" + arena.isLocal()),
                                        tc("&fState: " + gameState),
                                        tc("&fPlayers: &e" + arena.getCurrentPlayers()),
                                        tc("&fSpectators: &e" + arena.getCurrentSpectators()),
                                        tc("&fSpectate enabled: &e" + arena.getSpectatePermission())
                                }));
                                sender.spigot().sendMessage(component);
                            });

                            sender.sendMessage(" ");

                            if (arenas.size() > ARENAS_PER_PAGE * page) {
                                sender.sendMessage(ChatColor.GRAY + "Type " + ChatColor.GREEN + ICommandNode.getClickCommand(games) + " " + ++page + ChatColor.GREEN + " for next page.");
                            }
                        }));
    }

    private static String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    private static TextComponent tc(String msg) {
        return new TextComponent(ChatColor.translateAlternateColorCodes('&', msg + "\n"));
    }
}
