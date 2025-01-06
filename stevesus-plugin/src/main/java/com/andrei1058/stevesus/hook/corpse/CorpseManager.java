package com.andrei1058.stevesus.hook.corpse;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.PlayerCorpse;
import com.andrei1058.stevesus.api.arena.team.Team;
import com.andrei1058.stevesus.api.hook.hologram.HologramI;
import com.andrei1058.stevesus.api.hook.hologram.HologramManager;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.api.arena.room.CircleRegion;
import com.andrei1058.stevesus.api.arena.room.Region;
import com.andrei1058.stevesus.language.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.golde.bukkit.corpsereborn.CorpseAPI.CorpseAPI;
import org.golde.bukkit.corpsereborn.nms.Corpses;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.UUID;

public class CorpseManager {

    private CorpseManager() {
    }

    private static boolean corpseReborn = false;

    @Nullable
    public static PlayerCorpse spawnCorpse(Arena arena, Player player, Location location, ItemStack helmet, ItemStack chestPlate, ItemStack leggings, ItemStack boots) {
        if (!corpseReborn) {
            return null;
        }
        return new CorpseRebornBody(arena, player, location, helmet, chestPlate, leggings, boots);
    }

    public static class CorpseRebornBody implements PlayerCorpse {

        private final Corpses.CorpseData data;
        private final Player owner;
        private @Nullable HologramI hologram = null;
        private final Region region;

        public CorpseRebornBody(Arena arena, Player player, Location location, ItemStack helmet, ItemStack chestPlate, ItemStack leggings, ItemStack boots) {
            owner = player;
            this.region = new CircleRegion(location, 3, false);
            data = CorpseAPI.spawnCorpse(player, location, new ItemStack[]{}, helmet, chestPlate, leggings, boots);

            var holoManager = HologramManager.getInstance().getProvider();
            if (null != holoManager) {
                hologram = holoManager.spawnHologram(location);
                hologram.setPageContent(Arrays.asList(
                        r -> LanguageManager.getINSTANCE().getMsg(r, Message.DEAD_BODY_HOLO_LINE1),
                        r -> LanguageManager.getINSTANCE().getMsg(r, Message.DEAD_BODY_HOLO_LINE2)
                ));
            }
            arena.getPlayers().forEach(inGame -> {
                Team playerTeam = arena.getPlayerTeam(inGame);
                if (isInRange(inGame.getLocation()) && playerTeam.canReportBody()) {
                    hologram.showToPlayer(inGame);
                } else {
                    hologram.hideFromPlayer(inGame);
                }
            });
        }

        public Corpses.CorpseData getData() {
            return data;
        }

        @Override
        public void destroy() {
            CorpseAPI.removeCorpse(data);
            if (null != hologram) {
                hologram.remove();
            }
        }

        @Override
        public void teleport(Location location) {

        }

        @Override
        public void playAnimation(int animation) {

        }

        @Override
        public int getEntityId() {
            return 0;
        }

        @Override
        public UUID getOwner() {
            return owner.getUniqueId();
        }

        @Override
        public @Nullable HologramI getHologram() {
            return hologram;
        }

        @Override
        public boolean isInRange(Location location) {
            return region.isInRegion(location);
        }

        public Player getOwnerPlayer() {
            return owner;
        }
    }

    public static void init() {
        if (Bukkit.getPluginManager().getPlugin("CorpseReborn") != null) {
            corpseReborn = true;
            Bukkit.getPluginManager().registerEvents(new CorpseClickListener(), SteveSus.getInstance());
        }
    }
}
