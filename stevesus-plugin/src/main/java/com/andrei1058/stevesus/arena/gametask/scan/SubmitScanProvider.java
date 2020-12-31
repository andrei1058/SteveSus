package com.andrei1058.stevesus.arena.gametask.scan;

import com.andrei1058.hologramapi.Hologram;
import com.andrei1058.hologramapi.HologramPage;
import com.andrei1058.hologramapi.content.LineTextContent;
import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.task.TaskProvider;
import com.andrei1058.stevesus.api.arena.task.TaskTriggerType;
import com.andrei1058.stevesus.api.arena.task.TaskType;
import com.andrei1058.stevesus.api.server.GameSound;
import com.andrei1058.stevesus.api.setup.SetupListener;
import com.andrei1058.stevesus.api.setup.SetupSession;
import com.andrei1058.stevesus.arena.ArenaManager;
import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.gui.ItemUtil;
import com.andrei1058.stevesus.config.properties.OrphanLocationProperty;
import com.andrei1058.stevesus.language.LanguageManager;
import com.andrei1058.stevesus.api.server.multiarena.InventoryBackup;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class SubmitScanProvider extends TaskProvider {

    public static final String MSG_CANNOT_SCAN = "game-task-scan-in-use";
    public static final String MSG_SCANNING_DONE = "game-task-scan-completed";
    public static final String MSG_SCANNING_SUBTITLE = "game-task-scanning-subtitle";

    private static SubmitScanProvider instance;

    private SubmitScanProvider() {
    }

    public static SubmitScanProvider getInstance() {
        if (instance == null) {
            instance = new SubmitScanProvider();
        }
        return instance;
    }

    @Override
    public String getDefaultDisplayName() {
        return "&2&lSubmit Scan";
    }

    @Override
    public String getDefaultDescription() {
        return "&fShift to start scan!";
    }

    @Override
    public String getIdentifier() {
        return "submit_scan";
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
        InventoryBackup.createInventoryBackup(player);
        player.getInventory().setHeldItemSlot(1);

        double minRange = 1;

        Arrays.asList(" ", " ",
                ChatColor.AQUA + "1." + ChatColor.RESET + " Use the items in your inventory to adjust task settings.",
                ChatColor.AQUA + "2." + ChatColor.RESET + " You can increase and decrease this task's usage radius (Capsule radius). When set to " + minRange + " players will need to stand on the exact scan location block.",
                ChatColor.AQUA + "3." + ChatColor.RESET + " You can increase and decrease how long it takes to scan.")
                .forEach(player::sendMessage);

        ItemStack saveAndClose = ItemUtil.createItem("BOOK", (byte) 0, 1, true, Arrays.asList("customTaskItem", "saveAndClose"), ChatColor.RED + "" + ChatColor.BOLD + "Save and close: " + ChatColor.RESET + getDefaultDisplayName(), null);
        player.getInventory().setItem(0, saveAndClose);

        ItemStack setScanLocation = ItemUtil.createItem("GOLDEN_APPLE", (byte) 0, 1, false, Arrays.asList("customTaskItem", "scanLocation"), "&bSet Scan Capsule at this location &6[Right Click]", null);
        player.getInventory().setItem(2, setScanLocation);

        ItemStack plusDuration = ItemUtil.createItem(ItemUtil.getMaterial("CONCRETE", "RED_CONCRETE"), (byte) 14, 1, false, Arrays.asList("customTaskItem", "plusDuration"), "&cIncrease Scan duration in seconds &6[Right Click]", null);
        ItemStack minusDuration = ItemUtil.createItem(ItemUtil.getMaterial("CONCRETE", "LIGHT_BLUE_CONCRETE"), (byte) 3, 1, false, Arrays.asList("customTaskItem", "minusDuration"), "&9Decrease Scan duration in seconds &6[Right Click]", null);
        player.getInventory().setItem(4, plusDuration);
        player.getInventory().setItem(5, minusDuration);

        ItemStack plusRange = ItemUtil.createItem(ItemUtil.getMaterial("CONCRETE", "MAGENTA_CONCRETE"), (byte) 2, 1, false, Arrays.asList("customTaskItem", "plusRange"), "&dIncrease Scan Capsule range &6[Right Click]", null);
        ItemStack minusRange = ItemUtil.createItem(ItemUtil.getMaterial("CONCRETE", "YELLOW_CONCRETE"), (byte) 4, 1, false, Arrays.asList("customTaskItem", "minusRange"), "&eDecrease Scan Capsule range &6[Right Click]", null);
        player.getInventory().setItem(7, plusRange);
        player.getInventory().setItem(8, minusRange);

        double maxRange = 5;
        double rangeIncreaseUnit = 0.1;
        final double[] currentRange = {minRange};

        int minSeconds = 10;
        int maxSeconds = 60;
        final int[] currentSeconds = {minSeconds};

        Location[] scanCapsuleLocation = {null};
        Hologram[] scanCapsuleHologram = {null};

        // save and close task setup.
        final boolean[] preventCalledTwice = {false};
        Function<Void, Void> saveAndCloseTaskSetup = (Void o) -> {
            if (preventCalledTwice[0]) return null;
            preventCalledTwice[0] = true;
            if (scanCapsuleLocation[0] == null) {
                player.sendTitle(ChatColor.translateAlternateColorCodes('&', getDefaultDisplayName()), ChatColor.RED + "Not saved!", 0, 60, 0);
                player.sendMessage(ChatColor.RED + getDefaultDisplayName() + " wasn't saved because you didn't set a Scan Capsule location!");
                GameSound.JOIN_SOUND_CURRENT.playToPlayer(player);
                return null;
            }

            JsonObject config = new JsonObject();
            config.addProperty("radius", currentRange[0]);
            config.addProperty("radius", currentRange[0]);
            config.addProperty("seconds", currentSeconds[0]);
            config.addProperty("location", new OrphanLocationProperty().toExportValue(scanCapsuleLocation[0]).toString());
            ArenaManager.getINSTANCE().saveTaskData(this, setupSession, localName, config);
            setupSession.setAllowCommands(true);
            InventoryBackup.restoreInventory(player);
            GameSound.JOIN_SOUND_CURRENT.playToPlayer(player);
            player.sendMessage(ChatColor.GRAY + "Command usage is now enabled!");
            setupSession.removeSetupListener("submit_scan_setup");
            GameSound.JOIN_SOUND_CURRENT.playToPlayer(player);
            setupSession.cacheValue("submit_scan_" + localName, scanCapsuleHologram[0]);
            return null;
        };

        List<Location> particleLocations = new ArrayList<>();
        int particlesTask = Bukkit.getScheduler().runTaskTimer(SteveSus.getInstance(), () -> particleLocations.forEach(loc -> {
            loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc.clone().add(0, 1, 0), 2);
            loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc.clone().add(0, 2, 0), 2);
        }), 0L, 10).getTaskId();
        setupSession.cacheValue("submit_scan_task_id_" + localName, particlesTask);

        Function<Location, Void> setScanLoc = location -> {
            if (scanCapsuleHologram[0] != null) {
                scanCapsuleHologram[0].hide();
            }
            scanCapsuleHologram[0] = new Hologram(location, 2);
            HologramPage page = scanCapsuleHologram[0].getPage(0);
            assert page != null;
            page.setLineContent(0, new LineTextContent(s -> "&b&lSubmit Scan"));
            page.setLineContent(1, new LineTextContent(s -> "&fSet here! (" + localName + ")"));
            scanCapsuleLocation[0] = location;
            scanCapsuleHologram[0].hide(player);
            scanCapsuleHologram[0].show(player);
            player.sendTitle(" ", ChatColor.AQUA + "Scan location set!", 0, 50, 0);
            GameSound.JOIN_SOUND_CURRENT.playToPlayer(player);
            particleLocations.clear();
            particleLocations.addAll(getCircle(location, currentRange[0], 20));
            return null;
        };

        setupSession.addSetupListener("submit_scan_setup", new SetupListener() {

            @Override
            public void onPlayerInteract(SetupSession setupSession1, PlayerInteractEvent event) {
                ItemStack itemStack = CommonManager.getINSTANCE().getItemSupport().getInHand(event.getPlayer());
                if (itemStack == null) return;
                if (itemStack.getType() == Material.AIR) return;
                String tag = CommonManager.getINSTANCE().getItemSupport().getTag(itemStack, "customTaskItem");
                if (tag == null) return;
                event.setCancelled(true);

                if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    switch (tag) {
                        case "saveAndClose":
                            SteveSus.newChain().delay(2).sync(() -> saveAndCloseTaskSetup.apply(null)).execute();
                            break;
                        case "scanLocation":
                            setScanLoc.apply(player.getLocation());
                            break;
                        case "plusDuration":
                            if (currentSeconds[0] < maxSeconds) {
                                currentSeconds[0]++;
                            }
                            event.getPlayer().sendTitle(ChatColor.BLUE + "" + currentSeconds[0], ChatColor.WHITE + "Duration", 0, 40, 0);
                            GameSound.JOIN_SOUND_SELF.playToPlayer(player);
                            break;
                        case "minusDuration":
                            if (currentSeconds[0] > minSeconds) {
                                currentSeconds[0]--;
                            }
                            event.getPlayer().sendTitle(ChatColor.BLUE + "" + currentSeconds[0], ChatColor.WHITE + "Duration", 0, 40, 0);
                            GameSound.JOIN_SOUND_SELF.playToPlayer(player);
                            break;
                        case "plusRange":
                            if (currentRange[0] < maxRange) {
                                currentRange[0] += rangeIncreaseUnit;
                                if (scanCapsuleLocation[0] != null) {
                                    particleLocations.clear();
                                    particleLocations.addAll(getCircle(scanCapsuleLocation[0], currentRange[0], 20));
                                }
                            }
                            event.getPlayer().sendTitle(ChatColor.BLUE + "" + new DecimalFormat("#.0").format(currentRange[0]), ChatColor.WHITE + "Radius", 0, 40, 0);
                            GameSound.JOIN_SOUND_SELF.playToPlayer(player);
                            break;
                        case "minusRange":
                            if (currentRange[0] > minRange) {
                                currentRange[0] -= rangeIncreaseUnit;
                            }
                            if (scanCapsuleLocation[0] != null) {
                                particleLocations.clear();
                                particleLocations.addAll(getCircle(scanCapsuleLocation[0], currentRange[0], 20));
                            }
                            event.getPlayer().sendTitle(ChatColor.BLUE + "" + new DecimalFormat("#.0").format(currentRange[0]), ChatColor.WHITE + "Radius", 0, 40, 0);
                            GameSound.JOIN_SOUND_SELF.playToPlayer(player);
                            break;
                    }
                }
            }

            @Override
            public void onPlayerDropItem(SetupSession setupSession1, PlayerDropItemEvent event) {
                event.setCancelled(true);
            }

            @Override
            public void onPlayerPickupItem(SetupSession setupSession1, EntityPickupItemEvent event) {
                event.setCancelled(true);
            }
        });
    }

    @Override
    public void onSetupLoad(SetupSession setupSession, String localName, JsonObject configData) {
        double range = configData.get("radius").getAsDouble();
        Location location = new OrphanLocationProperty().convert(configData.get("location").getAsString(), null);
        if (location == null) return;
        location.setWorld(Bukkit.getWorld(setupSession.getWorldName()));

        List<Location> particleLocations = new ArrayList<>(getCircle(location, range, 20));
        int particlesTask = Bukkit.getScheduler().runTaskTimer(SteveSus.getInstance(), () -> particleLocations.forEach(loc -> {
            loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc.clone().add(0, 1, 0), 2);
            loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc.clone().add(0, 2, 0), 2);
        }), 0L, 10).getTaskId();
        setupSession.cacheValue("submit_scan_task_id_" + localName, particlesTask);

        Hologram hologram = new Hologram(location.add(0, 1.7, 0), 2);
        HologramPage page = hologram.getPage(0);
        assert page != null;
        page.setLineContent(0, new LineTextContent(s -> "&b&lSubmit Scan"));
        page.setLineContent(1, new LineTextContent(s -> "&fSet here! (" + localName + ")"));
        hologram.hide(setupSession.getPlayer());
        hologram.show(setupSession.getPlayer());
        setupSession.cacheValue("submit_scan_" + localName, hologram);
    }

    @Override
    public void onSetupClose(SetupSession setupSession, String localName, JsonObject configData) {
        Object cached = setupSession.getCachedValue("submit_scan_task_id_" + localName);
        if (cached != null) {
            Bukkit.getScheduler().cancelTask((Integer) cached);
        }
    }

    @Override
    public void onRemove(SetupSession setupSession, String localName, JsonObject configData) {
        Hologram hologram = (Hologram) setupSession.getCachedValue("submit_scan_" + localName);
        if (hologram != null) {
            hologram.hide();
            setupSession.removeCacheValue("submit_scan_" + localName);
        }
        Object cached = setupSession.getCachedValue("submit_scan_task_id_" + localName);
        if (cached != null) {
            Bukkit.getScheduler().cancelTask((Integer) cached);
            setupSession.removeCacheValue("submit_scan_task_id_" + localName);
        }
    }

    @Override
    public @Nullable SubmitScanTask onGameInit(Arena arena, JsonObject configuration, String localName) {
        if (!LanguageManager.getINSTANCE().getDefaultLocale().hasPath(MSG_CANNOT_SCAN)) {
            LanguageManager.getINSTANCE().getDefaultLocale().setMsg(MSG_CANNOT_SCAN, "&cCapsule already in use!");
        }
        if (!LanguageManager.getINSTANCE().getDefaultLocale().hasPath(MSG_SCANNING_SUBTITLE)) {
            LanguageManager.getINSTANCE().getDefaultLocale().setMsg(MSG_SCANNING_SUBTITLE, "&6Scan complete in {time}s.");
        }
        if (!LanguageManager.getINSTANCE().getDefaultLocale().hasPath(MSG_SCANNING_DONE)) {
            LanguageManager.getINSTANCE().getDefaultLocale().setMsg(MSG_SCANNING_DONE, "&a&lScan complete!");
        }
        try {
            JsonElement radius = configuration.get("radius");
            if (radius == null) return null;
            JsonElement scanDuration = configuration.get("seconds");
            if (scanDuration == null) return null;
            JsonElement capsule = configuration.get("location");
            if (capsule == null) return null;
            Location location = new OrphanLocationProperty().convert(capsule.getAsString(), null);
            if (location == null) return null;
            location.setWorld(arena.getWorld());
            return new SubmitScanTask(radius.getAsDouble(), scanDuration.getAsInt(), location, arena, localName);
        } catch (Exception ignored) {
        }
        return null;
    }

    public ArrayList<Location> getCircle(Location center, double radius, int amount) {
        World world = center.getWorld();
        double increment = (2 * Math.PI) / amount;
        ArrayList<Location> locations = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            double angle = i * increment;
            double x = center.getX() + (radius * Math.cos(angle));
            double z = center.getZ() + (radius * Math.sin(angle));
            locations.add(new Location(world, x, center.getY(), z));
        }
        return locations;
    }
}
