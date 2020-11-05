package com.andrei1058.stevesus.setup;

import ch.jalu.configme.SettingsManager;
import com.andrei1058.hologramapi.Hologram;
import com.andrei1058.hologramapi.HologramPage;
import com.andrei1058.hologramapi.content.LineTextContent;
import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.server.ServerType;
import com.andrei1058.stevesus.api.setup.SetupSession;
import com.andrei1058.stevesus.arena.ArenaHandler;
import com.andrei1058.stevesus.common.command.CommonCmdManager;
import com.andrei1058.stevesus.config.ArenaConfig;
import com.andrei1058.stevesus.server.ServerManager;
import com.andrei1058.stevesus.server.multiarena.InventoryBackup;
import com.andrei1058.stevesus.sidebar.GameSidebarManager;
import com.andrei1058.stevesus.sidebar.SidebarType;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;

public class SetupActivity implements SetupSession {

    private final Player player;
    private final String worldName;
    private SettingsManager config;
    private Hologram meetingButtonHologram;
    private BukkitTask setupTask;

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
        config = ArenaHandler.getINSTANCE().getTemplate(worldName, true);
        if (ServerManager.getINSTANCE().getServerType() == ServerType.MULTI_ARENA) {
            InventoryBackup.createInventoryBackup(player);
        }
        world.setWeatherDuration(0);
        world.setThunderDuration(0);
        world.setStorm(false);
        world.setThundering(false);
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
        reloadButtonHologram();

        setupTask = Bukkit.getScheduler().runTaskTimer(SteveSus.getInstance(), () -> {
            // better use this here than in a move event since this gets cancelled at a certain point
            if (player.getLocation().getY() < 0){
                player.teleport(player.getWorld().getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
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
        meetingButtonHologram = new Hologram(location, 2);
        page = meetingButtonHologram.getPage(0);
        // setting first line content
        assert page != null;
        page.setLineContent(0, new LineTextContent((p) -> "&4&lEmergency Meeting Button"));
        page.setLineContent(1, new LineTextContent((p) -> "&fwill be spawned here"));
    }
}
