package com.andrei1058.stevesus.api.arena;

import com.andrei1058.hologramapi.Hologram;
import com.andrei1058.hologramapi.HologramPage;
import com.andrei1058.hologramapi.content.LineTextContent;
import com.andrei1058.stevesus.api.SteveSusAPI;
import com.andrei1058.stevesus.api.arena.team.Team;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.common.api.arena.GameState;
import com.andrei1058.stevesus.common.gui.ItemUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class MeetingButton {

    // use this to identify armor stand entity
    public static final String MEETING_BUTTON_META_DATA_KEY = "mbtnss";

    private final Location buttonLocation;
    private final Hologram buttonHologram;
    private Instant lastUsage;
    private int coolDown;
    private List<Location> particleLocations;
    private int currentEntry = -1;

    public MeetingButton(Plugin plugin, Location location, Arena arena, int coolDown) {
        this.buttonLocation = location;
        this.buttonLocation.setX(location.getBlockX() + 0.5);
        this.buttonLocation.setZ(location.getBlockZ() + 0.5);
        this.coolDown = coolDown;
        ArmorStand buttonKeeper = location.getWorld().spawn(location.clone().subtract(0, 1.5, 0), ArmorStand.class);
        buttonKeeper.setRemoveWhenFarAway(false);
        buttonKeeper.setVisible(false);
        buttonKeeper.setGravity(false);
        ItemStack meetingHead = ItemUtil.createSkullWithSkin("https://textures.minecraft.net/texture/c73fb2a28f3b665486412db7b66cdb7fc5c7d33d74e850b94722dd3d14aaa", "Emergency Meeting");
        buttonKeeper.setHelmet(meetingHead);
        buttonKeeper.setMetadata(MEETING_BUTTON_META_DATA_KEY, new FixedMetadataValue(plugin, arena.getGameId()));

        buttonHologram = new Hologram(location.clone().add(0, 1.5, 0), 3);
        HologramPage page = buttonHologram.getPage(0);
        assert page != null;
        page.setLineContent(0, new LineTextContent(s -> SteveSusAPI.getInstance().getLocaleHandler().getMsg(s, Message.EMERGENCY_BUTTON_HOLO1)));
        page.setLineContent(1, new LineTextContent(s -> arena.getMeetingStage() == MeetingStage.NO_MEETING ? SteveSusAPI.getInstance().getLocaleHandler().getMsg(s, Message.EMERGENCY_BUTTON_HOLO2) : " "));
        page.setLineContent(2, new LineTextContent(s -> {
            if (arena.getMeetingStage() == MeetingStage.TALKING) {
                return SteveSusAPI.getInstance().getLocaleHandler().getMsg(s, Message.EMERGENCY_BUTTON_STATUS_VOTING_STARTS_IN).replace("{time}", String.valueOf(arena.getCountdown()));
            } else if (arena.getMeetingStage() == MeetingStage.VOTING) {
                return SteveSusAPI.getInstance().getLocaleHandler().getMsg(s, Message.EMERGENCY_BUTTON_STATUS_VOTING_ENDS_IN).replace("{time}", String.valueOf(arena.getCountdown()));
            }
            return SteveSusAPI.getInstance().getLocaleHandler().getMsg(s, Message.EMERGENCY_BUTTON_STATUS_YOUR_MEETINGS_LEFT).replace("{amount}", String.valueOf(arena.getMeetingsLeft(s)));
        }));

        this.particleLocations = getCircle(this.buttonLocation.clone().add(0, 0.3, 0), 0.6, 15);
    }

    /**
     * Refresh lines during meetings.
     */
    public void refreshLines(Arena arena) {
        if (buttonHologram != null) {
            arena.getPlayers().forEach(buttonHologram::refreshLines);
        }
        for (int i = 0; i < 5; i++){
            Location loc = particleLocations.get(particleLocations.size() == ++currentEntry ? currentEntry = 0 : currentEntry);
            loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 0, 1, 1, 0, 1);
        }
    }

    /**
     * Used when a player interacts with button holder entity.
     */
    public void onClick(Player player, Arena arena) {
        if (arena.getMeetingStage() != MeetingStage.NO_MEETING) return;
        if (arena.getGameState() != GameState.IN_GAME) return;
        Team playerTeam = arena.getPlayerTeam(player);
        if (playerTeam == null) return;
        if (!playerTeam.canUseMeetingButton()) {
            return;
        }
        // check cool down
        if (lastUsage != null && lastUsage.plusSeconds(coolDown).isBefore(Instant.now())) {
            player.sendMessage(SteveSusAPI.getInstance().getLocaleHandler().getMsg(player, Message.EMERGENCY_DENIED_COOL_DOWN).replace("{time}", String.valueOf(Instant.now().until(lastUsage.plusSeconds(coolDown), ChronoUnit.SECONDS))));
            return;
        }
        // check usage limit per player
        if (arena.getMeetingsLeft(player) == 0) {
            player.sendMessage(SteveSusAPI.getInstance().getLocaleHandler().getMsg(player, Message.EMERGENCY_DENIED_NO_MEETINGS_LEFT));
            return;
        }

        if (!arena.startMeeting(player, null)) {
            return;
        }

        lastUsage = Instant.now();
    }

    /**
     * Set button cool down.
     */
    public void setCoolDown(int coolDown) {
        this.coolDown = coolDown;
    }

    public ArrayList<Location> getCircle(Location center, double radius, int amount) {
        World world = center.getWorld();
        double increment = (2 * Math.PI) / amount;
        ArrayList<Location> locations = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            double angle = i * increment;
            double x = center.getX() + (radius * Math.cos(angle));
            double z = center.getZ() + (radius * Math.sin(angle));
            locations.add(new Location(world, x, center.getY(), z));
        }
        return locations;
    }
}