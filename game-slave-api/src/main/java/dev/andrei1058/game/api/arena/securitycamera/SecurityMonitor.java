package dev.andrei1058.game.api.arena.securitycamera;

import com.andrei1058.hologramapi.Hologram;
import com.andrei1058.hologramapi.HologramPage;
import com.andrei1058.hologramapi.content.HologramClickListener;
import com.andrei1058.hologramapi.content.LineTextContent;
import dev.andrei1058.game.api.SteveSusAPI;
import dev.andrei1058.game.api.arena.Arena;
import dev.andrei1058.game.api.arena.team.Team;
import dev.andrei1058.game.api.locale.Message;
import org.bukkit.Location;

public class SecurityMonitor {

    private static HologramClickListener clickListener;

    private final Location location;
    private Hologram hologram;

    public SecurityMonitor(Location location){
        this.location = location;
        this.hologram = new Hologram(location, 2);
        HologramPage page = hologram.getPage(0);
        assert page != null;
        page.setLineContent(0, new LineTextContent(player -> SteveSusAPI.getInstance().getLocaleHandler().getMsg(player, Message.SECURITY_MONITOR_HOLOGRAM_LINE1)));
        page.setLineContent(1, new LineTextContent(player -> SteveSusAPI.getInstance().getLocaleHandler().getMsg(player, Message.SECURITY_MONITOR_HOLOGRAM_LINE2)));

        if (clickListener == null){
            clickListener = (player, lineClickType) -> {
                Arena arena = SteveSusAPI.getInstance().getArenaHandler().getArenaByPlayer(player);
                if (arena == null) return;
                if (arena.getCamHandler() == null) return;
                if (arena.getCamHandler().getCams().isEmpty()) return;
                if (arena.isSpectator(player)) return;
                Team playerTeam = arena.getPlayerTeam(player);
                if (playerTeam == null) return;
                if (playerTeam.getIdentifier().endsWith("-ghost")) return;
                arena.getCamHandler().startWatching(player, arena, arena.getCamHandler().getCams().get(0));
            };
        }

        hologram.allowCollisions(true);
        hologram.setClickListener(clickListener);
    }

    public Hologram getHologram() {
        return hologram;
    }

    public Location getLocation() {
        return location;
    }

    public void setHologram(Hologram hologram) {
        this.hologram = hologram;
    }
}
