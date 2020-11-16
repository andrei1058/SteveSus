package com.andrei1058.stevesus.api.server;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;

public class PlayerCoolDown {

    private static final HashMap<UUID, PlayerCoolDown> coolDownByPlayer = new HashMap<>();

    private final HashMap<String, CoolDown> coolDown = new HashMap<>();

    public PlayerCoolDown(Player player) {
        coolDownByPlayer.remove(player.getUniqueId());
        coolDownByPlayer.put(player.getUniqueId(), this);
    }

    @Nullable
    public static PlayerCoolDown getPlayerData(Player player) {
        return coolDownByPlayer.get(player.getUniqueId());
    }

    public static void clearPlayerData(Player player) {
        coolDownByPlayer.remove(player.getUniqueId());
    }

    public boolean hasCoolDown(String key) {
        CoolDown coolDown = this.coolDown.get(key);
        return coolDown != null && coolDown.getNextAllowed() > System.currentTimeMillis();
    }

    public void updateCoolDown(String key, int seconds) {
        CoolDown coolDown = this.coolDown.getOrDefault(key, this.coolDown.put(key, new CoolDown()));
        coolDown.update(seconds);
    }

    public int getCoolDown(String key) {
        CoolDown coolDown = this.coolDown.get(key);
        if (coolDown != null) {
            return (int) ((coolDown.getNextAllowed() - System.currentTimeMillis()) / 1000);
        }
        return 0;
    }

    public void removeKey(String key) {
        this.coolDown.remove(key);
    }

    private static class CoolDown {

        long nextAllowed;

        public long getNextAllowed() {
            return nextAllowed;
        }

        public void update(int seconds) {
            this.nextAllowed = System.currentTimeMillis() + (seconds * 1000);
        }
    }
}
