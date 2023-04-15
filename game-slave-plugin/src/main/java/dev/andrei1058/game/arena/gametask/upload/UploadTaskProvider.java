package dev.andrei1058.game.arena.gametask.upload;

import dev.andrei1058.game.SteveSus;
import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.arena.task.GameTask;
import dev.andrei1058.game.api.arena.task.TaskProvider;
import dev.andrei1058.game.api.arena.task.TaskType;
import dev.andrei1058.game.api.locale.Message;
import dev.andrei1058.game.api.server.multiarena.InventoryBackup;
import dev.andrei1058.game.api.setup.SetupListener;
import dev.andrei1058.game.api.setup.SetupSession;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.common.CommonManager;
import dev.andrei1058.game.common.gui.ItemUtil;
import dev.andrei1058.game.config.properties.OrphanLocationProperty;
import dev.andrei1058.game.language.LanguageManager;
import dev.andrei1058.game.setup.command.AddCommand;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedList;

public class UploadTaskProvider extends TaskProvider {

    private static UploadTaskProvider instance;
    public static String DOWNLOAD_PANEL_HOLO;
    public static String UPLOAD_PANEL_HOLO;
    public static String UPLOAD_PANEL_NAME;
    public static String DOWNLOAD_PANEL_NAME;
    public static String UPLOAD_ROOM_MSG;

    public static UploadTaskProvider getInstance() {
        return instance == null ? instance = new UploadTaskProvider() : instance;
    }

    private UploadTaskProvider() {
        DOWNLOAD_PANEL_HOLO = Message.GAME_TASK_PATH_.toString() + getIdentifier() + "-download-panel-holo";
        UPLOAD_PANEL_HOLO = Message.GAME_TASK_PATH_.toString() + getIdentifier() + "-upload-panel-holo";
        UPLOAD_PANEL_NAME = Message.GAME_TASK_PATH_.toString() + getIdentifier() + "-upload-panel-name";
        DOWNLOAD_PANEL_NAME = Message.GAME_TASK_PATH_.toString() + getIdentifier() + "-download-panel-name";
        UPLOAD_ROOM_MSG = Message.GAME_TASK_PATH_.toString() + getIdentifier() + "-upload-room-name";
        LanguageManager.getINSTANCE().getDefaultLocale().addDefault(DOWNLOAD_PANEL_HOLO, "&2Download Data");
        LanguageManager.getINSTANCE().getDefaultLocale().addDefault(UPLOAD_PANEL_HOLO, "&2Upload Data");
        LanguageManager.getINSTANCE().getDefaultLocale().addDefault(UPLOAD_PANEL_NAME, "&8Uploading to Headquarters");
        LanguageManager.getINSTANCE().getDefaultLocale().addDefault(DOWNLOAD_PANEL_NAME, "&8Downloading to My Tablet");
        LanguageManager.getINSTANCE().getDefaultLocale().addDefault(UPLOAD_ROOM_MSG, "&7Go upload data in {room}&7.");
    }

    @Override
    public String getDefaultDisplayName() {
        return "&dUpload Data";
    }

    @Override
    public String getDefaultDescription() {
        return "&fDownload and Upload data.";
    }

