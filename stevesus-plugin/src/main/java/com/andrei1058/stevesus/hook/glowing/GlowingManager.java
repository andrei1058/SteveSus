package com.andrei1058.stevesus.hook.glowing;

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

    public static void setGlowing(@NotNull Entity player, @NotNull Player receiver) {
        if (!isGlowing(player, receiver)) {
            GlowAPI.setGlowing(player, GlowAPI.Color.RED, "never", "never", receiver);
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

    public static boolean isGlowing(@NotNull Entity player, @NotNull Player receiver) {
        return glowingAPI && GlowAPI.isGlowing(player, receiver);
    }
}
