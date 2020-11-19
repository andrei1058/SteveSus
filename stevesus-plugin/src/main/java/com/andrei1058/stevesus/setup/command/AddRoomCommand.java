package com.andrei1058.stevesus.setup.command;

import ch.jalu.configme.SettingsManager;
import com.andrei1058.spigot.commandlib.ICommandNode;
import com.andrei1058.spigot.commandlib.fast.FastSubCommand;
import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.api.setup.SetupListener;
import com.andrei1058.stevesus.api.setup.SetupSession;
import com.andrei1058.stevesus.arena.ArenaManager;
import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.gui.ItemUtil;
import com.andrei1058.stevesus.config.ArenaConfig;
import com.andrei1058.stevesus.config.properties.OrphanLocationProperty;
import com.andrei1058.stevesus.language.LanguageManager;
import com.andrei1058.stevesus.setup.SetupManager;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings("UnstableApiUsage")
public class AddRoomCommand extends FastSubCommand {

    public AddRoomCommand() {
        super("room");
        withDisplayName(s -> "&7" + getName() + " [name] [displayName]")
                .withClickAction(ClickEvent.Action.SUGGEST_COMMAND)
                .withDisplayHover(s -> "&eCreate a room with id and display name.")
                .withExecutor((s, args) -> {
                    SetupSession setupSession = SetupManager.getINSTANCE().getSession((Player) s);
                    assert setupSession != null;

                    if (args.length < 2) {
                        s.sendMessage(color("&cUsage: &/" + ICommandNode.getClickCommand(this) + " [name] [displayName]"));
                        return;
                    }

                    String identifier = args[0];

                    SettingsManager config = ArenaManager.getINSTANCE().getTemplate(setupSession.getWorldName(), true);
                    ArrayList<String> rooms = new ArrayList<>(config.getProperty(ArenaConfig.ROOMS));
                    if (rooms.stream().anyMatch(room -> room.startsWith(identifier + ";"))) {
                        s.sendMessage(color("&cRoom name already in use: &7" + identifier));
                        return;
                    }

                    String displayName;
                    StringBuilder nameBuilder = new StringBuilder();
                    Arrays.stream(Arrays.copyOfRange(args, 1, args.length)).forEach(arg -> nameBuilder.append(arg).append(" "));
                    displayName = nameBuilder.toString();
                    if (displayName.endsWith(" ")) {
                        displayName = displayName.substring(0, displayName.length() - 1);
                    }

                    setupSession.cacheValue("roomName", identifier);
                    setupSession.cacheValue("roomDisplayName", displayName);

                    // disable command usage and give region related items
                    setupSession.setAllowCommands(false);
                    setupSession.addSetupListener("roomSetupListener", new RoomCreationListener());

                    int taskId = Bukkit.getScheduler().runTaskTimer(SteveSus.getInstance(), () -> {
                        Object pos1 = setupSession.getCachedValue(POS1_TAG);
                        Object pos2 = setupSession.getCachedValue(POS2_TAG);
                        if (pos1 != null && pos2 != null) {
                            Location loc1 = (Location) pos1;
                            Location loc2 = (Location) pos2;
                            for (int x = loc1.getBlockX(); x <= loc2.getBlockX(); x++) {
                                for (int y = loc1.getBlockY(); y <= loc2.getBlockY(); y++) {
                                    for (int z = loc1.getBlockZ(); z <= loc2.getBlockZ(); z++) {
                                        loc1.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, x + 0.5, y + 0.5, z + 0.5, 1);
                                    }
                                }
                            }
                        }
                    }, 20L, 20L).getTaskId();
                    setupSession.cacheValue("roomTask", taskId);
                    s.sendMessage(color("&7Started room setup: &a" + displayName));
                    sendItems((Player) s);
                });
    }

    private static final String TAG_KEY = "roomsetup";
    private static final String SAVE_TAG = "roomsave";
    private static final String POS1_TAG = "roompos1";
    private static final String POS2_TAG = "roompos2";
    private static final String POS1_TAG_CFG = "roompos1cfg";
    private static final String POS2_TAG_CFG = "roompos2cfg";

    private static void sendItems(Player player) {
        player.getInventory().clear();
        player.getInventory().setHeldItemSlot(1);
        player.getInventory().setItem(0, ItemUtil.createItem("REDSTONE", (byte) 0, 1, true, Arrays.asList(TAG_KEY, SAVE_TAG), "&c&bSave Room", null));
        player.getInventory().setItem(3, ItemUtil.createItem("DIAMOND_BLOCK", (byte) 0, 1, true, Arrays.asList(TAG_KEY, POS1_TAG), "&b&lFirst Corner", null));
        player.getInventory().setItem(4, ItemUtil.createItem("EMERALD_BLOCK", (byte) 0, 1, true, Arrays.asList(TAG_KEY, POS2_TAG), "&2&lSecond Corner", null));
    }

    private static void handlePosition(SetupSession setupSession, String pos, Location location) {
        setupSession.removeCacheValue(pos);
        setupSession.cacheValue(pos.equals(POS1_TAG) ? POS1_TAG_CFG : POS2_TAG_CFG, location);

        Object pos1 = setupSession.getCachedValue(POS1_TAG_CFG);
        Object pos2 = setupSession.getCachedValue(POS2_TAG_CFG);
        if (pos1 != null && pos2 != null) {
            Location loc1 = (Location) pos1;
            Location loc2 = (Location) pos2;
            Location newPos1 = new Location(loc1.getWorld(), Math.min(loc1.getBlockX(), loc2.getBlockX()), Math.min(loc1.getBlockY(), loc2.getBlockY()), Math.min(loc1.getBlockZ(), loc2.getBlockZ()));
            Location newPos2 = new Location(loc1.getWorld(), Math.max(loc1.getBlockX(), loc2.getBlockX()), Math.max(loc1.getBlockY(), loc2.getBlockY()), Math.max(loc1.getBlockZ(), loc2.getBlockZ()));
            setupSession.getCachedValue(POS1_TAG);
            setupSession.getCachedValue(POS2_TAG);
            setupSession.cacheValue(POS1_TAG, newPos1);
            setupSession.cacheValue(POS2_TAG, newPos2);
        }
        setupSession.getPlayer().sendMessage(color("&7Position set!"));
        setupSession.getPlayer().setCooldown(setupSession.getPlayer().getInventory().getItemInMainHand().getType(), 20);
    }

    private static class RoomCreationListener implements SetupListener {

        @Override
        public void onPlayerDropItem(SetupSession setupSession, PlayerDropItemEvent event) {
            event.setCancelled(true);
        }

        @Override
        public void onPlayerPickupItem(SetupSession setupSession, EntityPickupItemEvent event) {
            event.setCancelled(true);
        }

        @Override
        public void onSetupPerClose(SetupSession setupSession) {
            Object taskId = setupSession.getCachedValue("roomTask");
            if (taskId != null) {
                Bukkit.getScheduler().cancelTask((Integer) taskId);
            }
        }

        @Override
        public void onPlayerInteract(SetupSession setupSession, PlayerInteractEvent event) {
            String tag = CommonManager.getINSTANCE().getItemSupport().getTag(event.getPlayer().getInventory().getItemInMainHand(), TAG_KEY);
            if (tag == null) return;
            if (SAVE_TAG.equals(tag)) {
                event.setCancelled(true);
                event.getPlayer().setCooldown(event.getPlayer().getInventory().getItemInMainHand().getType(), 20);
                Object taskId = setupSession.getCachedValue("roomTask");
                SteveSus.newChain().delay(1).sync(() -> setupSession.removeSetupListener("roomSetupListener"));
                if (taskId != null) {
                    Bukkit.getScheduler().cancelTask((Integer) taskId);
                }
                // save data
                String roomDisplayName = (String) setupSession.getCachedValue("roomDisplayName");
                String roomName = (String) setupSession.getCachedValue("roomName");
                Object pos1 = setupSession.getCachedValue(POS1_TAG_CFG);
                Object pos2 = setupSession.getCachedValue(POS2_TAG_CFG);
                if (pos1 == null || pos2 == null) {
                    setupSession.setAllowCommands(true);
                    setupSession.getPlayer().sendMessage(color("&7Could not save room: " + roomDisplayName + "&7."));
                    setupSession.getPlayer().sendMessage(color("&cYou didn't set both pos1 and pos2."));
                    return;
                }

                SettingsManager config = ArenaManager.getINSTANCE().getTemplate(setupSession.getWorldName(), true);
                ArrayList<String> rooms = new ArrayList<>(config.getProperty(ArenaConfig.ROOMS));
                OrphanLocationProperty exporter = new OrphanLocationProperty();
                rooms.add(roomName + ";" + exporter.toExportValue((Location) pos1).toString() + ";" + exporter.toExportValue((Location) pos2).toString());
                config.setProperty(ArenaConfig.ROOMS, rooms);
                config.save();
                LanguageManager.getINSTANCE().getDefaultLocale().addDefault(Message.GAME_ROOM_NAME_.toString() + roomName, roomDisplayName);
                // removed cached values
                setupSession.removeCacheValue("roomName");
                setupSession.removeCacheValue("roomDisplayName");
                setupSession.removeCacheValue("roomTask");
                setupSession.removeCacheValue("roompos1");
                setupSession.removeCacheValue("roompos2");
                setupSession.removeCacheValue(POS2_TAG_CFG);
                setupSession.removeCacheValue(POS1_TAG_CFG);
                setupSession.removeCacheValue("roomCalledTwice");
                setupSession.getPlayer().sendMessage(color("&7Room saved: " + roomDisplayName + "&7."));
                setupSession.getPlayer().getInventory().clear();
                setupSession.setAllowCommands(true);
            }
        }

        @Override
        public void onBlockPlace(SetupSession setupSession, BlockPlaceEvent event) {
            String tag = CommonManager.getINSTANCE().getItemSupport().getTag(event.getPlayer().getInventory().getItemInMainHand(), TAG_KEY);
            if (tag == null) return;
            event.setCancelled(true);
            if (tag.equals(POS1_TAG) || tag.equals(POS2_TAG)) {
                handlePosition(setupSession, tag, event.getBlock().getLocation());
            }
        }
    }

    private static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
