package dev.andrei1058.game.arena.gametask.primeshields;

import com.andrei1058.hologramapi.Hologram;
import com.andrei1058.hologramapi.HologramPage;
import com.andrei1058.hologramapi.content.LineTextContent;
import dev.andrei1058.game.SteveSus;
import dev.andrei1058.game.api.arena.Arena;
import dev.andrei1058.game.api.arena.task.GameTask;
import dev.andrei1058.game.api.arena.task.TaskProvider;
import dev.andrei1058.game.api.arena.task.TaskType;
import dev.andrei1058.game.api.glow.GlowColor;
import dev.andrei1058.game.api.glow.GlowingBox;
import dev.andrei1058.game.api.server.multiarena.InventoryBackup;
import dev.andrei1058.game.api.setup.SetupListener;
import dev.andrei1058.game.api.setup.SetupSession;
import dev.andrei1058.game.api.setup.util.SaveTaskItem;
import dev.andrei1058.game.api.setup.util.SelectTargetBlock;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.config.properties.OrphanLocationProperty;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public class PrimeShieldsTaskProvider extends TaskProvider {

    private static PrimeShieldsTaskProvider instance;

    public static PrimeShieldsTaskProvider getInstance() {
        return instance == null ? instance = new PrimeShieldsTaskProvider() : instance;
    }

    @Override
    public String getDefaultDisplayName() {
        return "&8Prime Shields";
    }

    @Override
    public String getDefaultDescription() {
        return "Click the red hexagons.";
    }

    @Override
    public String getIdentifier() {
        return "prime_shields";
    }

    @Override
    public Plugin getProvider() {
        return SteveSus.getInstance();
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.SHORT;
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
        player.sendMessage(ChatColor.GRAY + "Set the block players will interact with to open the GUI.");
        // disable commands usage
        setupSession.setAllowCommands(false);
        // Block handler
        SelectTargetBlock selectTargetBlock = new SelectTargetBlock("&d&lSet glowing on target block", "&e&lRemove glowing from set block");
        selectTargetBlock.giveItems(player);
        // Save logic
        SaveTaskItem saveTaskItem = new SaveTaskItem(this, player1 -> {
            // enable back command usage
            setupSession.setAllowCommands(true);
            // restore inventory
            player1.getInventory().clear();
            inventoryBackup.restore(player1);
            // remove current listener to prevent memory leaks
            SteveSus.newChain().delay(2).sync(() -> setupSession.removeSetupListener("primeShields-" + localName)).execute();
            // save data
            if (selectTargetBlock.getSetBlock() == null) {
                player1.sendMessage(ChatColor.RED + "Block not set! Aborting...");
            } else {
                SteveSus.newChain().delay(2).sync(() -> {
                    OrphanLocationProperty exporter = new OrphanLocationProperty();
                    JsonObject config = new JsonObject();
                    config.addProperty("loc", exporter.toExportValue(selectTargetBlock.getSetBlock()).toString());
                    ArenaManager.getINSTANCE().saveTaskData(getInstance(), setupSession, localName, config.getAsJsonObject());
                }).execute();
            }
            return null;
        });
        saveTaskItem.giveItem(player);

        setupSession.addSetupListener("primeShields-" + localName, new SetupListener() {
            @Override
            public void onPlayerInteract(SetupSession setupSession, PlayerInteractEvent event) {
                if (selectTargetBlock.onItemInteract(event.getItem(), event.getPlayer()) || saveTaskItem.onItemInteract(event.getItem(), event.getPlayer())) {
                    event.setCancelled(true);
                }
            }
        });
    }

    @Override
    public void onSetupLoad(SetupSession setupSession, String localName, JsonObject configData) {
        if (!validateElements(configData, "loc")) return;
        JsonElement loc = configData.get("loc");
        if (loc.isJsonNull()) return;
        Location location = new OrphanLocationProperty().convert(loc.getAsString(), null);
        if (location == null) return;
        location.setWorld(setupSession.getPlayer().getWorld());

        SteveSus.newChain().delay(20).sync(() -> {
            GlowingBox glowingBox = new GlowingBox(location.clone().add(0.5, 0, 0.5), 2, GlowColor.GREEN);
            glowingBox.startGlowing(setupSession.getPlayer());

            Hologram hologram = new Hologram(location, 2);
            HologramPage page = hologram.getPage(0);
            assert page != null;
            page.setLineContent(0, new LineTextContent(s -> ChatColor.translateAlternateColorCodes('&', getDefaultDisplayName())));
            page.setLineContent(1, new LineTextContent(s -> localName));

            // cache objects so they can be removed if player decides to remove this configuration
            setupSession.cacheValue(getIdentifier() + "-" + localName + "-holo", hologram);
            setupSession.cacheValue(getIdentifier() + "-" + localName + "-glowing", glowingBox);
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
        if (!validateElements(configuration, "loc")) return null;
        JsonElement loc = configuration.get("loc");
        if (loc.isJsonNull()) return null;
        Location location = new OrphanLocationProperty().convert(loc.getAsString(), null);
        if (location == null) return null;
        location.setWorld(arena.getWorld());
        return new PrimeShieldsTask(localName, location, arena);
    }
}
