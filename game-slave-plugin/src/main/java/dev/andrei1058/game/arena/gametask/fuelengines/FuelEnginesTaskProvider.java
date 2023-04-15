package dev.andrei1058.game.arena.gametask.fuelengines;

import dev.andrei1058.game.SteveSus;
import dev.andrei1058.game.api.arena.Arena;
import dev.andrei1058.game.api.arena.task.GameTask;
import dev.andrei1058.game.api.arena.task.TaskProvider;
import dev.andrei1058.game.api.arena.task.TaskType;
import dev.andrei1058.game.api.server.multiarena.InventoryBackup;
import dev.andrei1058.game.api.setup.SetupListener;
import dev.andrei1058.game.api.setup.SetupSession;
import dev.andrei1058.game.api.setup.util.SaveTaskItem;
import dev.andrei1058.game.api.setup.util.SelectTargetBlocks;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.common.gui.ItemUtil;
import dev.andrei1058.game.config.properties.OrphanLocationProperty;
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

import java.util.Arrays;
import java.util.LinkedList;

public class FuelEnginesTaskProvider extends TaskProvider {

    private static FuelEnginesTaskProvider instance;

    public static FuelEnginesTaskProvider getInstance() {
        if (instance == null) {
            instance = new FuelEnginesTaskProvider();
        }
        return instance;
    }

    @Override
    public String getDefaultDisplayName() {
        return "&3Fuel Engines";
    }

    @Override
    public String getDefaultDescription() {
        return "Fuel engines by using the gas can.";
    }

