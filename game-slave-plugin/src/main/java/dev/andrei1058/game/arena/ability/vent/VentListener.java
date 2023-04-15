package dev.andrei1058.game.arena.ability.vent;

import dev.andrei1058.game.SteveSus;
import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.arena.GameListener;
import dev.andrei1058.game.api.arena.team.Team;
import dev.andrei1058.game.api.arena.vent.Vent;
import dev.andrei1058.game.api.event.PlayerSwitchVentEvent;
import dev.andrei1058.game.api.event.PlayerUnVentEvent;
import dev.andrei1058.game.api.event.PlayerVentEvent;
import dev.andrei1058.game.common.CommonManager;
import dev.andrei1058.game.common.api.arena.GameState;
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
    public void onPlayerToggleSneakEvent(GameArena gameArena, Player player, boolean isSneaking) {
        if (gameArena.getVentHandler() == null) return;
        if (isSneaking) {
            if (gameArena.getVentHandler().isVenting(player)) {
                Vent vent = gameArena.getVentHandler().unVent(player, SteveSus.getInstance());
                if (vent != null) {
                    Bukkit.getPluginManager().callEvent(new PlayerUnVentEvent(gameArena, player, vent));
                    for (GameListener listener : gameArena.getGameListeners()) {
                        listener.onPlayerUnVent(gameArena, player, vent);
                    }
                }
            } else {
                Vent vent = gameArena.getVentHandler().startVenting(player, SteveSus.getInstance());
                if (vent != null) {
                    Bukkit.getPluginManager().callEvent(new PlayerVentEvent(gameArena, player, vent));
                    for (GameListener listener : gameArena.getGameListeners()) {
                        listener.onPlayerVent(gameArena, player, vent);
                    }
                }
            }
        }
    }

    @Override
    public void onPlayerInteract(GameArena gameArena, Player player, PlayerInteractEvent event, boolean hasItemInHand) {
        if (!hasItemInHand) return;
        if (gameArena.getVentHandler() == null) return;
        String tag = CommonManager.getINSTANCE().getItemSupport().getTag(event.getItem(), "nextVent");
        if (tag == null) return;
        event.setCancelled(true);
        Vent vent = gameArena.getVentHandler().switchVent(player, tag);
        if (vent != null) {
            Bukkit.getPluginManager().callEvent(new PlayerSwitchVentEvent(gameArena, player, vent));
            for (GameListener listener : gameArena.getGameListeners()) {
                listener.onPlayerSwitchVent(gameArena, player, vent);
            }
        }
    }

    @Override
    public void onPlayerInteractEntity(GameArena gameArena, Player player, Entity entity) {
        if (gameArena.getVentHandler() == null) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) return;
        String tag = CommonManager.getINSTANCE().getItemSupport().getTag(item, "nextVent");
        if (tag == null) return;
        Vent vent = gameArena.getVentHandler().switchVent(player, tag);
        if (vent != null) {
            Bukkit.getPluginManager().callEvent(new PlayerSwitchVentEvent(gameArena, player, vent));
            for (GameListener listener : gameArena.getGameListeners()) {
                listener.onPlayerSwitchVent(gameArena, player, vent);
            }
        }
    }

    @Override
    public void onGameStateChange(GameArena gameArena, GameState oldState, GameState newState) {
        if (gameArena.getVentHandler() == null) return;
        if (newState == GameState.IN_GAME) {
            for (Vent vent : gameArena.getVentHandler().getVents()) {
                vent.getHologram().show();
                for (Team team : gameArena.getGameTeams()) {
                    if (team.isInnocent()) {
                        team.getMembers().forEach(mem -> vent.getHologram().hide(mem));
                    }
                }
                for (Player spectator : gameArena.getSpectators()) {
                    vent.getHologram().hide(spectator);
                }
            }
        }
    }

    @Override
    public void onPlayerJoin(GameArena gameArena, Player player) {
        if (gameArena.getVentHandler() == null) return;
        if (gameArena.getGameState() == GameState.IN_GAME) {
            for (Vent vent : gameArena.getVentHandler().getVents()) {
                vent.getHologram().hide(player);
            }
        }
    }

    @Override
    public void onPlayerToSpectator(GameArena gameArena, Player player) {
        if (gameArena.getVentHandler() == null) return;
        for (Vent vent : gameArena.getVentHandler().getVents()) {
            vent.getHologram().hide(player);
        }
    }

    @Override
    public void onPlayerMove(GameArena gameArena, Player player, Location from, @Nullable Team playerTeam) {
        if (gameArena.getVentHandler() == null) return;
        if (gameArena.getGameState() != GameState.IN_GAME) return;
        if (playerTeam == null) return;
        if (playerTeam.isInnocent()) return;
        for (Vent vent : gameArena.getVentHandler().getVents()) {
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
