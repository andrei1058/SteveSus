package com.andrei1058.stevesus.arena.sabotage.oxygen;

import com.andrei1058.hologramapi.Hologram;
import com.andrei1058.hologramapi.HologramPage;
import com.andrei1058.hologramapi.content.LineTextContent;
import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.GameListener;
import com.andrei1058.stevesus.api.arena.meeting.MeetingStage;
import com.andrei1058.stevesus.api.arena.sabotage.*;
import com.andrei1058.stevesus.api.arena.team.Team;
import com.andrei1058.stevesus.api.event.GameSabotageActivateEvent;
import com.andrei1058.stevesus.api.event.GameSabotageDeactivateEvent;
import com.andrei1058.stevesus.api.glow.GlowColor;
import com.andrei1058.stevesus.api.glow.GlowingBox;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.common.gui.ItemUtil;
import com.andrei1058.stevesus.language.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class OxygenSabotage extends SabotageBase implements TimedSabotage {

    //private static ItemStack standardFace = null;
    private static final LinkedList<ItemStack> errorFaces = new LinkedList<>();
    private static final LinkedList<ItemStack> pulseFaces = new LinkedList<>();


    private final Arena arena;
    private final GenericWarning warning;
    private int deadLineSeconds;
    private boolean active = false;
    private final List<OxygenMonitor> monitors = new ArrayList<>();

    /**
     * @param fixLocations where to spawn fix monitor.
     */
    public OxygenSabotage(Arena arena, int deadLineSeconds, List<Location> fixLocations) {
        this.arena = arena;
        this.deadLineSeconds = deadLineSeconds;
        fixLocations.forEach(fix -> monitors.add(new OxygenMonitor(fix)));
        this.warning = new GenericWarning(arena, deadLineSeconds, LanguageManager.getINSTANCE().getDefaultLocale().getMsg(null, OxygenSabotageProvider.NAME_PATH).replace("{fixed}", "0").replace("{total}", String.valueOf(monitors.size())));
        arena.registerGameListener(new OxygenListener());
    }

    public Arena getArena() {
        return arena;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void tryDeactivate() {
        int fixed = (int) monitors.stream().filter(OxygenMonitor::isFixed).count();
        if (monitors.size() == fixed) {
            active = false;
            warning.restore();
            this.deadLineSeconds = warning.getOriginalSeconds();
            getArena().getPlayers().forEach(player -> player.sendTitle(" ", LanguageManager.getINSTANCE().getMsg(player, OxygenSabotageProvider.FIXED_SUBTITLE), 0, 40, 0));
            arena.tryEnableTaskIndicators();
            Bukkit.getPluginManager().callEvent(new GameSabotageDeactivateEvent(getArena(), this, false));
        } else {
            warning.setBarName(LanguageManager.getINSTANCE().getDefaultLocale().getMsg(null, OxygenSabotageProvider.NAME_PATH).replace("{fixed}", String.valueOf(fixed)).replace("{total}", String.valueOf(monitors.size())));
        }
    }

    public void forceFixOnDeadBody() {
        active = false;
        warning.restore();
        this.deadLineSeconds = warning.getOriginalSeconds();
        for (OxygenMonitor monitor : monitors) {
            monitor.onErrorFix(true);
        }
        arena.tryEnableTaskIndicators();
        Bukkit.getPluginManager().callEvent(new GameSabotageDeactivateEvent(getArena(), this, true));
    }

    @Override
    public void activate(@Nullable Player player) {
        warning.setBarName(LanguageManager.getINSTANCE().getDefaultLocale().getMsg(null, OxygenSabotageProvider.NAME_PATH).replace("{fixed}", "0").replace("{total}", String.valueOf(monitors.size())));
        warning.sendBar();
        active = true;
        for (OxygenMonitor monitor : monitors) {
            monitor.startError();
        }
        arena.interruptTasks();
        arena.disableTaskIndicators();
        Bukkit.getPluginManager().callEvent(new GameSabotageActivateEvent(getArena(), this));
    }

    @Override
    public @NotNull SabotageProvider getProvider() {
        return OxygenSabotageProvider.getInstance();
    }

    @Override
    public int getCountDown() {
        return deadLineSeconds;
    }

    @Override
    public void doTick() {
        if (isActive()) {
            if (deadLineSeconds == 0) {
                getArena().removeSabotage(this);
                warning.refreshWarning(0);
                for (Player player : getArena().getPlayers()){
                    player.closeInventory();
                }
                getArena().defeatBySabotage(Message.DEFEAT_REASON_PATH_.toString() + getProvider().getUniqueIdentifier());
            } else {
                warning.refreshWarning(--deadLineSeconds);
            }
        }
        for (OxygenMonitor monitor : monitors) {
            monitor.nextFace();
        }
    }

    public class OxygenMonitor {

        private final ArmorStand armorStand;
        private byte nextErrorFace = -1;
        private byte nextPulseFace = -1;
        private boolean fixed = true;
        private final Hologram hologram;
        private final GlowingBox glowingBox;

        public OxygenMonitor(Location location) {
            // try to create error state hologram
            hologram = new Hologram(location.clone().add(0, 2, 0).add(location.getDirection()), 1);
            hologram.allowCollisions(false);
            HologramPage page = hologram.getPage(0);
            assert page != null;
            page.setLineContent(0, new LineTextContent(s -> LanguageManager.getINSTANCE().getMsg(s, OxygenSabotageProvider.TO_FIX_HOLOGRAM)));
            hologram.hide();
            glowingBox = new GlowingBox(location.clone().add(0, 1.5, 0), 1, GlowColor.RED);


            /*if (standardFace == null) {
                standardFace = ItemUtil.createSkullWithSkin("https://textures.minecraft.net/texture/2fd253c4c6d66ed6694bec818aac1be7594a3dd8e59438d01cb76737f959", "Monitor");
            }*/
            if (errorFaces.isEmpty()) {
                // e
                errorFaces.add(ItemUtil.createSkullWithSkin("https://textures.minecraft.net/texture/b821d7684b699d5767986dbefa67d03fb391a563be9866ea2918259142d9a5", "E"));
                // r,r
                ItemStack r = ItemUtil.createSkullWithSkin("https://textures.minecraft.net/texture/c9731704b6973ab0e4beb8d81be782fdbf6fe4d7ac54b84a94317ab37c17", "R");
                errorFaces.add(r);
                //o
                errorFaces.add(ItemUtil.createSkullWithSkin("https://textures.minecraft.net/texture/b8d519b619952f1a1097f9245a81b57dba97149e4fdad6ef8bf4842543f3ce8b", "O"));
                //r
                errorFaces.add(r);
                //!
                errorFaces.add(ItemUtil.createSkullWithSkin("https://textures.minecraft.net/texture/c415aace88a32b9ef223746e4af85ecd64ddd78653cbc6cd0f125635767", "!"));
            }
            if (pulseFaces.isEmpty()) {
                pulseFaces.add(ItemUtil.createSkullWithSkin("https://textures.minecraft.net/texture/3bba759d9136ec85bd8482ce24c825c740afff45cf1bb3a8e32d2c1c71043e8", "Pulse"));
                pulseFaces.add(ItemUtil.createSkullWithSkin("https://textures.minecraft.net/texture/e9d57e1fa8e02c3bf4d9d7dec8be9be34a42faab419c769e07daf6ce1a2b95b", "Pulse"));
            }
            armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
            armorStand.setInvulnerable(true);
            armorStand.setVisible(false);
            armorStand.setRemoveWhenFarAway(false);
            armorStand.setHeadPose(new EulerAngle(25, 0, 0));
            armorStand.setSilent(true);
        }

        public void startError() {
            hologram.show();
            hologram.refreshLines();
            fixed = false;
            for (Player player : getArena().getPlayers()) {
                getGlowingBox().startGlowing(player);
            }
        }

        public GlowingBox getGlowingBox() {
            return glowingBox;
        }

        public void nextFace() {
            if (fixed) {
                armorStand.setHelmet(pulseFaces.get(++nextPulseFace < pulseFaces.size() ? nextPulseFace : (nextPulseFace = 0)));
            } else {
                armorStand.setHelmet(errorFaces.get(++nextErrorFace < errorFaces.size() ? nextErrorFace : (nextErrorFace = 0)));
            }
        }

        public void onErrorFix(boolean meeting) {
            nextErrorFace = -1;
            nextPulseFace = -1;
            fixed = true;
            if (!meeting) {
                tryDeactivate();
            }
            hologram.hide();
            for (Player player : getArena().getPlayers()) {
                getGlowingBox().stopGlowing(player);
            }
        }

        public boolean isFixed() {
            return fixed;
        }
    }

    private class OxygenListener implements GameListener {
        @Override
        public void onPlayerLeave(Arena arena, Player player, boolean spectator) {
            warning.removePlayer(player);
        }

        @Override
        public void onPlayerToSpectator(Arena arena, Player player) {
            warning.removePlayer(player);
        }

        @Override
        public void onPlayerInteractEntity(Arena arena, Player player, Entity entity) {
            if (!isActive()) return;
            tryOpen(player, entity);
        }

        @Override
        public void onEntityPunch(Arena arena, Player player, Entity entity) {
            tryOpen(player, entity);
        }

        @Override
        public void onMeetingStageChange(Arena arena, MeetingStage oldStage, MeetingStage newStage) {
            if (oldStage == MeetingStage.NO_MEETING) {
                forceFixOnDeadBody();
            }
        }

        private void tryOpen(Player player, Entity entity) {
            if (!isActive()) return;
            Team playerTeam = arena.getPlayerTeam(player);
            if (playerTeam == null || playerTeam.getIdentifier().endsWith("-ghost")) return;
            if (arena.getCamHandler() != null && arena.getCamHandler().isOnCam(player, arena)) return;
            for (OxygenMonitor monitor : monitors) {
                if ((entity.equals(monitor.armorStand) || entity.equals(monitor.getGlowingBox().getMagmaCube())) && !monitor.isFixed()) {
                    SteveSus.newChain().async(() -> {
                        OxygenDisplay gui = new OxygenDisplay(LanguageManager.getINSTANCE().getLocale(player), monitor);
                        SteveSus.newChain().sync(() -> gui.open(player)).execute();
                    }).execute();
                }
            }
        }
    }
}
