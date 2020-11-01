package com.andrei1058.amongusmc.server.multiarena.listener;

import com.andrei1058.amongusmc.AmongUsMc;
import com.andrei1058.amongusmc.config.MainConfig;
import com.andrei1058.amongusmc.commanditem.InventoryUtil;
import com.andrei1058.amongusmc.commanditem.JoinItemsManager;
import com.andrei1058.amongusmc.server.ServerManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.UUID;

public class LobbyProtectionListener implements Listener {

    private static boolean initialized = false;
    private static String lobbyWorld;
    private static final LinkedList<UUID> allowedToBuild = new LinkedList<>();

    private LobbyProtectionListener() {
        AmongUsMc.debug("Registered listener: " + getClass().getSimpleName() + ".");
    }

    /**
     * Initialize multi-arena lobby protect listener.
     */
    public static void init(boolean worldNameChange) {
        if (!initialized) {
            ServerManager.getINSTANCE().getConfig().reload();
            Location mainLobby = ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.MULTI_ARENA_SPAWN_LOC);
            if (mainLobby != null && mainLobby.getWorld() != null) {
                initialized = true;
                lobbyWorld = mainLobby.getWorld().getName();
                Bukkit.getPluginManager().registerEvents(new LobbyProtectionListener(), AmongUsMc.getInstance());
                mainLobby.getWorld().getEntities().forEach(entity -> {
                    if (entity instanceof Creature) {
                        entity.remove();
                    }
                });
            }
        } else if (worldNameChange) {
            // update lobby to protect
            lobbyWorld = ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.MULTI_ARENA_SPAWN_LOC).getWorld().getName();
        }
    }

    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Get multi-arena lobby world name.
     */
    @Nullable
    public static String getLobbyWorld() {
        return lobbyWorld;
    }

    /**
     * Give access to build.
     */
    public static void addBuilder(UUID uuid) {
        if (!isBuilder(uuid)) allowedToBuild.add(uuid);
    }

    /**
     * Remove build access.
     */
    public static void removeBuilder(UUID uuid) {
        allowedToBuild.remove(uuid);
    }

    /**
     * Check if a player is allowed to build in the main lobby.
     */
    public static boolean isBuilder(UUID uuid) {
        return allowedToBuild.contains(uuid);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled()) return;
        if (e.getBlock().getWorld().getName().equals(getLobbyWorld())) {
            if (!isBuilder(e.getPlayer().getUniqueId())) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onIceMelt(BlockFadeEvent e) {
        if (e.isCancelled()) return;
        if (e.getBlock().getWorld().getName().equals(getLobbyWorld())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.isCancelled()) return;
        if (e.getBlock().getWorld().getName().equals(getLobbyWorld())) {
            if (!isBuilder(e.getPlayer().getUniqueId())) e.setCancelled(true);
        }
    }

    // interact prevention is superficial
    @EventHandler
    public void onBreakFire(PlayerInteractEvent e) {
        if (e.isCancelled()) return;
        if (e.getPlayer().getWorld().getName().equals(getLobbyWorld())) {
            // prevent fire removal
            if (e.getClickedBlock().getRelative(BlockFace.UP).getType() == Material.FIRE) {
                if (!isBuilder(e.getPlayer().getUniqueId())) e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent e) {
        if (e.isCancelled()) return;
        if (e.getBlockClicked().getWorld().getName().equals(getLobbyWorld())) {
            if (!isBuilder(e.getPlayer().getUniqueId())) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        if (e.isCancelled()) return;
        if (e.getBlockClicked().getWorld().getName().equals(getLobbyWorld())) {
            if (!isBuilder(e.getPlayer().getUniqueId())) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        if (e.isCancelled()) return;
        if (e.getLocation().getWorld().getName().equals(getLobbyWorld())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e) {
        if (e.isCancelled()) return;
        if (e.getBlock().getWorld().getName().equals(getLobbyWorld())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPaintingRemove(HangingBreakByEntityEvent e) {
        if (e.isCancelled()) return;
        if (e.getEntity().getLocation().getWorld().getName().equals(getLobbyWorld())) {
            if (!isBuilder(e.getRemover().getUniqueId())) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        if (e.isCancelled()) return;
        if (e.getBlock().getWorld().getName().equals(getLobbyWorld())) {
            if (!isBuilder(e.getEntity().getUniqueId())) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (e.isCancelled()) return;
        if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM || e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG)
            return;
        if (e.getLocation().getWorld().getName().equals(getLobbyWorld())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        if (e.isCancelled()) return;
        if (e.getEntity().getType() != EntityType.PLAYER) return;
        if (e.getFoodLevel() > ((Player) e.getEntity()).getFoodLevel()) return;
        if (e.getEntity().getWorld().getName().equals(getLobbyWorld())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamageFromEntity(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        if (e.getEntity().getWorld().getName().equals(getLobbyWorld())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamageFromBlock(EntityDamageByBlockEvent e) {
        if (e.isCancelled()) return;
        if (e.getEntity().getWorld().getName().equals(getLobbyWorld())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.isCancelled()) return;
        if (e.getEntity().getWorld().getName().equals(getLobbyWorld())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onQuit(PlayerQuitEvent e) {
        removeBuilder(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent e) {
        if (e.isCancelled()) return;
        if (e.getPlayer().getLocation().getBlockY() < 0) {
            if (e.getPlayer().getWorld().getName().equals(getLobbyWorld())) {
                Location mainLobby = ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.MULTI_ARENA_SPAWN_LOC);
                e.getPlayer().teleport((mainLobby == null || mainLobby.getWorld() == null) ? Bukkit.getWorlds().get(0).getSpawnLocation() : mainLobby, PlayerTeleportEvent.TeleportCause.PLUGIN);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent e) {
        Location mainLobby = ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.MULTI_ARENA_SPAWN_LOC);
        e.getPlayer().teleport((mainLobby == null || mainLobby.getWorld() == null) ? Bukkit.getWorlds().get(0).getSpawnLocation() : mainLobby, PlayerTeleportEvent.TeleportCause.PLUGIN);
        InventoryUtil.wipePlayer(e.getPlayer());
        JoinItemsManager.sendCommandItems(e.getPlayer(), JoinItemsManager.CATEGORY_MAIN_LOBBY);
    }

    /*@EventHandler
    public void onItemDrop(EntityDropItemEvent e){
        if (e.isCancelled()) return;
        if (e.getEntity().getWorld().getName().equals(getLobbyWorld())){
            e.setCancelled(true);
        }
    }*/

    @EventHandler
    public void onItemPickUp(EntityPickupItemEvent e) {
        if (e.isCancelled()) return;
        if (e.getEntity().getWorld().getName().equals(getLobbyWorld())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrop2(PlayerDropItemEvent e) {
        if (e.isCancelled()) return;
        if (e.getPlayer().getWorld().getName().equals(getLobbyWorld())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onWeather(WeatherChangeEvent e) {
        if (e.isCancelled()) return;
        if (!e.toWeatherState()) return;
        if (e.getWorld().getName().equals(getLobbyWorld())) {
            e.setCancelled(true);
        }
    }
}
