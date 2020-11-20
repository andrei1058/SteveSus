package com.andrei1058.stevesus.setup;

import ch.jalu.configme.SettingsManager;
import com.andrei1058.hologramapi.Hologram;
import com.andrei1058.hologramapi.HologramPage;
import com.andrei1058.hologramapi.content.LineTextContent;
import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.ArenaTime;
import com.andrei1058.stevesus.api.server.ServerType;
import com.andrei1058.stevesus.api.setup.SetupListener;
import com.andrei1058.stevesus.api.setup.SetupSession;
import com.andrei1058.stevesus.arena.ArenaManager;
import com.andrei1058.stevesus.common.command.CommonCmdManager;
import com.andrei1058.stevesus.config.ArenaConfig;
import com.andrei1058.stevesus.server.ServerManager;
import com.andrei1058.stevesus.api.server.multiarena.InventoryBackup;
import com.andrei1058.stevesus.sidebar.GameSidebarManager;
import com.andrei1058.stevesus.sidebar.SidebarType;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;

public class SetupActivity implements SetupSession {

    private final Player player;
    private final String worldName;
    private SettingsManager config;
    private Hologram meetingButtonHologram;
    private BukkitTask setupTask;
    private boolean allowCommands = true;
    private final LinkedHashMap<String, Object> cachedValues = new LinkedHashMap<>();
    private final LinkedHashMap<String, SetupListener> setupListeners = new LinkedHashMap<>();
    private ArenaTime time;

    public SetupActivity(Player player, String world) {
        this.player = player;
        this.worldName = world;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    public SettingsManager getConfig() {
        return config;
    }

    @Override
    public String getWorldName() {
        return worldName;
    }

    @Override
    public void onStart(World world) {
        config = ArenaManager.getINSTANCE().getTemplate(worldName, true);
        if (ServerManager.getINSTANCE().getServerType() == ServerType.MULTI_ARENA) {
            InventoryBackup.createInventoryBackup(player);
        }
        time = config.getProperty(ArenaConfig.MAP_TIME);
        world.setWeatherDuration(0);
        world.setThunderDuration(0);
        world.setStorm(false);
        world.setThundering(false);
        world.setTime(time.getStartTick());
        player.teleport(world.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        player.setGameMode(GameMode.CREATIVE);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.getInventory().clear();
        GameSidebarManager.getInstance().removeSidebar(player);
        boolean up = false;
        if (player.getLocation().getY() <= 0) {
            up = true;
            player.teleport(player.getLocation().add(0, 10, 0), PlayerTeleportEvent.TeleportCause.PLUGIN);
        }
        player.sendMessage(ChatColor.GRAY + "Teleported to " + ChatColor.GREEN + getWorldName() + ChatColor.GRAY + "'s spawn point" + (up ? "(+ 10 y)." : "."));

        Bukkit.getScheduler().runTaskLater(SteveSus.getInstance(), () -> Bukkit.dispatchCommand(player, CommonCmdManager.getINSTANCE().getMainCmd().getName()), 60L);

        // spawn holograms
        SteveSus.newChain().delay(60).sync(this::reloadButtonHologram).execute();

        setupTask = Bukkit.getScheduler().runTaskTimer(SteveSus.getInstance(), () -> {
            // better use this here than in a move event since this gets cancelled at a certain point
            if (player.getLocation().getY() < 0) {
                player.teleport(player.getWorld().getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            }
            if (time != null) {
                if (!time.isInRange(world.getTime())) {
                    world.setTime(time.getStartTick());
                }
            }
        }, 20L, 20L);
    }

    @Override
    public void onStop() {
        setupTask.cancel();
        if (player.isOnline()) {
            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            player.setGameMode(Bukkit.getDefaultGameMode());
            player.setFlying(false);
            player.setAllowFlight(false);
            player.getInventory().clear();
            if (ServerManager.getINSTANCE().getServerType() == ServerType.MULTI_ARENA) {
                InventoryBackup.restoreInventory(player);
                GameSidebarManager.getInstance().setSidebar(player, SidebarType.MULTI_ARENA_LOBBY, null, true);
            }
        }
    }

    @Override
    public void setAllowCommands(boolean toggle) {
        this.allowCommands = toggle;
    }

    @Override
    public boolean canUseCommands() {
        return allowCommands;
    }

    @Override
    public void cacheValue(String identifier, Object value) {
        cachedValues.remove(identifier);
        cachedValues.put(identifier, value);
    }

    @Override
    public void removeCacheValue(String identifier) {
        cachedValues.remove(identifier);
    }

    @Override
    public @Nullable Object getCachedValue(String identifier) {
        return cachedValues.get(identifier);
    }

    @Override
    public void addSetupListener(@NotNull String identifier, @NotNull SetupListener listener) {
        setupListeners.remove(identifier);
        setupListeners.put(identifier, listener);
    }

    @Override
    public void removeSetupListener(@NotNull String identifier) {
        setupListeners.remove(identifier);
    }

    @Override
    public Collection<SetupListener> getSetupListeners() {
        return Collections.unmodifiableCollection(setupListeners.values());
    }

    public void reloadButtonHologram() {
        config.reload();
        if (!config.getProperty(ArenaConfig.MEETING_BUTTON_LOC).isPresent()) return;
        HologramPage page;
        if (meetingButtonHologram != null) {
            meetingButtonHologram.hide();
        }
        // the first page is created automatically
        Location location = config.getProperty(ArenaConfig.MEETING_BUTTON_LOC).get();
        location.setWorld(player.getWorld());
        meetingButtonHologram = new Hologram(location.clone().add(0, 1.3, 0), 2);
        page = meetingButtonHologram.getPage(0);
        // setting first line content
        assert page != null;
        page.setLineContent(0, new LineTextContent((p) -> "&4&lEmergency Meeting Button"));
        page.setLineContent(1, new LineTextContent((p) -> "&fwill be spawned here"));
        SteveSus.newChain().delay(10).sync(()-> {
            meetingButtonHologram.hide(player);
            meetingButtonHologram.show(player);
        }).execute();
    }

    public void setTime(ArenaTime time) {
        this.time = time;
        Bukkit.getWorld(getWorldName()).setTime(time.getStartTick());
    }
}
