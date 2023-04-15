package dev.andrei1058.game.hook.corpse;

import com.andrei1058.hologramapi.Hologram;
import com.andrei1058.hologramapi.HologramPage;
import com.andrei1058.hologramapi.content.LineTextContent;
import dev.andrei1058.game.SteveSus;
import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.arena.PlayerCorpse;
import dev.andrei1058.game.api.arena.team.Team;
import dev.andrei1058.game.api.locale.Message;
import dev.andrei1058.game.api.arena.room.CircleRegion;
import dev.andrei1058.game.api.arena.room.Region;
import dev.andrei1058.game.language.LanguageManager;
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
    public static PlayerCorpse spawnCorpse(GameArena gameArena, Player player, Location location, ItemStack helmet, ItemStack chestPlate, ItemStack leggings, ItemStack boots) {
        if (!corpseReborn) {
            return null;
        }
        return new CorpseRebornBody(gameArena, player, location, helmet, chestPlate, leggings, boots);
    }

    public static class CorpseRebornBody implements PlayerCorpse {

        private final Corpses.CorpseData data;
        private final Player owner;
        private final Hologram hologram;
        private final Region region;

        public CorpseRebornBody(GameArena gameArena, Player player, Location location, ItemStack helmet, ItemStack chestPlate, ItemStack leggings, ItemStack boots) {
            owner = player;
            this.region = new CircleRegion(location, 3, false);
            data = CorpseAPI.spawnCorpse(player, location, new ItemStack[]{}, helmet, chestPlate, leggings, boots);
            hologram = new Hologram(location, 2);
            HologramPage page = hologram.getPage(0);
            assert page != null;
            page.setLineContent(0, new LineTextContent(s -> LanguageManager.getINSTANCE().getMsg(s, Message.DEAD_BODY_HOLO_LINE1)));
            page.setLineContent(1, new LineTextContent(s -> LanguageManager.getINSTANCE().getMsg(s, Message.DEAD_BODY_HOLO_LINE2)));
            gameArena.getPlayers().forEach(inGame -> {
                Team playerTeam = gameArena.getPlayerTeam(inGame);
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
