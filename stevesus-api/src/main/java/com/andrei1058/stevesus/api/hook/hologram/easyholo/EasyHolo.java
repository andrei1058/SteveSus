package com.andrei1058.stevesus.api.hook.hologram.easyholo;

import com.andrei1058.stevesus.api.hook.hologram.HologramI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.holoeasy.line.TextLine;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class EasyHolo implements HologramI {

    private HashMap<UUID, PerPlayer> perPlayer = new HashMap<>();
    private Location location;
    private List<Function<Player, String>> pageContent = new ArrayList<>();

    public EasyHolo(@NotNull Location location) {
        this.location = location;
    }

    @Override
    public void setPageContent(List<Function<Player, String>> pageContent) {
        this.pageContent = pageContent;
        if (!perPlayer.isEmpty()) {
            perPlayer.forEach((uuid, holo) -> {
                var player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    updateLines(player, holo);
                }
            });
        }
    }

    @Override
    public void hideToAll() {
        perPlayer.forEach((uuid, holo) -> {
            holo.hide();
        });
    }

    @Override
    public void refreshLines(@NotNull Player player) {
        var playerHolo = perPlayer.get(player.getUniqueId());

        if (null == playerHolo) {
            return;
        }

        updateLines(player, playerHolo);
    }

    @Override
    public void refreshForAll() {
        for (var perPlayer : perPlayer.values()) {
            perPlayer.getPvt().getSeeingPlayers().forEach(pvt -> {
                updateLines(pvt, perPlayer);
            });
        }
    }

    @Override
    public void unHide() {
        for (var entry : perPlayer.entrySet()) {
            var player = Bukkit.getPlayer(entry.getKey());
            if (null == player) {
                perPlayer.remove(entry.getKey());
            } else {
                entry.getValue().show(player);
            }
        }
    }

    @Override
    public void showToPlayer(@NotNull Player player) {
        var playerHolo = perPlayer.get(player.getUniqueId());
        if (playerHolo == null) {
            playerHolo = createForPlayer(player);
        }
        playerHolo.show(player);
    }

    private @NotNull PerPlayer createForPlayer(@NotNull Player player) {
        var holo = new PerPlayer(this.location);
        updateLines(player, holo);
        perPlayer.put(player.getUniqueId(), holo);
        return holo;
    }

    private void updateLines(@NotNull Player player, @NotNull PerPlayer hologram) {
        hologram.getLines().clear();
        this.pageContent.forEach(function -> {
            hologram.getLines().add(new TextLine(hologram, function.apply(player), null, false));
        });
    }

    @Override
    public void hideFromPlayer(@NotNull Player player) {
        var playerHolo = perPlayer.get(player.getUniqueId());
        if (playerHolo == null) {
            return;
        }
        playerHolo.hide(player);
        perPlayer.remove(player.getUniqueId());
    }

    @Override
    public void remove() {
        perPlayer.values().forEach(holo -> {
            holo.getPvt().getSeeingPlayers().forEach(holo::hide);
            holo.getLines().clear();
            holo.getLocation().setWorld(null);
        });
        perPlayer.clear();
    }

    @Override
    public boolean isHiddenFor(@NotNull Player player) {
        var holo = perPlayer.get(player.getUniqueId());
        if (null == holo) {
            return true;
        }
        return !holo.isShownFor(player);
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public void destroyPlayer(@NotNull Player player) {
        perPlayer.remove(player.getUniqueId());
    }
}
