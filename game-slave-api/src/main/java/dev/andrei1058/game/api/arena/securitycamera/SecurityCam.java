package dev.andrei1058.game.api.arena.securitycamera;

import com.andrei1058.stevesus.common.gui.ItemUtil;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("unused")
public class SecurityCam {

    private static ItemStack inUse;
    private static ItemStack notInUse;

    private final String identifier;
    private ArmorStand holder;

    /**
     * @param location   camera location. Where to spawn camera head.
     * @param identifier cam identifier. Used for display name path etc.
     */
    public SecurityCam(Location location, String identifier) {
        this.identifier = identifier;
        this.holder = (ArmorStand) location.subtract(0, 0.2, 0).getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        holder.setVisible(false);
        holder.setInvulnerable(true);
        holder.setRemoveWhenFarAway(false);
        holder.setSilent(true);
        holder.setGravity(false);

        if (inUse == null) {
            inUse = ItemUtil.createSkullWithSkin("https://textures.minecraft.net/texture/2ae3a3a4a1aa50d85dbcdac8da63d7cbfd45e520dfec2d50bedf8e90e8b0e4ea", "In use");
        }
        if (notInUse == null) {
            notInUse = ItemUtil.createSkullWithSkin("https://textures.minecraft.net/texture/d6d15e72acf219bc1da8ae7686f4f83c773adcb4f8facc87cbbd43ee79057f3", "Not in use");
        }

        //todo rotate head??
        //armorStand.setHeadPose(new EulerAngle(25, 0, 0));

        holder.setHelmet(notInUse);
    }

    public ArmorStand getHolder() {
        return holder;
    }

    public void setHolder(ArmorStand holder) {
        this.holder = holder;
    }

    public String getIdentifier() {
        return identifier;
    }

    public static ItemStack getInUse() {
        return inUse;
    }

    public static ItemStack getNotInUse() {
        return notInUse;
    }

    public static void setInUse(ItemStack inUse) {
        SecurityCam.inUse = inUse;
    }

    public static void setNotInUse(ItemStack notInUse) {
        SecurityCam.notInUse = notInUse;
    }
}
