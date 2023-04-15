package dev.andrei1058.game.api.glow;

import dev.andrei1058.game.api.SteveSusAPI;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GlowingBox {

    private MagmaCube magmaCube;
    private final GlowColor color;

    public GlowingBox(Location location, int boxSize, GlowColor color){
        if (boxSize < 1){
            boxSize = 1;
        }
        location.setYaw(0);
        location.setPitch(0);
        // check of there is a glowing box already
        for (Entity entity : location.getWorld().getNearbyEntities(location, 1,1,1)){
            if (entity.getType() == EntityType.MAGMA_CUBE && entity.isInvulnerable() && entity.isSilent() && !entity.hasGravity()){
                magmaCube = (MagmaCube) entity;
                break;
            }
        }
        if (magmaCube == null) {
            magmaCube = (MagmaCube) location.getWorld().spawnEntity(location, EntityType.MAGMA_CUBE);
            magmaCube.setSize(boxSize);
            magmaCube.setInvulnerable(true);
            magmaCube.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
            magmaCube.setAI(false);
            magmaCube.setRemoveWhenFarAway(false);
            magmaCube.setGravity(false);
            magmaCube.setSilent(true);
        }
        this.color = color;
    }

    public void startGlowing(Player player){
        SteveSusAPI.getInstance().getGlowingHandler().setGlowing(getMagmaCube(), player, getColor());
    }

    public void stopGlowing(Player player){
        SteveSusAPI.getInstance().getGlowingHandler().removeGlowing(getMagmaCube(), player);
    }

    public MagmaCube getMagmaCube() {
        return magmaCube;
    }

    public GlowColor getColor() {
        return color;
    }
}
