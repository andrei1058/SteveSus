package com.andrei1058.amongusmc.setup;

import ch.jalu.configme.SettingsManager;
import com.andrei1058.amongusmc.AmongUsMc;
import com.andrei1058.amongusmc.api.server.ServerType;
import com.andrei1058.amongusmc.api.setup.SetupSession;
import com.andrei1058.amongusmc.arena.ArenaManager;
import com.andrei1058.amongusmc.common.command.CommonCmdManager;
import com.andrei1058.amongusmc.server.ServerManager;
import com.andrei1058.amongusmc.server.multiarena.InventoryBackup;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

public class SetupActivity implements SetupSession {

    private final Player player;
    private final String worldName;
    private SettingsManager config;

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
        player.teleport(world.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        player.setGameMode(GameMode.CREATIVE);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.getInventory().clear();
        boolean up = false;
        if (player.getLocation().getY() <= 0) {
            up = true;
            player.teleport(player.getLocation().add(0, 10, 0), PlayerTeleportEvent.TeleportCause.PLUGIN);
        }
        player.sendMessage(ChatColor.GRAY + "Teleported to " + ChatColor.GREEN + getWorldName() + ChatColor.GRAY + "'s spawn point" + (up ? "(+ 10 y)." : "."));

        Bukkit.getScheduler().runTaskLater(AmongUsMc.getInstance(), () -> Bukkit.dispatchCommand(player, CommonCmdManager.getINSTANCE().getMainCmd().getName()), 60L);
    }

    @Override
    public void onStop() {
        if (player.isOnline()) {
            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            player.setGameMode(Bukkit.getDefaultGameMode());
            player.setFlying(false);
            player.setAllowFlight(false);
            player.getInventory().clear();
            if (ServerManager.getINSTANCE().getServerType() == ServerType.MULTI_ARENA) {
                InventoryBackup.restoreInventory(player);
            }
        }
    }
}
