package dev.andrei1058.game.arena.sabotage.oxygen;

import com.andrei1058.spigot.commandlib.ICommandNode;
import com.andrei1058.spigot.commandlib.fast.FastSubCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubRootCommand;
import dev.andrei1058.game.api.SteveSusAPI;
import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.arena.sabotage.SabotageBase;
import dev.andrei1058.game.api.arena.sabotage.SabotageProvider;
import dev.andrei1058.game.api.locale.LocaleManager;
import dev.andrei1058.game.api.locale.Message;
import dev.andrei1058.game.api.setup.SetupSession;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.config.properties.OrphanLocationProperty;
import dev.andrei1058.game.setup.SetupManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OxygenSabotageProvider extends SabotageProvider {

    public static String NAME_PATH;
    public static String FIXED_SUBTITLE;
    public static String TO_FIX_HOLOGRAM;
    private static OxygenSabotageProvider instance;

    private OxygenSabotageProvider() {

    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void onRegister() {
        LocaleManager localeManager = SteveSusAPI.getInstance().getLocaleHandler();
        NAME_PATH = Message.SABOTAGE_PATH_.toString() + getUniqueIdentifier() + "-boss-bar";
        TO_FIX_HOLOGRAM = Message.SABOTAGE_PATH_.toString() + getUniqueIdentifier() + "-to-fix-holo";
        FIXED_SUBTITLE = Message.SABOTAGE_PATH_.toString() + getUniqueIdentifier() + "-to-subtitle";
        localeManager.getDefaultLocale().addDefault(TO_FIX_HOLOGRAM, "&c&lSystem Error!");
        localeManager.getDefaultLocale().addDefault(FIXED_SUBTITLE, "&aOxygen Leak Fixed!");
        localeManager.getDefaultLocale().addDefault(NAME_PATH, "&cFix Oxygen Leak &f({fixed}&c/&f{total}&f)");
        localeManager.getDefaultLocale().addDefault(Message.DEFEAT_REASON_PATH_.toString() + getUniqueIdentifier(), "&cYou couldn't fix oxygen leak in time!");

        // add setup commands
        FastSubRootCommand currentSabotage = new FastSubRootCommand(getUniqueIdentifier());
        currentSabotage.withHeaderContent("&1|| &3Oxygen Sabotage Commands:");
        currentSabotage.withDescription(s -> "&e - Oxygen Leak");
        getMyCommand().withSubNode(currentSabotage);
        FastSubCommand addFixLocation = new FastSubCommand("addFixLocation");
        FastSubCommand setDeadLine = new FastSubCommand("setDeadLine");
        FastSubCommand removeCurrentLocations = new FastSubCommand("removeCurrentLocations");
        FastSubCommand removeAllLocations = new FastSubCommand("removeAllLocations");
        currentSabotage
                .withSubNode(addFixLocation
                        .withDisplayHover(s -> "&eA fix utility will be spawned at this\n&elocation and players will have to go\n&ein all locations you set to insert\n&ePINs to fix this sabotage.")
                        .withPermAdditions(s -> (s instanceof Player))
                        .withExecutor((s, args) -> {
                            Player p = (Player) s;
                            SetupSession setupSession = SetupManager.getINSTANCE().getSession(p);
                            assert setupSession != null;
                            // cache and save later at setup close
                            Object count = setupSession.getCachedValue(getUniqueIdentifier() + "_lc");
                            int locCount = count == null ? 0 : (int) count;
                            setupSession.cacheValue(getUniqueIdentifier() + "_lc", ++locCount);
                            setupSession.cacheValue(getUniqueIdentifier() + "_l_" + locCount, p.getLocation());
                            p.sendMessage(ChatColor.GRAY + "Oxygen fix location saved!");
                        }))
                .withSubNode(setDeadLine
                        .withDisplayHover(s -> "&eSet fix dead line in seconds.\n&eIf players will not fix it in time\n&eithey will lose the game.")
                        .withPermAdditions(s -> (s instanceof Player))
                        .withExecutor((s, args) -> {
                            Player p = (Player) s;
                            if (args.length != 1) {
                                p.sendMessage(ChatColor.RED + "Usage: " + ChatColor.GRAY + ICommandNode.getClickCommand(setDeadLine) + " [time]");
                                return;
                            }
                            int seconds = 0;
                            try {
                                seconds = Integer.parseInt(args[0]);
                            } catch (Exception ignored) {
                            }
                            if (seconds < 1) {
                                p.sendMessage(ChatColor.RED + "Please provide a valid number");
                                return;
                            }
                            SetupSession setupSession = SetupManager.getINSTANCE().getSession(p);
                            assert setupSession != null;
                            // cache and save later at setup close
                            setupSession.removeCacheValue(getUniqueIdentifier() + "_deadLine");
                            setupSession.cacheValue(getUniqueIdentifier() + "_deadLine", seconds);
                            p.sendMessage(ChatColor.GRAY + "Timer set to: " + ChatColor.GOLD + seconds + ChatColor.GRAY + " seconds.");
                        }))
                .withSubNode(removeCurrentLocations
                        .withDisplayHover(s -> "&eRemove locations added during\n&ethis setup session.")
                        .withPermAdditions(s -> (s instanceof Player))
                        .withExecutor((s, args) -> {
                            Player p = (Player) s;
                            SetupSession setupSession = SetupManager.getINSTANCE().getSession(p);
                            assert setupSession != null;
                            // cache and save later at setup close
                            Object count = setupSession.getCachedValue(getUniqueIdentifier() + "_lc");
                            if (count == null) {
                                p.sendMessage(ChatColor.GRAY + "Nothing to remove.");
                            } else {
                                for (int i = 1; i <= (Integer) count; i++) {
                                    setupSession.removeCacheValue(getUniqueIdentifier() + "_l_" + i);
                                }
                                p.sendMessage(ChatColor.GRAY + "Location added during this session removed: " + ChatColor.GOLD + count + ChatColor.GRAY + ".");
                            }
                        }))
                .withSubNode(removeAllLocations
                        .withDisplayHover(s -> "&eRemove all fix locations.")
                        .withPermAdditions(s -> (s instanceof Player))
                        .withExecutor((s, args) -> {
                            Player p = (Player) s;
                            SetupSession setupSession = SetupManager.getINSTANCE().getSession(p);
                            assert setupSession != null;
                            // cache and save later at setup close
                            Object count = setupSession.getCachedValue(getUniqueIdentifier() + "_lc");
                            if (count != null) {
                                for (int i = 1; i <= (Integer) count; i++) {
                                    setupSession.removeCacheValue(getUniqueIdentifier() + "_l_" + i);
                                }
                            }
                            JsonObject config = SteveSusAPI.getInstance().getArenaHandler().getSabotageConfiguration(setupSession.getWorldName(), this);
                            if (config != null) {
                                if (config.has("locations")) {
                                    config.remove("locations");
                                }
                            } else {
                                config = new JsonObject();
                            }
                            SteveSusAPI.getInstance().getArenaHandler().saveSabotageConfiguration(setupSession.getWorldName(), this, config, true);
                            p.sendMessage(ChatColor.GRAY + "Fix locations removed (if there were any).");
                        }))
        ;
    }

    @Override
    public Plugin getOwner() {
        return SteveSusAPI.getInstance();
    }

    @Override
    public @NotNull String getUniqueIdentifier() {
        return "oxygen";
    }

    @Override
    public @Nullable SabotageBase onArenaInit(GameArena gameArena, JsonObject configuration) {
        if (gameArena.hasLoadedSabotage(getUniqueIdentifier())) return null;
        // check required data
        if (!(configuration.has("locations") && configuration.has("deadLine"))) return null;

        // load locations
        List<Location> locationList = new ArrayList<>();
        OrphanLocationProperty importer = new OrphanLocationProperty();
        for (JsonElement element : configuration.get("locations").getAsJsonArray()) {
            Location loc = importer.convert(element.getAsString(), null);
            if (loc != null) {
                loc.setWorld(gameArena.getWorld());
                locationList.add(loc);
            }
        }
        if (locationList.isEmpty()) return null;
        int deadLine = configuration.get("deadLine").getAsInt();
        return new OxygenSabotage(gameArena, deadLine, locationList);
    }

    @Override
    public void onSetupSessionClose(SetupSession setupSession) {
        Object counter = setupSession.getCachedValue(getUniqueIdentifier() + "_lc");
        Object deadLine = setupSession.getCachedValue(getUniqueIdentifier() + "_deadLine");
        if (counter == null && deadLine == null) return;
        JsonObject currentConfig = ArenaManager.getINSTANCE().getSabotageConfiguration(setupSession.getWorldName(), this);
        if (currentConfig == null) {
            currentConfig = new JsonObject();
        }
        JsonArray locations = new JsonArray();
        if (currentConfig.has("locations")) {
            locations = currentConfig.get("locations").getAsJsonArray();
        }
        // add new locations
        if (counter != null) {
            OrphanLocationProperty convertor = new OrphanLocationProperty();
            for (int i = 1; i <= (Integer) counter; i++) {
                Object cached = setupSession.getCachedValue(getUniqueIdentifier() + "_l_" + i);
                if (cached != null) {
                    locations.add(convertor.toExportValue((Location) cached).toString());
                }
            }
        }
        if (!currentConfig.has("locations")) {
            currentConfig.add("locations", locations);
        }
        // add dead line
        if (deadLine != null) {
            if (currentConfig.has("deadLine")) {
                currentConfig.remove("deadLine");
            }
            currentConfig.addProperty("deadLine", (int) deadLine);
        }
        SteveSusAPI.getInstance().getArenaHandler().saveSabotageConfiguration(setupSession.getWorldName(), this, currentConfig, true);
    }

    public static OxygenSabotageProvider getInstance() {
        return instance == null ? instance = new OxygenSabotageProvider() : instance;
    }
}
