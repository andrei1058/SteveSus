package com.andrei1058.stevesus.hook.glowing;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.glow.GlowColor;
import com.andrei1058.stevesus.api.glow.GlowingHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.inventivetalent.glow.GlowAPI;
import org.jetbrains.annotations.NotNull;

public class GlowingManager implements GlowingHandler {

    private static boolean glowingAPI = false;
    private static GlowingManager instance;

    private GlowingManager() {
        instance = this;
    }

    public static GlowingManager getInstance() {
        return instance == null ? instance = new GlowingManager() : instance;
    }

    public static void init() {
        if (Bukkit.getPluginManager().getPlugin("GlowAPI") != null) {
            glowingAPI = true;
        }
    }

    public static void setGlowingRed(@NotNull Entity player, @NotNull Player receiver, Arena arena) {
        if (!isGlowing(player, receiver)) {
            GlowAPI.setGlowing(player, GlowAPI.Color.RED, "never", "never", receiver);
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

    public static void setGlowingYellow(@NotNull Entity player, @NotNull Player receiver) {
        if (!isGlowing(player, receiver)) {
            GlowAPI.setGlowing(player, GlowAPI.Color.YELLOW, "never", "never", receiver);
        }
    }

    public static void setGlowingBlue(@NotNull Entity player, @NotNull Player receiver) {
        if (!isGlowing(player, receiver)) {
            GlowAPI.setGlowing(player, GlowAPI.Color.BLUE, "never", "never", receiver);
        }
    }

    public void removeGlowing(@NotNull Entity player, @NotNull Player receiver) {
        if (isGlowing(player, receiver)) {
            GlowAPI.setGlowing(player, null, "never", "never", receiver);
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
                SteveSus.newChain().delay(1).sync(()-> GlowAPI.setGlowing(player, null, "never", "never", receiver)).execute();
            }
        }
    }

    @Override
    public void setGlowing(@NotNull Entity player, @NotNull Player receiver, GlowColor color) {
        if (!isGlowing(player, receiver)){
            GlowAPI.setGlowing(player, getLegacyColor(color), "never", "never", receiver);
        }
    }

    private GlowAPI.Color getLegacyColor(GlowColor color){
       try {
           return GlowAPI.Color.valueOf(color.name().toUpperCase());
       } catch (Exception ex){
           return GlowAPI.Color.WHITE;
       }
    }
}
