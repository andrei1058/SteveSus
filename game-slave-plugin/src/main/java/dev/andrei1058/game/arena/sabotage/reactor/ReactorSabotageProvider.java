package dev.andrei1058.game.arena.sabotage.reactor;

import com.andrei1058.spigot.commandlib.ICommandNode;
import com.andrei1058.spigot.commandlib.fast.FastSubCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubRootCommand;
import dev.andrei1058.game.SteveSus;
import dev.andrei1058.game.api.SteveSusAPI;
import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.arena.sabotage.SabotageBase;
import dev.andrei1058.game.api.arena.sabotage.SabotageProvider;
import dev.andrei1058.game.api.glow.GlowColor;
import dev.andrei1058.game.api.glow.GlowingBox;
import dev.andrei1058.game.api.locale.LocaleManager;
import dev.andrei1058.game.api.locale.Message;
import dev.andrei1058.game.api.setup.SetupSession;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.config.properties.OrphanLocationProperty;
import dev.andrei1058.game.setup.SetupManager;
import com.google.gson.JsonObject;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class ReactorSabotageProvider extends SabotageProvider {

    public static String NAME_PATH;
    public static String GUI_WAITING;
    public static String GUI_NORMAL;
    public static String FIXED_SUBTITLE;

    private static ReactorSabotageProvider instance;

    private ReactorSabotageProvider() {
    }

    public static ReactorSabotageProvider getInstance() {
        return instance == null ? instance = new ReactorSabotageProvider() : instance;
    }

    @Override
    public void onRegister() {
        LocaleManager localeManager = SteveSusAPI.getInstance().getLocaleHandler();
        NAME_PATH = Message.SABOTAGE_PATH_.toString() + getUniqueIdentifier() + "-boss-bar";
        GUI_WAITING = Message.SABOTAGE_PATH_.toString() + getUniqueIdentifier() + "-gui-waiting";
        GUI_NORMAL = Message.SABOTAGE_PATH_.toString() + getUniqueIdentifier() + "-gui-normal";
        FIXED_SUBTITLE = Message.SABOTAGE_PATH_.toString() + getUniqueIdentifier() + "-to-subtitle";
        localeManager.getDefaultLocale().addDefault(NAME_PATH, "&cFix Reactor Meltdown");
        localeManager.getDefaultLocale().addDefault(Message.DEFEAT_REASON_PATH_.toString() + getUniqueIdentifier(), "&cYou couldn't fix reactor meltdown in time!");
        localeManager.getDefaultLocale().addDefault(GUI_WAITING, "&0Waiting for second user");
        localeManager.getDefaultLocale().addDefault(GUI_NORMAL, "&0Reactor Normal");
        localeManager.getDefaultLocale().addDefault(FIXED_SUBTITLE, "&aReactor Meltdown Fixed!");

        // add setup commands
        FastSubRootCommand currentSabotage = new FastSubRootCommand(getUniqueIdentifier());
        currentSabotage.withHeaderContent("&1|| &3Reactor Meltdown Commands:");
        currentSabotage.withDescription(s -> "&e - Reactor Meltdown");
        getMyCommand().withSubNode(currentSabotage);

        FastSubCommand addFixLocation1 = new FastSubCommand("setFixLoc1");
        currentSabotage.withSubNode(addFixLocation1
                .withDisplayHover(s -> "&eSet a fix panel at your target block.")
                .withPermAdditions(s -> (s instanceof Player))
                .withExecutor((s, args) -> {
                    Player p = (Player) s;
                    SetupSession setupSession = SetupManager.getINSTANCE().getSession(p);
                    assert setupSession != null;
                    // cache glowing box
                    Block newBlock = p.getTargetBlock(null, 3);
                    if (newBlock == null) {
                        p.sendTitle(" ", ChatColor.RED + "You need to target a block!", 0, 60, 0);
                        return;
                    }
                    Object pos1box = setupSession.getCachedValue(getUniqueIdentifier() + "pos1glow");
                    if (pos1box != null) {
                        GlowingBox glowingBox = (GlowingBox) pos1box;
                        glowingBox.stopGlowing(p);
                        glowingBox.getMagmaCube().remove();
                    }
                    GlowingBox glowingBox = new GlowingBox(newBlock.getLocation().add(0.5, 0, 0.5), 2, GlowColor.RED);
                    glowingBox.startGlowing(p);
                    setupSession.cacheValue(getUniqueIdentifier() + "pos1glow", glowingBox);
                    setupSession.cacheValue(getUniqueIdentifier() + "pos1loc", newBlock.getLocation());
                    p.sendMessage(ChatColor.GRAY + "Panel1 loc set!");
                }));
        FastSubCommand addFixLocation2 = new FastSubCommand("setFixLoc2");
        currentSabotage.withSubNode(addFixLocation2
                .withDisplayHover(s -> "&eSet a fix panel at your target block.")
                .withPermAdditions(s -> (s instanceof Player))
                .withExecutor((s, args) -> {
                    Player p = (Player) s;
                    SetupSession setupSession = SetupManager.getINSTANCE().getSession(p);
                    assert setupSession != null;
                    // cache glowing box
                    Block newBlock = p.getTargetBlock(null, 3);
                    if (newBlock == null) {
                        p.sendTitle(" ", ChatColor.RED + "You need to target a block!", 0, 60, 0);
                        return;
                    }
                    Object pos1box = setupSession.getCachedValue(getUniqueIdentifier() + "pos2glow");
                    if (pos1box != null) {
                        GlowingBox glowingBox = (GlowingBox) pos1box;
                        glowingBox.stopGlowing(p);
                        glowingBox.getMagmaCube().remove();
                    }
                    GlowingBox glowingBox = new GlowingBox(newBlock.getLocation().add(0.5, 0, 0.5), 2, GlowColor.RED);
                    glowingBox.startGlowing(p);
                    setupSession.cacheValue(getUniqueIdentifier() + "pos2glow", glowingBox);
                    setupSession.cacheValue(getUniqueIdentifier() + "pos2loc", newBlock.getLocation());
                    p.sendMessage(ChatColor.GRAY + "Panel2 loc set!");
                }));
        FastSubCommand setDeadLine = new FastSubCommand("setDeadLine");
        currentSabotage.withSubNode(setDeadLine
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
                }));
    }

    @Override
    public Plugin getOwner() {
        return SteveSus.getInstance();
    }

    @Override
    public @NotNull String getUniqueIdentifier() {
        return "reactor_meltdown";
    }

    @Override
    public @Nullable SabotageBase onArenaInit(GameArena gameArena, JsonObject configuration) {
        if (gameArena.hasLoadedSabotage(getUniqueIdentifier())) return null;
        if (!(configuration.has("loc1") && configuration.has("loc2") && configuration.has("deadLine"))) return null;
        OrphanLocationProperty importer = new OrphanLocationProperty();
        Location loc1 = importer.convert(configuration.get("loc1").getAsString(), null);
        if (loc1 == null) return null;
        loc1.setWorld(gameArena.getWorld());
        Location loc2 = importer.convert(configuration.get("loc2").getAsString(), null);
        if (loc2 == null) return null;
        loc2.setWorld(gameArena.getWorld());
        int deadLine = configuration.get("deadLine").getAsInt();
        return new ReactorSabotage(gameArena, deadLine, loc1, loc2);
    }

    @Override
    public void onSetupSessionClose(SetupSession setupSession) {
        Object deadLine = setupSession.getCachedValue(getUniqueIdentifier() + "_deadLine");
        Object pos1 = setupSession.getCachedValue(getUniqueIdentifier() + "pos1loc");
        Object pos2 = setupSession.getCachedValue(getUniqueIdentifier() + "pos2loc");
        if (deadLine == null && pos1 == null && pos2 == null) return;

        JsonObject currentConfig = ArenaManager.getINSTANCE().getSabotageConfiguration(setupSession.getWorldName(), this);
        if (currentConfig == null) {
            currentConfig = new JsonObject();
        }

        // replace existing deadLine
        if (currentConfig.has("deadLine")) {
            if (deadLine != null) {
                currentConfig.remove("deadLine");
                currentConfig.addProperty("deadLine", (int) deadLine);
            }
        } else {
            // save deadLine
            currentConfig.addProperty("deadLine", deadLine == null ? 35 : (int) deadLine);
        }

        if (pos1 != null){
            if (currentConfig.has("loc1")){
                currentConfig.remove("loc1");
            }
            OrphanLocationProperty exporter = new OrphanLocationProperty();
            currentConfig.addProperty("loc1", exporter.toExportValue((Location) pos1).toString());
        }
        if (pos2 != null){
            if (currentConfig.has("loc2")){
                currentConfig.remove("loc2");
            }
            OrphanLocationProperty exporter = new OrphanLocationProperty();
            currentConfig.addProperty("loc2", exporter.toExportValue((Location) pos2).toString());
        }
        SteveSusAPI.getInstance().getArenaHandler().saveSabotageConfiguration(setupSession.getWorldName(), this, currentConfig, true);
    }
}