    @Override
    public String getIdentifier() {
        return "fuel_engines";
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
        return false;
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
        player.sendMessage(ChatColor.GRAY + "Set the block players will interact with to open the GUI. Fill canister (optional): If you set a reactor point before setting a storage point this part will be skipped for the current stage.");
        player.sendMessage(ChatColor.BLUE + "Slot: 6-7");
        player.sendMessage(ChatColor.GRAY + "Set the block players will interact with to open the GUI. Fill reactor (optional).");
        player.sendMessage(ChatColor.BLUE + "Slot: 9");
        player.sendMessage(ChatColor.GRAY + "Set amount of stages per player.");
        // disable commands usage
        setupSession.setAllowCommands(false);
        // Block handler
        SelectTargetBlocks storageFillCanister = new SelectTargetBlocks("&d&lSet storage point (fill canister)", "&e&lRemove latest storage point");
        SelectTargetBlocks fillReactor = new SelectTargetBlocks("&d&lSet reactor location", "&e&lRemove latest reactor location");
        storageFillCanister.setAddItemSlot(2);
        storageFillCanister.setRemoveItemSlot(3);
        storageFillCanister.giveItems(player);
        storageFillCanister.setAddListener(player12 -> {
            if (storageFillCanister.getSetBlocks().size() - 2 == fillReactor.getSetBlocks().size()) {
                // add null in case is skipped
                fillReactor.getSetBlocks().add(null);
                fillReactor.getGlowingBox().add(null);
            }
            return null;
        });
        storageFillCanister.setRemoveListener(player13 -> {
            if (storageFillCanister.getSetBlocks().size() < fillReactor.getSetBlocks().size()) {
                // un-skip equivalent fill reactor location
                if (fillReactor.getSetBlocks().get(storageFillCanister.getSetBlocks().size()) == null) {
                    fillReactor.getSetBlocks().remove(fillReactor.getSetBlocks().size() - 1);
                    fillReactor.getGlowingBox().remove(fillReactor.getGlowingBox().size() - 1);
                    player13.sendMessage(ChatColor.GRAY + "Un-skipped equivalent fill reactor location.");
                }
            }
            return null;
        });
        fillReactor.setAddItemSlot(5);
        fillReactor.setRemoveItemSlot(6);
        fillReactor.giveItems(player);
        fillReactor.setAddListener(player14 -> {
            if (fillReactor.getSetBlocks().size() > storageFillCanister.getSetBlocks().size()) {
                storageFillCanister.getSetBlocks().add(null);
                storageFillCanister.getGlowingBox().add(null);
                player14.sendMessage(ChatColor.GRAY + "Skipped equivalent fill canister location.");
            }
            return null;
        });
        fillReactor.setRemoveListener(player15 -> {
            if (fillReactor.getSetBlocks().size() < storageFillCanister.getSetBlocks().size()) {
                // un-skip equivalent fill canister location
                if (storageFillCanister.getSetBlocks().get(fillReactor.getSetBlocks().size()) == null) {
                    storageFillCanister.getSetBlocks().remove(storageFillCanister.getSetBlocks().size() - 1);
                    storageFillCanister.getGlowingBox().remove(storageFillCanister.getGlowingBox().size() - 1);
                    player15.sendMessage(ChatColor.GRAY + "Un-skipped equivalent fill canister location.");
                }
            }
            return null;
        });

        ItemStack stagesItem = ItemUtil.createItem(ItemUtil.getMaterial("CLOCK", "CLOCK"), (byte) 0, 1, true, Arrays.asList("selectT" + getIdentifier(), "stages"), "&a&lSet stages per player", null);
        player.getInventory().setItem(8, stagesItem);
        // Temp memory
        final int[] stages = {2};
        // Save logic
        SaveTaskItem saveTaskItem = new SaveTaskItem(this, player1 -> {
            if (storageFillCanister.getSetBlocks().isEmpty() && fillReactor.getSetBlocks().isEmpty()) {
                player1.sendMessage(ChatColor.RED + "Aborting...");
                return null;
            }
            // enable back command usage
            setupSession.setAllowCommands(true);
            // restore inventory
            player1.getInventory().clear();
            inventoryBackup.restore(player1);
            // remove current listener to prevent memory leaks
            SteveSus.newChain().delay(2).sync(() -> setupSession.removeSetupListener("fullEngines-" + localName)).execute();
            // save data
            SteveSus.newChain().delay(2).sync(() -> {
                OrphanLocationProperty exporter = new OrphanLocationProperty();
                JsonObject config = new JsonObject();
                config.addProperty("stages", stages[0]);
                JsonArray list = new JsonArray();
                for (int i = 0; i < storageFillCanister.getSetBlocks().size(); i++) {
                    JsonObject boundle = new JsonObject();
                    Location storage = storageFillCanister.getSetBlocks().size() > i ? storageFillCanister.getSetBlocks().get(i) : null;
                    if (storage != null) {
                        boundle.addProperty("storage", exporter.toExportValue(storage).toString());
                    }
                    Location drop = fillReactor.getSetBlocks().size() > i ? fillReactor.getSetBlocks().get(i) : null;
                    if (drop != null) {
                        boundle.addProperty("engine", exporter.toExportValue(drop).toString());
                    }
                    if (storage != null || drop != null) {
                        list.add(boundle);
                    }
                }
                if (list.size() == 0) {
                    player1.sendMessage(ChatColor.RED + "Aborting...");
                    return;
                }
                config.add("candidates", list);
                ArenaManager.getINSTANCE().saveTaskData(getInstance(), setupSession, localName, config.getAsJsonObject());
            }).execute();
            return null;
        });
        saveTaskItem.giveItem(player);

        setupSession.addSetupListener("fullEngines-" + localName, new SetupListener() {
            @Override
            public void onPlayerInteract(SetupSession setupSession, PlayerInteractEvent event) {
                if (event.getItem() == null) return;
                if (storageFillCanister.onItemInteract(event.getItem(), event.getPlayer()) || fillReactor.onItemInteract(event.getItem(), event.getPlayer())
                || saveTaskItem.onItemInteract(event.getItem(), event.getPlayer())){
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
                }
            }
        });
    }

    @Override
    public void onSetupLoad(SetupSession setupSession, String localName, JsonObject configData) {

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
        if (!configuration.has("candidates")) return null;
        if (!configuration.get("candidates").isJsonArray()) {
            return null;
        }
        JsonArray array = configuration.get("candidates").getAsJsonArray();
        LinkedList<FuelStage> taskCandidates = new LinkedList<>();
        for (JsonElement element : array) {
            JsonObject candidate = element.getAsJsonObject();
            if (candidate.isJsonNull()) continue;

            OrphanLocationProperty convertor = new OrphanLocationProperty();
            Location storage = null;
            Location engine = null;

            if (candidate.has("storage")) {
                JsonElement storageElement = candidate.get("storage");
                if (storageElement.isJsonNull()) continue;
                storage = convertor.convert(storageElement.getAsString(), null);
                if (storage != null) {
                    storage.setWorld(arena.getWorld());
                }
            }
            if (candidate.has("engine")) {
                JsonElement engineElement = candidate.get("engine");
                engine = convertor.convert(engineElement.getAsString(), null);
                if (engine != null) {
                    engine.setWorld(arena.getWorld());
                }

            }
            if (storage != null || engine != null) {
                FuelStage stage = new FuelStage(storage, engine);
                taskCandidates.add(stage);
            }
        }
        if (taskCandidates.isEmpty()) return null;
        return new FuelEnginesTask(localName, taskCandidates, arena);
    }
}
