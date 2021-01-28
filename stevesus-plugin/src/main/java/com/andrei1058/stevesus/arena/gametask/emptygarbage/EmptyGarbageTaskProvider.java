package com.andrei1058.stevesus.arena.gametask.emptygarbage;

import com.andrei1058.hologramapi.Hologram;
import com.andrei1058.hologramapi.HologramPage;
import com.andrei1058.hologramapi.content.LineTextContent;
import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.task.GameTask;
import com.andrei1058.stevesus.api.arena.task.TaskProvider;
import com.andrei1058.stevesus.api.arena.task.TaskType;
import com.andrei1058.stevesus.api.glow.GlowColor;
import com.andrei1058.stevesus.api.glow.GlowingBox;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.api.server.multiarena.InventoryBackup;
import com.andrei1058.stevesus.api.setup.SetupListener;
import com.andrei1058.stevesus.api.setup.SetupSession;
import com.andrei1058.stevesus.api.setup.util.SaveTaskItem;
import com.andrei1058.stevesus.api.setup.util.SelectTargetBlocks;
import com.andrei1058.stevesus.arena.ArenaManager;
import com.andrei1058.stevesus.common.gui.ItemUtil;
import com.andrei1058.stevesus.config.properties.OrphanLocationProperty;
import com.andrei1058.stevesus.language.LanguageManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EmptyGarbageTaskProvider extends TaskProvider {

    private static EmptyGarbageTaskProvider instance;
    public static String NEXT_PANEL;
    public static String LEVER_NAME;
    public static String LEVER_LORE;
    public static String GARBAGE_GUI_NAME;
    public static String GARBAGE_ITEM_NAME;
    public static String GARBAGE_ITEM_LORE;

    private EmptyGarbageTaskProvider() {
        NEXT_PANEL = Message.GAME_TASK_PATH_.toString() + getIdentifier() + "-panel-next";
        LEVER_NAME = Message.GAME_TASK_PATH_.toString() + getIdentifier() + "-lever-item-name";
        LEVER_LORE = Message.GAME_TASK_PATH_.toString() + getIdentifier() + "-lever-item-lore";
        GARBAGE_GUI_NAME = Message.GAME_TASK_PATH_.toString() + getIdentifier() + "-garbage-gui-name";
        GARBAGE_ITEM_NAME = Message.GAME_TASK_PATH_.toString() + getIdentifier() + "-garbage-item-name";
        GARBAGE_ITEM_LORE = Message.GAME_TASK_PATH_.toString() + getIdentifier() + "-garbage-item-lore";
        LanguageManager.getINSTANCE().getDefaultLocale().addDefault(NEXT_PANEL, "&7Next garbage to be emptied is in: {room}.");
        LanguageManager.getINSTANCE().getDefaultLocale().addDefault(LEVER_NAME, "&6&lLEVER");
        LanguageManager.getINSTANCE().getDefaultLocale().addDefault(LEVER_LORE, Arrays.asList("&fClick to", "&fempty garbage!"));
        LanguageManager.getINSTANCE().getDefaultLocale().addDefault(GARBAGE_ITEM_NAME, "&8Garbage");
        LanguageManager.getINSTANCE().getDefaultLocale().addDefault(GARBAGE_GUI_NAME, "&0Empty Garbage");
        LanguageManager.getINSTANCE().getDefaultLocale().addDefault(GARBAGE_ITEM_LORE, "&7Click the lever.");
    }


    public static EmptyGarbageTaskProvider getInstance() {
        if (instance == null) {
            instance = new EmptyGarbageTaskProvider();
        }
        return instance;
    }


    @Override
    public String getDefaultDisplayName() {
        return "&6Empty Garbage";
    }

    @Override
    public String getDefaultDescription() {
        return "&fClick the lever many items in the GUI.";
    }

    @Override
    public String getIdentifier() {
        return "empty_garbage";
    }

    @Override
    public Plugin getProvider() {
        return SteveSus.getInstance();
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.LONG;
    }

    @Override
    public boolean isVisual() {
        return true;
    }

    @Override
    public boolean canSetup(Player player, SetupSession setupSession) {
        return true;
    }

    @Override
    public void onSetupRequest(Player player, SetupSession setupSession, String localName) {
        // create inventory backup to be restored later
        InventoryBackup inventoryBackup = new InventoryBackup(player);
        // instructions
        player.sendMessage(ChatColor.BLUE + "Slot: 3-4");
        player.sendMessage(ChatColor.GRAY + "Set the block players will interact with to open the GUI.");
        player.sendMessage(ChatColor.BLUE + "Slot: 5");
        player.sendMessage(ChatColor.GRAY + "Set the order of a recently set location if will be picked.");
        player.sendMessage(ChatColor.BLUE + "Slot: 6-7");
        player.sendMessage(ChatColor.GRAY + "Set garbage drop location for the latest point set (Optional). This will mark the task as visual.");
        player.sendMessage(ChatColor.BLUE + "Slot: 9");
        player.sendMessage(ChatColor.GRAY + "Set amount of stages per player.");
        // disable commands usage
        setupSession.setAllowCommands(false);
        // Block handler
        SelectTargetBlocks emptyGarbage = new SelectTargetBlocks("&d&lSet empty garbage point", "&e&lRemove latest empty garbage point");
        SelectTargetBlocks dropLocation = new SelectTargetBlocks("&d&lSet garbage drop location for latest point", "&e&lRemove latest garbage drop location");
        emptyGarbage.setAddItemSlot(2);
        emptyGarbage.setRemoveItemSlot(3);
        emptyGarbage.giveItems(player);
        emptyGarbage.setAddListener(player12 -> {
            if (emptyGarbage.getSetBlocks().size() - 2 == dropLocation.getSetBlocks().size()) {
                // add null in case is skipped
                dropLocation.getSetBlocks().add(null);
                dropLocation.getGlowingBox().add(null);
            }
            return null;
        });
        emptyGarbage.setRemoveListener(player13 -> {
            if (emptyGarbage.getSetBlocks().size() < dropLocation.getSetBlocks().size()) {
                // un-skip equivalent fill reactor location
                if (dropLocation.getSetBlocks().get(emptyGarbage.getSetBlocks().size()) == null) {
                    dropLocation.getSetBlocks().remove(dropLocation.getSetBlocks().size() - 1);
                    dropLocation.getGlowingBox().remove(dropLocation.getGlowingBox().size() - 1);
                    player13.sendMessage(ChatColor.GRAY + "Un-skipped equivalent drop location.");
                }
            }
            return null;
        });
        dropLocation.setAddItemSlot(5);
        dropLocation.setRemoveItemSlot(6);
        dropLocation.giveItems(player);

        ItemStack stagesItem = ItemUtil.createItem(ItemUtil.getMaterial("CLOCK", "CLOCK"), (byte) 0, 1, true, Arrays.asList("selectT" + getIdentifier(), "stages"), "&a&lSet stages per player", null);
        ItemStack order = ItemUtil.createItem(ItemUtil.getMaterial("GOLD_INGOT", "GOLD_INGOT"), (byte) 0, 1, true, Arrays.asList("selectT" + getIdentifier(), "order"), "&a&lSet current location order when assigned", null);
        player.getInventory().setItem(8, stagesItem);
        player.getInventory().setItem(4, order);
        // Temp memory
        final int[] stages = {2};
        OrderPriority[] orders = new OrderPriority[10];
        // Save logic
        SaveTaskItem saveTaskItem = new SaveTaskItem(this, player1 -> {
            if (stages[0] < emptyGarbage.getSetBlocks().size() && !emptyGarbage.getSetBlocks().isEmpty()) {
                player1.sendMessage(ChatColor.RED + "Could not save: garbage points are less than stages per player!");
                return null;
            }
            // enable back command usage
            setupSession.setAllowCommands(true);
            // restore inventory
            player1.getInventory().clear();
            inventoryBackup.restore(player1);
            // remove current listener to prevent memory leaks
            SteveSus.newChain().delay(2).sync(() -> setupSession.removeSetupListener("emptyGarbage-" + localName)).execute();
            // save data
            if (emptyGarbage.getSetBlocks().isEmpty()) {
                player1.sendMessage(ChatColor.RED + "No garbage points set! Aborting...");
            } else {
                SteveSus.newChain().delay(2).sync(() -> {
                    OrphanLocationProperty exporter = new OrphanLocationProperty();
                    JsonObject config = new JsonObject();
                    config.addProperty("stages", stages[0]);
                    JsonArray list = new JsonArray();
                    for (int i = 0; i < emptyGarbage.getSetBlocks().size(); i++) {
                        JsonObject boundle = new JsonObject();
                        boundle.addProperty("location", exporter.toExportValue(emptyGarbage.getSetBlocks().get(i)).toString());
                        OrderPriority order1 = orders.length > i ? (orders[i] == null ? OrderPriority.NONE : orders[i]) : OrderPriority.NONE;
                        boundle.addProperty("order", order1.toString());
                        Location drop = dropLocation.getSetBlocks().size() > i ? dropLocation.getSetBlocks().get(i) : null;
                        if (drop != null) {
                            boundle.addProperty("drop", exporter.toExportValue(drop).toString());
                        }
                        list.add(boundle);
                    }
                    config.add("list", list);
                    ArenaManager.getINSTANCE().saveTaskData(getInstance(), setupSession, localName, config.getAsJsonObject());
                    player.sendMessage(ChatColor.GREEN + localName + ChatColor.GRAY + " task saved!");
                }).execute();
            }
            return null;
        });
        saveTaskItem.giveItem(player);

        setupSession.addSetupListener("emptyGarbage-" + localName, new SetupListener() {
            @Override
            public void onPlayerInteract(SetupSession setupSession, PlayerInteractEvent event) {
                if (event.getItem() == null) return;
                if (emptyGarbage.onItemInteract(event.getItem(), event.getPlayer()) || dropLocation.onItemInteract(event.getItem(), event.getPlayer())
                        || saveTaskItem.onItemInteract(event.getItem(), event.getPlayer())) {
                    event.setCancelled(true);
                }
                if (event.getPlayer().hasCooldown(event.getItem().getType())) return;
                if (event.getItem() != null && event.getItem().equals(stagesItem)) {
                    event.setCancelled(true);
                    if (event.getAction().toString().contains("RIGHT")) {
                        stages[0]++;
                    } else {
                        if (stages[0] != 1) {
                            stages[0]--;
                        }
                    }
                    player.sendTitle(ChatColor.BLUE + "" + stages[0], ChatColor.YELLOW + "Stages", 0, 30, 0);
                } else if (event.getItem() != null && event.getItem().equals(order)) {
                    event.setCancelled(true);
                    if (emptyGarbage.getSetBlocks().isEmpty()) {
                        player.sendTitle("", ChatColor.YELLOW + "Set a location first", 0, 30, 0);
                        return;
                    }
                    int entry = emptyGarbage.getSetBlocks().size() - 1;
                    if (orders[entry] == null) {
                        orders[entry] = OrderPriority.NONE;
                    } else {
                        orders[entry] = orders[entry].next();
                    }
                    player.sendTitle(ChatColor.BLUE + "" + orders[entry].getDescription(), ChatColor.YELLOW + "Order", 0, 30, 0);
                }
            }
        });
    }

    @Override
    public void onSetupLoad(SetupSession setupSession, String localName, JsonObject configData) {
        if (configData.isJsonNull()) return;
        if (!configData.has("list")) return;
        if (!configData.has("stages")) return;
        JsonArray array = configData.get("list").getAsJsonArray();
        if (array.isJsonNull()) {
            return;
        }

        SteveSus.newChain().delay(20).sync(() -> {
            List<GlowingBox> glowingBoxes = new ArrayList<>();
            List<Hologram> holograms = new ArrayList<>();

            for (JsonElement element : array) {
                JsonObject obj = element.getAsJsonObject();
                if (obj.isJsonNull()) continue;
                Location location = null;
                if (obj.has("location")) {
                    JsonElement loc = obj.get("location");
                    location = new OrphanLocationProperty().convert(loc.getAsString(), null);
                    if (location != null) {
                        location.setWorld(setupSession.getPlayer().getWorld());
                    }
                }
                Location drop = null;
                if (obj.has("drop")) {
                    drop = new OrphanLocationProperty().convert(obj.get("drop").getAsString(), null);
                    if (drop != null) {
                        drop.setWorld(setupSession.getPlayer().getWorld());
                    }
                }
                if (location != null) {
                    GlowingBox glowingBox = new GlowingBox(location.clone().add(0.5, 0, 0.5), 2, GlowColor.GREEN);
                    glowingBoxes.add(glowingBox);
                    glowingBox.startGlowing(setupSession.getPlayer());

                    Hologram hologram = new Hologram(location, 2);
                    HologramPage page = hologram.getPage(0);
                    assert page != null;
                    page.setLineContent(0, new LineTextContent(s -> ChatColor.translateAlternateColorCodes('&', getDefaultDisplayName()) + " - &fGARBAGE POINT"));
                    page.setLineContent(1, new LineTextContent(s -> localName));
                    holograms.add(hologram);
                }
                if (drop != null) {
                    GlowingBox glowingBox = new GlowingBox(drop.clone().add(0.5, 0, 0.5), 2, GlowColor.GREEN);
                    glowingBoxes.add(glowingBox);
                    glowingBox.startGlowing(setupSession.getPlayer());

                    Hologram hologram = new Hologram(drop, 2);
                    HologramPage page = hologram.getPage(0);
                    assert page != null;
                    page.setLineContent(0, new LineTextContent(s -> ChatColor.translateAlternateColorCodes('&', getDefaultDisplayName()) + " - &fDROP POINT"));
                    page.setLineContent(1, new LineTextContent(s -> localName));
                    holograms.add(hologram);
                }
            }

            // cache objects so they can be removed if player decides to remove this configuration
            setupSession.cacheValue(getIdentifier() + "-" + localName + "-holo", holograms);
            setupSession.cacheValue(getIdentifier() + "-" + localName + "-glowing", glowingBoxes);
        }).execute();
    }

    @Override
    public void onSetupClose(SetupSession setupSession, String localName, JsonObject configData) {

    }

    @Override
    public void onRemove(SetupSession setupSession, String localName, JsonObject configData) {

    }

    @Override
    public @Nullable GameTask onGameInit(Arena arena, JsonObject configuration, String localName) {
        if (configuration.isJsonNull()) return null;
        if (!configuration.has("list")) return null;
        if (!configuration.has("stages")) return null;
        JsonArray array = configuration.get("list").getAsJsonArray();
        if (array.isJsonNull()) {
            return null;
        }
        List<WallLever> wallLevers = new ArrayList<>();
        for (JsonElement element : array) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.isJsonNull()) continue;
            if (!obj.has("location")) continue;
            JsonElement loc = obj.get("location");
            Location location = new OrphanLocationProperty().convert(loc.getAsString(), null);
            if (location == null) continue;
            location.setWorld(arena.getWorld());
            Location drop = null;
            if (obj.has("drop")) {
                drop = new OrphanLocationProperty().convert(obj.get("drop").getAsString(), null);
                if (drop != null) {
                    drop.setWorld(arena.getWorld());
                }
            }
            OrderPriority priority = OrderPriority.NONE;
            if (obj.has("order")) {
                try {
                    priority = OrderPriority.valueOf(obj.get("order").getAsString().toUpperCase());
                } catch (Exception ignored) {
                }
            }
            wallLevers.add(new WallLever(location, drop, priority));
        }
        int stages = configuration.get("stages").getAsInt();
        return new EmptyGarbageTask(localName, arena, wallLevers, stages);
    }
}
