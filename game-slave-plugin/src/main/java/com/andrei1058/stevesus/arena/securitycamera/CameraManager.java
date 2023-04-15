package com.andrei1058.stevesus.arena.securitycamera;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.securitycamera.CamHandler;
import com.andrei1058.stevesus.api.arena.securitycamera.SecurityCam;
import com.andrei1058.stevesus.api.arena.securitycamera.SecurityMonitor;
import com.andrei1058.stevesus.api.arena.team.Team;
import com.andrei1058.stevesus.api.server.multiarena.InventoryBackup;
import com.andrei1058.stevesus.arena.ArenaManager;
import com.andrei1058.stevesus.commanditem.CommandItemsManager;
import com.andrei1058.stevesus.server.ServerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CameraManager implements CamHandler {

    private final LinkedList<SecurityCam> cams = new LinkedList<>();
    private final List<SecurityMonitor> monitors = new ArrayList<>();

    private final HashMap<UUID, SecurityCam> onCameras = new HashMap<>();
    private final HashMap<UUID, Player> clones = new HashMap<>();
    private final HashMap<UUID, InventoryBackup> inventories = new HashMap<>();
    private boolean sabotaged = false;

    public CameraManager(List<SecurityCam> cameras, List<SecurityMonitor> monitors, Arena arena) {
        cams.addAll(cameras);
        this.monitors.addAll(monitors);
        arena.registerGameListener(SecurityListener.getInstance());
    }

    @Override
    public boolean startWatching(Player player, Arena arena, SecurityCam cam) {
        if (!arena.isPlayer(player)) return false;
        Team team = arena.getPlayerTeam(player);
        if (team == null) return false;
        if (team.getIdentifier().endsWith("-ghost")) return false;
        player.getInventory().setHeldItemSlot(4);
        if (isOnCam(player, arena)) {
            SecurityCam previous = onCameras.remove(player.getUniqueId());
            if (previous != null) {
                if (onCameras.values().stream().noneMatch(occurrence -> occurrence.equals(previous))) {
                    previous.getHolder().setHelmet(SecurityCam.getNotInUse());
                }
            }
            onCameras.put(player.getUniqueId(), cam);
        } else {
            onCameras.put(player.getUniqueId(), cam);
            Player clone = ServerManager.getINSTANCE().getPlayerNPCSupport().spawnNPC(player.getLocation(), player, true);
            clones.put(player.getUniqueId(), clone);
            inventories.put(player.getUniqueId(), new InventoryBackup(player));
            player.setAllowFlight(true);
            player.setFlying(true);
            player.getInventory().clear();
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 2, false, false));
            arena.setCantMove(player, true);
            CommandItemsManager.sendCommandItems(player, CommandItemsManager.CATEGORY_ON_CAM);
        }
        cam.getHolder().setHelmet(SecurityCam.getInUse());
        player.teleport(cam.getHolder().getLocation().clone().subtract(0, 0.5, 0), PlayerTeleportEvent.TeleportCause.PLUGIN);
        return true;
    }

    @Override
    public void stopWatching(Player player, Arena arena) {
        Player clone = clones.remove(player.getUniqueId());
        SecurityCam camera = onCameras.remove(player.getUniqueId());
        if (clone != null) {
            player.teleport(clone, PlayerTeleportEvent.TeleportCause.PLUGIN);
            player.setFlying(false);
            player.setAllowFlight(false);
            clone.damage(Integer.MAX_VALUE);
            clone.remove();
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
            arena.setCantMove(player, false);
            ServerManager.getINSTANCE().getPlayerNPCSupport().sendDestroyPacket(clone, arena.getPlayers());
            ServerManager.getINSTANCE().getPlayerNPCSupport().sendDestroyPacket(clone, arena.getSpectators());
        }
        if (camera != null) {
            if (onCameras.values().stream().noneMatch(cam -> cam.equals(camera))) {
                camera.getHolder().setHelmet(SecurityCam.getNotInUse());
            }
        }
        InventoryBackup inventoryBackup = inventories.remove(player.getUniqueId());
        if (inventoryBackup != null) {
            inventoryBackup.restore(player);
        }
    }

    @Override
    public boolean isOnCam(Player player, Arena arena, SecurityCam cam) {
        return onCameras.containsKey(player.getUniqueId());
    }

    @Override
    public boolean isOnCam(Player player, Arena arena) {
        return onCameras.containsKey(player.getUniqueId());
    }

    @Override
    public @Nullable SecurityCam getPlayerCam(Player player) {
        return onCameras.get(player.getUniqueId());
    }

    @Override
    public List<SecurityCam> getCams() {
        return cams;
    }

    public List<SecurityMonitor> getMonitors() {
        return monitors;
    }

    @Override
    public List<UUID> getPlayersOnCams() {
        return new ArrayList<>(onCameras.keySet());
    }

    @Override
    public void setSabotaged(boolean toggle) {
        this.sabotaged = toggle;
        if (toggle) {
            for (UUID uuid : getPlayersOnCams()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer(player);
                    if (arena != null) {
                        stopWatching(player, arena);
                    }
                }
            }
        }
    }

    @Override
    public boolean isSabotaged() {
        return sabotaged;
    }

    @Override
    public Collection<Player> getClones() {
        return clones.values();
    }

    @Override
    public @Nullable Player getClone(UUID player) {
        return clones.get(player);
    }

    @Override
    public void nextCam(Player player, Arena arena) {
        if (!isOnCam(player, arena)) return;
        SecurityCam currentCam = getPlayerCam(player);
        if (cams.isEmpty()) return;
        if (currentCam == null) return;
        // get cam position
        int entry = 0;
        for (SecurityCam cam : cams) {
            entry++;
            if (currentCam.equals(cam)) {
                break;
            }
        }
        SecurityCam nextCam = cams.get(entry >= cams.size() ? 0 : entry);
        if (nextCam == null) return;
        startWatching(player, arena, nextCam);
    }

    @Override
    public void previousCam(Player player, Arena arena) {
        if (!isOnCam(player, arena)) return;
        SecurityCam currentCam = getPlayerCam(player);
        if (cams.isEmpty()) return;
        if (currentCam == null) return;
        // get cam position
        int entry = 0;
        for (SecurityCam cam : cams) {
            entry++;
            if (currentCam.equals(cam)) {
                break;
            }
        }
        entry -= 2;
        SecurityCam nextCam = cams.get(entry < 0 ? cams.size() - 1 : entry >= cams.size() ? 0 : entry);
        if (nextCam == null) return;
        startWatching(player, arena, nextCam);
    }
}
