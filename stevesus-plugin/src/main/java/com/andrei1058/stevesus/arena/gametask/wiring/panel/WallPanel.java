package com.andrei1058.stevesus.arena.gametask.wiring.panel;

import com.andrei1058.hologramapi.Hologram;
import com.andrei1058.hologramapi.HologramPage;
import com.andrei1058.hologramapi.content.LineTextContent;
import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class WallPanel {

    private final int wiresAmount;
    private final FixWiringTask.PanelFlag flag;
    private final ItemFrame itemFrame;
    private Hologram hologram;

    private int assignments = 0;

    public WallPanel(Arena arena, int x, int y, int z, int wiresAmount, FixWiringTask.PanelFlag flag) {
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

        this.hologram = new Hologram(itemFrame.getLocation().clone().add(0,1,0).add((itemFrame.getLocation().getDirection().normalize()))/*.add(itemFrame.getLocation().getDirection())*/, 1);
        HologramPage page = this.hologram.getPage(0);
        assert page != null;
        page.setLineContent(0, new LineTextContent(s -> LanguageManager.getINSTANCE().getMsg(s, FixWiringProvider.PANEL_HOLO)));
        hologram.hide();
        hologram.allowCollisions(false);
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

    public void startGlowing(UUID player){
        Player player1 = Bukkit.getPlayer(player);
        GlowingManager.setGlowingGreen(getItemFrame(), player1);
        hologram.show(player1);
    }

    public void startGlowing(Player player){
        GlowingManager.setGlowingGreen(getItemFrame(), player);
        hologram.show(player);
    }

    public void stopGlowing(UUID player){
        Player player1 = Bukkit.getPlayer(player);
        GlowingManager.removeGlowing(getItemFrame(), player1);
        hologram.hide(player1);
    }

    public void increaseAssignments() {
        assignments++;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public void stopGlowing(Player player) {
        GlowingManager.removeGlowing(getItemFrame(), player);
        hologram.hide(player);
    }
}
