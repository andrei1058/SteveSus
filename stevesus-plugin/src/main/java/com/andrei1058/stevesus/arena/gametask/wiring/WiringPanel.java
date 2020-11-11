package com.andrei1058.stevesus.arena.gametask.wiring;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.github.johnnyjayjay.spigotmaps.MapBuilder;
import com.github.johnnyjayjay.spigotmaps.RenderedMap;
import com.github.johnnyjayjay.spigotmaps.rendering.ImageRenderer;
import com.github.johnnyjayjay.spigotmaps.util.ImageTools;
import org.bukkit.Location;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WiringPanel {

    private final int x;
    private final int y;
    private final int z;
    private final int wiresAmount;
    private final FixWiring.PanelFlag flag;
    private ItemFrame itemFrame;

    protected WiringPanel(Arena arena, int x, int y, int z, int wiresAmount, FixWiring.PanelFlag flag) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.wiresAmount = wiresAmount;
        this.flag = flag;

        // TESTING
        this.itemFrame = (ItemFrame) arena.getWorld().getNearbyEntities(new Location(arena.getWorld(), x, y, z), 1, 1, 1).stream().filter(entity -> entity instanceof ItemFrame).findFirst().orElse(null);
        if (this.itemFrame == null) {
            SteveSus.getInstance().getLogger().warning("Item Frame needs to be placed at " + x + " " + y + " " + z + " on " + arena.getTemplateWorld() + " for Fix Wiring task!");
            return;
        }
        this.itemFrame.setItem(null);


        try {
            BufferedImage catImage = ImageIO.read(new File("wiring_panel.png")); // read an image from a source, e.g. a file
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
    }

    public ItemFrame getItemFrame() {
        return itemFrame;
    }
}
