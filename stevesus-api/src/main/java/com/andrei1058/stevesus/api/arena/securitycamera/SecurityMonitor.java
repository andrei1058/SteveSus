package com.andrei1058.stevesus.api.arena.securitycamera;

import com.andrei1058.stevesus.api.SteveSusAPI;
import com.andrei1058.stevesus.api.hook.hologram.HologramI;
import com.andrei1058.stevesus.api.hook.hologram.HologramManager;
import com.andrei1058.stevesus.api.locale.Message;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class SecurityMonitor {

//    private static HologramClickListener clickListener;

    private final Location location;
    private @Nullable HologramI hologram;

    public SecurityMonitor(Location location){
        this.location = location;

        var holoManager = HologramManager.getInstance().getProvider();

        if (null != holoManager) {
            this.hologram = holoManager.spawnHologram(location);

            var langManager = SteveSusAPI.getInstance().getLocaleHandler();
            this.hologram.setPageContent(Arrays.asList(
                    player -> langManager.getMsg(player, Message.SECURITY_MONITOR_HOLOGRAM_LINE1),
                    player -> langManager.getMsg(player, Message.SECURITY_MONITOR_HOLOGRAM_LINE2)
            ));
        }

        // fixme
//        if (clickListener == null){
//            clickListener = (player, lineClickType) -> {
//                Arena arena = SteveSusAPI.getInstance().getArenaHandler().getArenaByPlayer(player);
//                if (arena == null) return;
//                if (arena.getCamHandler() == null) return;
//                if (arena.getCamHandler().getCams().isEmpty()) return;
//                if (arena.isSpectator(player)) return;
//                Team playerTeam = arena.getPlayerTeam(player);
//                if (playerTeam == null) return;
//                if (playerTeam.getIdentifier().endsWith("-ghost")) return;
//                arena.getCamHandler().startWatching(player, arena, arena.getCamHandler().getCams().get(0));
//            };
//        }

        // fixme
//        hologram.allowCollisions(true);
//        hologram.setClickListener(clickListener);
    }

    public @Nullable HologramI getHologram() {
        return hologram;
    }

    public Location getLocation() {
        return location;
    }

    public void setHologram(@Nullable HologramI hologram) {
        this.hologram = hologram;
    }
}
