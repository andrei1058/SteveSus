package com.andrei1058.stevesus.arena.ability.vent;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.GameListener;
import com.andrei1058.stevesus.api.arena.team.Team;
import com.andrei1058.stevesus.api.arena.vent.Vent;
import com.andrei1058.stevesus.api.event.PlayerSwitchVentEvent;
import com.andrei1058.stevesus.api.event.PlayerUnVentEvent;
import com.andrei1058.stevesus.api.event.PlayerVentEvent;
import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.api.arena.GameState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class VentListener implements GameListener {

    private static VentListener instance;

    private VentListener() {
    }

    @Override
    public void onPlayerToggleSneakEvent(Arena arena, Player player, boolean isSneaking) {
        if (arena.getVentHandler() == null) return;
        if (isSneaking) {
            if (arena.getVentHandler().isVenting(player)) {
                Vent vent = arena.getVentHandler().unVent(player, SteveSus.getInstance());
                if (vent != null) {
                    Bukkit.getPluginManager().callEvent(new PlayerUnVentEvent(arena, player, vent));
                    for (GameListener listener : arena.getGameListeners()) {
                        listener.onPlayerUnVent(arena, player, vent);
                    }
                }
            } else {
                Vent vent = arena.getVentHandler().startVenting(player, SteveSus.getInstance());
                if (vent != null) {
                    Bukkit.getPluginManager().callEvent(new PlayerVentEvent(arena, player, vent));
                    for (GameListener listener : arena.getGameListeners()) {
                        listener.onPlayerVent(arena, player, vent);
                    }
                }
            }
        }
    }

    @Override
    public void onPlayerInteract(Arena arena, Player player, PlayerInteractEvent event, boolean hasItemInHand) {
        if (!hasItemInHand) return;
        if (arena.getVentHandler() == null) return;
        String tag = CommonManager.getINSTANCE().getItemSupport().getTag(event.getItem(), "nextVent");
        if (tag == null) return;
        event.setCancelled(true);
        Vent vent = arena.getVentHandler().switchVent(player, tag);
        if (vent != null) {
            Bukkit.getPluginManager().callEvent(new PlayerSwitchVentEvent(arena, player, vent));
            for (GameListener listener : arena.getGameListeners()) {
                listener.onPlayerSwitchVent(arena, player, vent);
            }
        }
    }

    @Override
    public void onPlayerInteractEntity(Arena arena, Player player, Entity entity) {
        if (arena.getVentHandler() == null) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) return;
        String tag = CommonManager.getINSTANCE().getItemSupport().getTag(item, "nextVent");
        if (tag == null) return;
        Vent vent = arena.getVentHandler().switchVent(player, tag);
        if (vent != null) {
            Bukkit.getPluginManager().callEvent(new PlayerSwitchVentEvent(arena, player, vent));
            for (GameListener listener : arena.getGameListeners()) {
                listener.onPlayerSwitchVent(arena, player, vent);
            }
        }
    }

    @Override
    public void onGameStateChange(Arena arena, GameState oldState, GameState newState) {
        if (arena.getVentHandler() == null) return;
        if (newState == GameState.IN_GAME) {
            for (Vent vent : arena.getVentHandler().getVents()) {
                vent.getHologram().show();
                for (Team team : arena.getGameTeams()) {
                    if (team.isInnocent()) {
                        team.getMembers().forEach(mem -> vent.getHologram().hide(mem));
                    }
                }
                for (Player spectator : arena.getSpectators()) {
                    vent.getHologram().hide(spectator);
                }
            }
        }
    }

    @Override
    public void onPlayerJoin(Arena arena, Player player) {
        if (arena.getVentHandler() == null) return;
        if (arena.getGameState() == GameState.IN_GAME) {
            for (Vent vent : arena.getVentHandler().getVents()) {
                vent.getHologram().hide(player);
            }
        }
    }

    @Override
    public void onPlayerToSpectator(Arena arena, Player player) {
        if (arena.getVentHandler() == null) return;
        for (Vent vent : arena.getVentHandler().getVents()) {
            vent.getHologram().hide(player);
        }
    }

    @Override
    public void onPlayerMove(Arena arena, Player player, Location from, @Nullable Team playerTeam) {
        if (arena.getVentHandler() == null) return;
        if (arena.getGameState() != GameState.IN_GAME) return;
        if (playerTeam == null) return;
        if (playerTeam.isInnocent()) return;
        for (Vent vent : arena.getVentHandler().getVents()) {
            int distance = (int) player.getLocation().distance(vent.getBlock().getLocation());
            if (vent.getHologram().isHiddenFor(player)) {
                if (distance <= 7) {
                    vent.getHologram().show(player);
                }
            } else {
                if (distance > 7) {
                    vent.getHologram().hide(player);
                }
            }
        }
    }

    public static VentListener getInstance() {
        return instance == null ? instance = new VentListener() : instance;
    }
}
