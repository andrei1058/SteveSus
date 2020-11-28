package com.andrei1058.stevesus.api.arena.vent;

import com.andrei1058.stevesus.api.SteveSusAPI;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.room.GameRoom;
import com.andrei1058.stevesus.api.arena.team.Team;
import com.andrei1058.stevesus.api.locale.Locale;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.api.server.multiarena.InventoryBackup;
import com.andrei1058.stevesus.common.api.arena.GameState;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Openable;
import org.bukkit.material.TrapDoor;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("unused")
public class VentHandler {

    private final Arena arena;
    private final LinkedList<Vent> vents = new LinkedList<>();
    private final HashMap<UUID, InventoryBackup> currentlyVenting = new HashMap<>();

    public VentHandler(Arena arena, LinkedList<Vent> vents) {
        this.arena = arena;
        this.vents.addAll(vents);
    }


    /**
     * Check if the given player is currently venting.
     */
    public boolean isVenting(@NotNull Player player) {
        return currentlyVenting.containsKey(player.getUniqueId());
    }

    /**
     * Start venting.
     * Will try venting at player's location.
     * Innocents cannot vent.
     *
     * @return true if vented successfully.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Vent startVenting(@NotNull Player player, Plugin plugin) {
        if (arena.getGameState() != GameState.IN_GAME) return null;
        if (isVenting(player)) return null;
        Vent vent = getVent(player.getLocation().getBlock());
        if (vent == null) return null;
        Team team = arena.getPlayerTeam(player);
        if (team == null) return null;
        if (team.isInnocent()) return null;

        if (vent.getBlock().getState().getData() instanceof Openable) {
            BlockState state = vent.getBlock().getState();
            TrapDoor door = (TrapDoor) state.getData();
            door.setOpen(true);
            state.setData(door);
            state.update(true, true);

            vent.getBlock().getWorld().playEffect(vent.getBlock().getLocation(), Effect.TRAPDOOR_TOGGLE, 3);

            //todo play trap open
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                BlockState state2 = vent.getBlock().getState();
                TrapDoor door1 = (TrapDoor) state2.getData();
                door1.setOpen(false);
                state2.setData(door1);
                state2.update(true, true);
                vent.getBlock().getWorld().playEffect(vent.getBlock().getLocation(), Effect.TRAPDOOR_TOGGLE, 3);
                //todo play trap close
            }, 30L);
        }

        InventoryBackup inventoryBackup = new InventoryBackup(player);
        arena.setCantMove(player, true);
        sendItems(player, vent);
        player.teleport(vent.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
        player.sendTitle(" ", SteveSusAPI.getInstance().getLocaleHandler().getMsg(player, Message.VENT_ENTER_SUBTITLE), 0, 30, 0);

        currentlyVenting.put(player.getUniqueId(), inventoryBackup);
        if (arena.getSabotageCooldown() != null){
            arena.getSabotageCooldown().tryPause();
        }
        //todo re-apply kill cooldown
        return vent;
    }

    private void sendItems(Player player, Vent currentVent) {
        Locale lang = SteveSusAPI.getInstance().getLocaleHandler().getLocale(player);
        player.getInventory().clear();
        for (Vent vent : currentVent.getConnections()) {
            ItemStack item = vent.getDisplayItem().clone();
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                GameRoom room = arena.getRoom(vent.getBlock().getLocation());
                meta.setDisplayName(room == null ? lang.getMsg(null, Message.GAME_ROOM_NO_NAME) : room.getDisplayName(lang));
                item.setItemMeta(meta);
            }
            player.getInventory().addItem(item);
        }
    }

    @Nullable
    public Vent unVent(@NotNull Player player, Plugin plugin) {
        if (!isVenting(player)) return null;
        Vent vent = getVent(player.getLocation().getBlock());
        if (vent == null) return null;

        if (vent.getBlock().getState().getData() instanceof Openable) {
            BlockState state = vent.getBlock().getState();
            TrapDoor door = (TrapDoor) state.getData();
            door.setOpen(true);
            state.setData(door);
            state.update(true, true);

            vent.getBlock().getWorld().playEffect(vent.getBlock().getLocation(), Effect.TRAPDOOR_TOGGLE, 3);

            //todo play trap open
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                BlockState state2 = vent.getBlock().getState();
                TrapDoor door1 = (TrapDoor) state2.getData();
                door1.setOpen(false);
                state2.setData(door1);
                state2.update(true, true);
                vent.getBlock().getWorld().playEffect(vent.getBlock().getLocation(), Effect.TRAPDOOR_TOGGLE, 3);
                //todo play trap close
            }, 30L);
        }

        Location loc = vent.getLocation().clone();
        loc.add(0, 1, 0);
        loc.setPitch(player.getLocation().getPitch());
        loc.setYaw(player.getLocation().getPitch());
        player.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
        // todo send impostor items
        player.getInventory().clear();
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        arena.setCantMove(player, false);
        currentlyVenting.remove(player.getUniqueId()).restore(player);
        if (arena.getSabotageCooldown() != null){
            arena.getSabotageCooldown().tryUnPause();
            arena.getSabotageCooldown().updateCooldownOnItems(player, player.getInventory());
        }
        return vent;
    }

    /**
     * If the given player is venting, move to next vent.
     */
    public Vent switchVent(Player player, String nextVent) {
        if (!isVenting(player)) return null;
        Vent vent = getVent(nextVent);
        if (vent == null) return null;
        player.teleport(vent.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        player.sendTitle(" ", SteveSusAPI.getInstance().getLocaleHandler().getMsg(player, Message.VENT_ENTER_SUBTITLE), 0, 30, 0);
        sendItems(player, vent);
        // todo play sound
        return vent;
    }

    /**
     * Interrupt venting before teleporting the player on emergency meetings.
     * Used as well on player disconnect etc.
     * This should only un-hide the player.
     */
    public void interruptVenting(@NotNull Player player, boolean disconnect) {
        if (!isVenting(player)) return;
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        InventoryBackup inventoryBackup = currentlyVenting.remove(player.getUniqueId());
        // re-apply color on meetings
        if (!disconnect) {
            if (inventoryBackup != null){
                inventoryBackup.restore(player);
            }
        }
        if (arena.getSabotageCooldown() != null){
            arena.getSabotageCooldown().tryUnPause();
            arena.getSabotageCooldown().updateCooldownOnItems(player, player.getInventory());
        }
    }

    public boolean isVent(Block block) {
        return getVent(block) != null;
    }

    /**
     * Get vent at given location.
     */
    public @Nullable Vent getVent(Block block) {
        for (Vent vent : vents) {
            if (vent.isThis(block)) {
                return vent;
            }
        }
        return null;
    }

    /**
     * Vents list.
     */
    public LinkedList<Vent> getVents() {
        return vents;
    }

    /**
     * List of venting players.
     */
    public Set<UUID> getCurrentlyVenting() {
        return currentlyVenting.keySet();
    }

    /**
     * Add a vent.
     */
    public void addVent(Vent vent) {
        this.vents.add(vent);
    }

    /**
     * Remove vent.
     */
    public void removeVent(Vent vent) {
        this.vents.remove(vent);
    }

    /**
     * Get vent by identifier.
     */
    public Vent getVent(String identifier) {
        for (Vent vent : vents) {
            if (vent.getIdentifier().equals(identifier)) {
                return vent;
            }
        }
        return null;
    }
}
