package com.andrei1058.stevesus.hook.glowing;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.inventivetalent.glow.GlowAPI;
import org.jetbrains.annotations.NotNull;

public class GlowingManager {

    private static boolean glowingAPI = false;

    private GlowingManager() {
    }

    public static void init() {
        if (Bukkit.getPluginManager().getPlugin("GlowAPI") != null) {
            glowingAPI = true;
        }
    }

    public static void setGlowingRed(@NotNull Entity player, @NotNull Player receiver, Arena arena) {
        if (!isGlowing(player, receiver)) {
            GlowAPI.setGlowing(player, GlowAPI.Color.RED, "NEVER", "NEVER", receiver);
            for (Player inGame : arena.getPlayers()) {
                sendRemove(player, inGame);
            }
        }
    }

    public static void setGlowingGreen(@NotNull Entity player, @NotNull Player receiver) {
        if (!isGlowing(player, receiver)) {
            GlowAPI.setGlowing(player, GlowAPI.Color.GREEN, "never", "never", receiver);
        }
    }

    public static void setGlowingBlue(@NotNull Entity player, @NotNull Player receiver) {
        if (!isGlowing(player, receiver)) {
            GlowAPI.setGlowing(player, GlowAPI.Color.BLUE, "never", "never", receiver);
        }
    }

    public static void removeGlowing(@NotNull Entity player, @NotNull Player receiver) {
        if (isGlowing(player, receiver)) {
            GlowAPI.setGlowing(player, null, receiver);
        }
    }

    public static boolean isGlowing(Entity player,  Player receiver) {
        if (player == null) return false;
        if (receiver == null) return false;
        return glowingAPI && GlowAPI.isGlowing(player, receiver);
    }

    /**
     * This will remove white glowing on entities because the GlowAPI is a bit buggy.
     */
    public static void sendRemove(@NotNull Entity player, @NotNull Player receiver) {
        if (glowingAPI) {
            GlowAPI.Color glowColor = GlowAPI.getGlowColor(player, receiver);
            if (glowColor == null || glowColor == GlowAPI.Color.WHITE) {
                SteveSus.newChain().delay(1).sync(()-> GlowAPI.setGlowing(player, null, receiver)).execute();
            }
        }
    }
}
