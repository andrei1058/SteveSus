package com.andrei1058.stevesus.hook.corpse;

import com.andrei1058.hologramapi.Hologram;
import com.andrei1058.hologramapi.HologramPage;
import com.andrei1058.hologramapi.content.LineTextContent;
import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.PlayerCorpse;
import com.andrei1058.stevesus.api.arena.team.Team;
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

import java.util.UUID;

public class CorpseManager {

    private CorpseManager() {
    }

    private static boolean corpseReborn = false;

    @Nullable
    public static PlayerCorpse spawnCorpse(Arena arena, Player player, Location location) {
        if (!corpseReborn) {
            return null;
        }
        return new CorpseRebornBody(arena, player, location);
    }

    public static class CorpseRebornBody implements PlayerCorpse {

        private final Corpses.CorpseData data;
        private final Player owner;
        private final Hologram hologram;
        private final Region region;

        public CorpseRebornBody(Arena arena, Player player, Location location) {
            owner = player;
            this.region = new CircleRegion(location, 3, false);
            data = CorpseAPI.spawnCorpse(player, location, new ItemStack[]{}, player.getInventory().getHelmet(), player.getInventory().getChestplate(), player.getInventory().getLeggings(), player.getInventory().getBoots());
            hologram = new Hologram(location, 2);
            HologramPage page = hologram.getPage(0);
            assert page != null;
            page.setLineContent(0, new LineTextContent(s -> LanguageManager.getINSTANCE().getMsg(s, Message.DEAD_BODY_HOLO_LINE1)));
            page.setLineContent(1, new LineTextContent(s -> LanguageManager.getINSTANCE().getMsg(s, Message.DEAD_BODY_HOLO_LINE2)));
            arena.getPlayers().forEach(inGame -> {
                Team playerTeam = arena.getPlayerTeam(inGame);
                if (isInRange(inGame.getLocation()) && playerTeam.canReportBody()) {
                    hologram.show(inGame);
                } else {
                    hologram.hide(inGame);
                }
            });
        }

        public Corpses.CorpseData getData() {
            return data;
        }

        @Override
        public void destroy() {
            CorpseAPI.removeCorpse(data);
            hologram.hide();
            //todo destroy hologram
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
        public @Nullable Hologram getHologram() {
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
