package com.andrei1058.stevesus.arena.gametask.upload.panel;

import com.andrei1058.hologramapi.Hologram;
import com.andrei1058.hologramapi.HologramPage;
import com.andrei1058.hologramapi.content.LineTextContent;
import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.arena.gametask.upload.UploadTaskProvider;
import com.andrei1058.stevesus.language.LanguageManager;
import com.github.johnnyjayjay.spigotmaps.MapBuilder;
import com.github.johnnyjayjay.spigotmaps.RenderedMap;
import com.github.johnnyjayjay.spigotmaps.rendering.ImageRenderer;
import com.github.johnnyjayjay.spigotmaps.util.ImageTools;
import org.bukkit.Location;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class WallPanel {

    public enum PanelType {
        DOWNLOAD, UPLOAD
    }

    private final ItemFrame itemFrame;
    private Hologram hologram;
    private final PanelType panelType;

    public WallPanel(Arena arena, Location location, PanelType panelType) {
        this.panelType = panelType;
        this.itemFrame = (ItemFrame) location.getWorld().getNearbyEntities(new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ()), 1, 1, 1).stream().filter(entity -> entity instanceof ItemFrame).findFirst().orElse(null);
        if (this.itemFrame == null) {
            SteveSus.getInstance().getLogger().warning("Item Frame needs to be placed at " + location.toString() + " on " + arena.getTemplateWorld() + " for UploadTask task!");
            return;
        }
        this.itemFrame.setItem(null);


        try {
            BufferedImage catImage = ImageIO.read(this.getClass().getResource(getPanelType() == PanelType.DOWNLOAD ? "download_panel.png" : "upload_panel.png")); // read an image from a source, e.g. a file
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

        this.hologram = new Hologram(itemFrame.getLocation().clone().add(0, 1, 0).add((itemFrame.getLocation().getDirection().normalize())), 1);
        HologramPage page = this.hologram.getPage(0);
        assert page != null;
        page.setLineContent(0, new LineTextContent(s -> LanguageManager.getINSTANCE().getMsg(s, getPanelType() == PanelType.DOWNLOAD ? UploadTaskProvider.DOWNLOAD_PANEL_HOLO : UploadTaskProvider.UPLOAD_PANEL_HOLO)));
        hologram.hide();
        hologram.allowCollisions(false);
    }

    public PanelType getPanelType() {
        return panelType;
    }

    public ItemFrame getItemFrame() {
        return itemFrame;
    }

    public Hologram getHologram() {
        return hologram;
    }
}
