package com.andrei1058.stevesus.arena.gametask.wiring.panel;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.hook.hologram.HologramI;
import com.andrei1058.stevesus.api.hook.hologram.HologramManager;
import com.andrei1058.stevesus.api.locale.Locale;
import com.andrei1058.stevesus.arena.gametask.wiring.FixWiringProvider;
import com.andrei1058.stevesus.arena.gametask.wiring.FixWiringTask;
import com.andrei1058.stevesus.hook.glowing.GlowingManager;
import com.andrei1058.stevesus.language.LanguageManager;
import com.github.johnnyjayjay.spigotmaps.MapBuilder;
import com.github.johnnyjayjay.spigotmaps.RenderedMap;
import com.github.johnnyjayjay.spigotmaps.rendering.ImageRenderer;
import com.github.johnnyjayjay.spigotmaps.util.ImageTools;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class WallPanel {

    private final int wiresAmount;
    private final FixWiringTask.PanelFlag flag;
    private final ItemFrame itemFrame;
    private @Nullable HologramI hologram = null;

    private int assignments = 0;

    public WallPanel(@NotNull Arena arena, int x, int y, int z, int wiresAmount, FixWiringTask.PanelFlag flag) {
        this.wiresAmount = wiresAmount;
        this.flag = flag;

        this.itemFrame = (ItemFrame) arena.getWorld().getNearbyEntities(new Location(arena.getWorld(), x, y, z), 1, 1, 1).stream().filter(entity -> entity instanceof ItemFrame).findFirst().orElse(null);
        if (this.itemFrame == null) {
            SteveSus.getInstance().getLogger().warning("Item Frame needs to be placed at " + x + " " + y + " " + z + " on " + arena.getTemplateWorld() + " for Fix Wiring task!");
            return;
        }
        this.itemFrame.setItem(null);

        try {
            BufferedImage catImage = ImageIO.read(this.getClass().getResource("wiring_panel.png")); // read an image from a source, e.g. a file
            catImage = ImageTools.resizeToMapSize(catImage); // resize the image to the minecraft map size
            ImageRenderer catRenderer = ImageRenderer.builder().image(catImage).renderOnce(true).build();

            RenderedMap map = MapBuilder.create() // make a new builder
                    .addRenderers(catRenderer) // add the renderers to this map
                    .world(arena.getWorld()) // set the world this map is bound to, e.g. the world of the target player
                    .build(); // build the map
            ItemStack mapItem = map.createItemStack();
            this.itemFrame.setItem(mapItem);
        } catch (IOException e) {
            e.printStackTrace();
        }

        var holoManager = HologramManager.getInstance().getProvider();

        if (null != holoManager) {
            this.hologram = holoManager.spawnHologram(
                    itemFrame.getLocation().clone().add(0, 1, 0)
                            .add((itemFrame.getLocation().getDirection().normalize())
                            )
            );
            this.hologram.setPageContent(List.of(
                    r -> LanguageManager.getINSTANCE().getMsg(r, FixWiringProvider.PANEL_HOLO)
            ));
            this.hologram.hideToAll();
        }
    }

    public ItemFrame getItemFrame() {
        return itemFrame;
    }

    public FixWiringTask.PanelFlag getFlag() {
        return flag;
    }

    /**
     * Number of users having this panel.
     */
    public int getAssignments() {
        return assignments;
    }

    /**
     * Open fix panel to the given  player.
     */
    public void startFixingPanel(Player player, FixWiringTask fixWiring) {
        SteveSus.newChain().async(() -> {
            Locale playerLang = LanguageManager.getINSTANCE().getLocale(player);
            PanelGUI gui = new PanelGUI(PanelGUI.getPattern(wiresAmount), playerLang, fixWiring, wiresAmount);
            SteveSus.newChain().sync(() -> gui.open(player)).execute();
        }).execute();
    }

    public void startGlowing(UUID player) {
        Player player1 = Bukkit.getPlayer(player);
        if (player1 == null) return;
        GlowingManager.setGlowingGreen(getItemFrame(), player1);
        if (hologram != null) {
            hologram.showToPlayer(player1);
        }
    }

    public void startGlowing(Player player) {
        GlowingManager.setGlowingGreen(getItemFrame(), player);
        if (hologram != null) {
            hologram.showToPlayer(player);
        }
    }

    public void stopGlowing(UUID player) {
        Player player1 = Bukkit.getPlayer(player);
        if (player1 == null) return;
        GlowingManager.getInstance().removeGlowing(getItemFrame(), player1);
        if (hologram != null) {
            hologram.showToPlayer(player1);
        }
    }

    public void increaseAssignments() {
        assignments++;
    }

    public @Nullable HologramI getHologram() {
        return hologram;
    }

    public void stopGlowing(Player player) {
        GlowingManager.getInstance().removeGlowing(getItemFrame(), player);
        if (hologram != null) {
            hologram.showToPlayer(player);
        }
    }
}
