package com.andrei1058.stevesus.arena.sabotage.fixlights;

import com.andrei1058.spigot.commandlib.fast.FastSubCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubRootCommand;
import com.andrei1058.stevesus.api.SteveSusAPI;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.sabotage.SabotageBase;
import com.andrei1058.stevesus.api.arena.sabotage.SabotageProvider;
import com.andrei1058.stevesus.api.glow.GlowColor;
import com.andrei1058.stevesus.api.glow.GlowingBox;
import com.andrei1058.stevesus.api.locale.LocaleManager;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.api.setup.SetupSession;
import com.andrei1058.stevesus.config.properties.OrphanLocationProperty;
import com.andrei1058.stevesus.setup.SetupManager;
import com.google.gson.JsonObject;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class LightsSabotageProvider extends SabotageProvider {

    public static String NAME_PATH;
    public static String FIXED_SUBTITLE;
    private static LightsSabotageProvider instance;

    private LightsSabotageProvider() {
    }

    public static LightsSabotageProvider getInstance() {
        return instance == null ? instance = new LightsSabotageProvider() : instance;
    }

    @Override
    public void onRegister() {
        LocaleManager localeManager = SteveSusAPI.getInstance().getLocaleHandler();
        NAME_PATH = Message.SABOTAGE_PATH_.toString() + getUniqueIdentifier() + "-boss-bar";
        FIXED_SUBTITLE = Message.SABOTAGE_PATH_.toString() + getUniqueIdentifier() + "-to-subtitle";
        localeManager.getDefaultLocale().addDefault(FIXED_SUBTITLE, "&aLights Fixed!");
        localeManager.getDefaultLocale().addDefault(NAME_PATH, "&cFix Lights");

        // add setup commands
        FastSubRootCommand currentSabotage = new FastSubRootCommand(getUniqueIdentifier());
        currentSabotage.withHeaderContent("&1|| &3Fix Lights Commands:");
        currentSabotage.withDescription(s -> "&e - Fix Lights");
        getMyCommand().withSubNode(currentSabotage);

        FastSubCommand setLoc = new FastSubCommand("setLocation");
        currentSabotage.withSubNode(setLoc
                .withDisplayHover(s -> "&eSet the fix panel at your target block.")
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
                    setupSession.cacheValue(getUniqueIdentifier() + "loc", newBlock.getLocation());
                    p.sendMessage(ChatColor.GRAY + "Fix Lights panel loc set!");

                    JsonObject currentConfig = new JsonObject();
                    OrphanLocationProperty exporter = new OrphanLocationProperty();
                    currentConfig.addProperty("location", exporter.toExportValue(newBlock.getLocation()).toString());
                    SteveSusAPI.getInstance().getArenaHandler().saveSabotageConfiguration(setupSession.getWorldName(), this, currentConfig, true);
                }));
    }

    @Override
    public Plugin getOwner() {
        return SteveSusAPI.getInstance();
    }

    @Override
    public @NotNull String getUniqueIdentifier() {
        return "lights";
    }

    @Override
    public @Nullable SabotageBase onArenaInit(Arena arena, JsonObject configuration) {
        if (arena.hasLoadedSabotage(getUniqueIdentifier())) return null;
        // check required data
        if (!(configuration.has("location"))) return null;

        // load location
        OrphanLocationProperty importer = new OrphanLocationProperty();
        Location loc = importer.convert(configuration.get("location").getAsString(), null);
        if (loc == null) return null;
        loc.setWorld(arena.getWorld());
        return new LightsSabotage(arena, loc);
    }

    @Override
    public void onSetupSessionClose(SetupSession setupSession) {

    }
}