    @Override
    public String getIdentifier() {
        return "upload_data";
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
        InventoryBackup inventoryBackup = new InventoryBackup(player);
        player.sendMessage(ChatColor.GRAY + "Place the download and upload panels and save changes.");
        player.sendMessage(ChatColor.GRAY + "Only a download and an upload panel will be assigned to players, randomly between placed panels.");
        setupSession.setAllowCommands(false);

        ItemStack save = ItemUtil.createItem("BOOK", (byte) 0, 1, true, Arrays.asList("uploadPanel", "save"), ChatColor.RED + "" + ChatColor.BOLD + "Save and close: " + ChatColor.RESET + getDefaultDisplayName(), null);
        player.getInventory().setItem(0, save);

        ItemStack download = ItemUtil.createItem("ITEM_FRAME", (byte) 0, 1, false, Arrays.asList("uploadPanel", "download"), ChatColor.YELLOW + "Place this where to spawn DOWNLOAD panel.", null);
        player.getInventory().setItem(6, download);

        ItemStack upload = ItemUtil.createItem("ITEM_FRAME", (byte) 0, 1, false, Arrays.asList("uploadPanel", "upload"), ChatColor.LIGHT_PURPLE + "Place this where to spawn UPLOAD panel.", null);
        player.getInventory().setItem(7, upload);

        ItemStack plus = ItemUtil.createItem(ItemUtil.getMaterial("CONCRETE", "MAGENTA_CONCRETE"), (byte) 2, 1, false, Arrays.asList("uploadPanel", "plus"), "&dIncrease task time", null);
        player.getInventory().setItem(2, plus);

        ItemStack minus = ItemUtil.createItem(ItemUtil.getMaterial("CONCRETE", "YELLOW_CONCRETE"), (byte) 4, 1, false, Arrays.asList("uploadPanel", "minus"), "&eDecrease task time", null);
        player.getInventory().setItem(3, minus);

        final LinkedList<ItemFrame> downloadFrame = new LinkedList<>();
        final LinkedList<ItemFrame> uploadFrame = new LinkedList<>();
        final int[] taskTime = new int[]{9};

        setupSession.addSetupListener("uploadTaskListener", new SetupListener() {
            @Override
            public void onPlayerInteract(SetupSession setupSession, PlayerInteractEvent event) {
                if (event.getItem() == null || event.getItem().getType() == Material.AIR) return;
                String tag = CommonManager.getINSTANCE().getItemSupport().getTag(event.getItem(), "uploadPanel");
                if (tag == null) return;
                switch (tag) {
                    case "save":
                        event.setCancelled(true);
                        event.getPlayer().setCooldown(event.getItem().getType(), 40);
                        setupSession.removeSetupListener("uploadTaskListener");
                        setupSession.setAllowCommands(true);
                        player.getInventory().clear();
                        inventoryBackup.restore(player);
                        registerItemFrameProtector(setupSession, localName);
                        SteveSus.newChain().delay(2).sync(() -> {
                            if (uploadFrame.isEmpty() || downloadFrame.isEmpty()) {
                                player.sendMessage(ChatColor.RED + "Task not saved because you didn't set all required panels.");
                                uploadFrame.forEach(Entity::remove);
                                downloadFrame.forEach(Entity::remove);
                                return;
                            }
                            OrphanLocationProperty exporter = new OrphanLocationProperty();
                            JsonObject config = new JsonObject();
                            config.addProperty("downloadTime", taskTime[0]);
                            config.addProperty("uploadTime", taskTime[0]);
                            JsonArray downloadPanels = new JsonArray();
                            JsonArray uploadPanels = new JsonArray();
                            uploadFrame.forEach(frame -> uploadPanels.add(exporter.toExportValue(frame.getLocation()).toString()));
                            downloadFrame.forEach(frame -> downloadPanels.add(exporter.toExportValue(frame.getLocation()).toString()));
                            config.add("downloadPanels", downloadPanels);
                            config.add("uploadPanels", uploadPanels);
                            ArenaManager.getINSTANCE().saveTaskData(getInstance(), setupSession, localName, config.getAsJsonObject());
                            player.sendMessage(ChatColor.GRAY + "Task saved: " + ChatColor.translateAlternateColorCodes('&', getDefaultDisplayName()));
                        }).execute();
                        break;
                    case "plus":
                        event.setCancelled(true);
                        taskTime[0]++;
                        player.sendTitle(String.valueOf(taskTime[0]), ChatColor.YELLOW + "Task time in seconds", 0, 20, 0);
                        break;
                    case "minus":
                        event.setCancelled(true);
                        if (taskTime[0] > 4) {
                            taskTime[0]--;
                            player.sendTitle(String.valueOf(taskTime[0]), ChatColor.YELLOW + "Task time in seconds", 0, 20, 0);
                        }
                        break;
                }
            }

            @Override
            public void onHangingPlace(SetupSession setupSession, HangingPlaceEvent event) {
                ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
                if (item == null || item.getType() == Material.AIR) return;
                String tag = CommonManager.getINSTANCE().getItemSupport().getTag(item, "uploadPanel");
                if (tag == null) return;
                if (tag.equals("download")) {
                    downloadFrame.add((ItemFrame) event.getEntity());
                    event.getEntity().setMetadata("uploadPanel", new FixedMetadataValue(SteveSus.getInstance(), localName));
                    ItemStack itemItem = ItemUtil.createItem(ItemUtil.getMaterial("CONCRETE_POWDER", "BLACK_CONCRETE_POWDER"), (byte) 0, 1, false, null, "&eDOWNLOAD", null);
                    ((ItemFrame) event.getEntity()).setItem(itemItem);
                    player.sendMessage(ChatColor.GRAY + "DOWNLOAD panel placed. If you want to remove this location just break it.");
                } else if (tag.equals("upload")) {
                    uploadFrame.add((ItemFrame) event.getEntity());
                    ItemStack itemItem = ItemUtil.createItem(ItemUtil.getMaterial("CONCRETE_POWDER", "BLACK_CONCRETE_POWDER"), (byte) 0, 1, false, null, "&eUPLOAD", null);
                    ((ItemFrame) event.getEntity()).setItem(itemItem);
                    event.getEntity().setMetadata("uploadPanel", new FixedMetadataValue(SteveSus.getInstance(), localName));
                    player.sendMessage(ChatColor.GRAY + "UPLOAD panel placed. If you want to remove this location just break it.");
                }
            }


            @Override
            public void onHangingBreakByEntity(SetupSession setupSession, HangingBreakByEntityEvent event) {
                Entity item = event.getEntity();
                if (item instanceof ItemFrame) {
                    if (downloadFrame.contains(item)) {
                        downloadFrame.remove(item);
                        item.remove();
                    } else if (uploadFrame.contains(item)) {
                        uploadFrame.remove(item);
                        item.remove();
                    } else {
                        if (event.getEntity().hasMetadata("uploadPanel")) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        });


    }

    @Override
    public void onSetupLoad(SetupSession setupSession, String localName, JsonObject configData) {
        registerItemFrameProtector(setupSession, localName);
    }

    @Override
    public void onSetupClose(SetupSession setupSession, String localName, JsonObject configData) {

    }

    @Override
    public void onRemove(SetupSession setupSession, String localName, JsonObject configData) {

    }

    @Override
    public @Nullable GameTask onGameInit(GameArena gameArena, JsonObject configuration, String localName) {
        if (!validateElements(configuration, "downloadTime", "uploadTime", "downloadPanels", "uploadPanels"))
            return null;
        int downloadTime = configuration.get("downloadTime").getAsInt();
        int uploadTime = configuration.get("uploadTime").getAsInt();
        OrphanLocationProperty converter = new OrphanLocationProperty();
        JsonArray downloadFrames = configuration.getAsJsonArray("downloadPanels");
        JsonArray uploadFrames = configuration.getAsJsonArray("uploadPanels");
        LinkedList<Location> downloadLocations = new LinkedList<>();
        LinkedList<Location> uploadLocations = new LinkedList<>();
        downloadFrames.forEach(element -> {
            Location loc = converter.convert(element.getAsString(), null);
            if (loc != null) {
                loc.setWorld(gameArena.getWorld());
                downloadLocations.add(loc);
            }
        });
        uploadFrames.forEach(element -> {
            Location loc = converter.convert(element.getAsString(), null);
            if (loc != null) {
                loc.setWorld(gameArena.getWorld());
                uploadLocations.add(loc);
            }
        });

        return new UploadTask(gameArena, downloadTime, uploadTime, downloadLocations, uploadLocations, localName);
    }

    private static void registerItemFrameProtector(SetupSession setupSession, String localTaskName) {
        setupSession.addSetupListener(localTaskName + "_uploadPanelProtector", new SetupListener() {
            @Override
            public void onPlayerInteractEntity(SetupSession setupSession1, PlayerInteractEntityEvent event) {
                if (event.getRightClicked() != null && event.getRightClicked().hasMetadata("uploadPanel")) {
                    String taskName = event.getRightClicked().getMetadata("uploadPanel").get(0).asString();
                    if (AddCommand.hasTaskWithRememberName(event.getPlayer(), taskName)) {
                        event.setCancelled(true);
                    }
                }
            }

            @Override
            public void onHangingBreakByEntity(SetupSession setupSession1, HangingBreakByEntityEvent event) {
                if (event.getEntity() != null && event.getEntity().hasMetadata("uploadPanel")) {
                    String taskName = event.getEntity().getMetadata("uploadPanel").get(0).asString();
                    if (AddCommand.hasTaskWithRememberName(event.getRemover(), taskName)) {
                        event.setCancelled(true);
                    }
                }
            }

            @Override
            public void onHangingBreak(SetupSession setupSession1, HangingBreakEvent event) {
                if (event.getEntity() != null && event.getEntity().hasMetadata("uploadPanel")) {
                    String taskName = event.getEntity().getMetadata("uploadPanel").get(0).asString();
                    if (AddCommand.hasTaskWithRememberName(event.getEntity().getWorld().getName(), taskName)) {
                        event.setCancelled(true);
                    }
                }
            }

            @Override
            public void onEntityDamageByEntity(SetupSession setupSession1, EntityDamageByEntityEvent event) {
                if (event.getEntity() != null && event.getEntity().hasMetadata("uploadPanel")) {
                    String taskName = event.getEntity().getMetadata("uploadPanel").get(0).asString();
                    if (AddCommand.hasTaskWithRememberName(event.getEntity().getWorld().getName(), taskName)) {
                        event.setCancelled(true);
                    }
                }
            }
        });
    }
}
