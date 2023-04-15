package dev.andrei1058.game.api.arena.team;

import dev.andrei1058.game.api.SteveSusAPI;
import dev.andrei1058.game.api.arena.Arena;
import dev.andrei1058.game.api.locale.Message;
import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.gui.ItemUtil;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jetbrains.annotations.NotNull;

public enum LegacyPlayerColor implements PlayerColorAssigner.PlayerColor {

    RED("Light Red", "legacy-light-red", "a7d5eb0aea5d61ba3ff4996416a90096a9d77609ebcd3b308f906ae888a45f6d",
            ItemUtil.getMaterial("STAINED_GLASS", "RED_STAINED_GLASS"), 14, Color.fromRGB(255, 0, 0)),
    GOLD("Orange", "legacy-orange", "d910e30441cb829b4ee8ca1c0444c1fac6d94ace5a5c17ce46d4ef6cd93b23a9",
            ItemUtil.getMaterial("STAINED_GLASS", "ORANGE_STAINED_GLASS"), 1, Color.ORANGE),
    GRAY("Light Gray", "legacy-light-gray", "f24daa849ffd2ee87829349c74346532154a9572eae0dfed2bb2831f1bd097b",
            ItemUtil.getMaterial("STAINED_GLASS", "LIGHT_GRAY_STAINED_GLASS"), 8, Color.fromRGB(207, 207, 207)),
    AQUA("Cyan", "legacy-light-aqua", "7d3ef1564636889fe3acd3bb264efd752c90d4c6b23b00a3ed6c2d7f5e822775",
            ItemUtil.getMaterial("STAINED_GLASS", "CYAN_STAINED_GLASS"), 9, Color.AQUA),
    DARK_GREEN("Fortegreen", "legacy-green", "4e633480d4bfbeaa049d013ed5746d9f5df9495d0bae1d9a70d5e2649bc264f",
            ItemUtil.getMaterial("STAINED_GLASS", "GREEN_STAINED_GLASS"), 13, Color.GREEN),
    LIGHT_PURPLE("Pink", "legacy-pink", "feb20b93453a82018e2d4063b084035a5fe55a8a175da4ce1adbc6ec40ebe272",
            ItemUtil.getMaterial("STAINED_GLASS", "PINK_STAINED_GLASS"), 6, Color.fromRGB(255, 128, 255)),
    GREEN("Lime", "legacy-lime", "e58e56c765e34423ad2877840ab7c5688b44939c537c202363a4f1b5b7580dc8",
            ItemUtil.getMaterial("STAINED_GLASS", "LIGHT_GREEN_STAINED_GLASS"), 5, Color.LIME),
    YELLOW("Yellow", "legacy-yellow", "ab6b12c1b862b68936e8aee7a248c3e252e88b1fcff05700fce1c959120a229d",
            ItemUtil.getMaterial("STAINED_GLASS", "YELLOW_STAINED_GLASS"), 4, Color.YELLOW),
    WHITE("White", "legacy-white", "e994f7b302612ac3231d41f0e6d78a3082db3bd667d0a9c5bcf12ced8f9405bc",
            ItemUtil.getMaterial("STAINED_GLASS", "WHITE_STAINED_GLASS"), 0, Color.WHITE),
    DARK_PURPLE("Purple", "legacy-purple", "68b818677be3c2079937137f50d555c161703d07e99cc708b8b5f4112938281",
            ItemUtil.getMaterial("STAINED_GLASS", "DARK_PURPLE_STAINED_GLASS"), 10, Color.PURPLE),
    BLUE("Light Blue", "legacy-light-blue", "7016014716f5d9e2949178ec382792f9899671de894c1d3818488e5db56d9fae",
            ItemUtil.getMaterial("STAINED_GLASS", "BLUE_STAINED_GLASS"), 3, Color.fromRGB(128, 128, 255)),
    DARK_GRAY("Dark Gray", "legacy-dark-gray", "1de32c764f3f28d2729b161ee350fa043eb7d29211323aef34c36c938d108a10",
            ItemUtil.getMaterial("STAINED_GLASS", "GRAY_STAINED_GLASS"), 7, Color.fromRGB(133, 133, 133)),
    DARK_AQUA("Turquoise", "legacy-dark-aqua", "80376e74bf758a00aea628a70ae53d85de5e1b5771477c14a847df8fbb1de830",
            ItemUtil.getMaterial("STAINED_GLASS", "CYAN_STAINED_GLASS"), 9, Color.fromRGB(0, 149, 179)),
    DARK_BLUE("Dark Blue", "legacy-dark-blue", "6670bc5f045830094054aebc75b2ed37fc55f524d979d81ef61f3de5c217d0ca",
            ItemUtil.getMaterial("STAINED_GLASS", "BLUE_STAINED_GLASS"), 11, Color.BLUE),
    DARK_RED("Dark Red", "legacy-dark-red", "10fe36a1f7691f13493d341501280efd17f89f7604c179dbc852dbb4b2e32ad",
            ItemUtil.getMaterial("STAINED_GLASS", "RED_STAINED_GLASS"), 14, Color.fromRGB(179, 0, 0)),
    BLACK("Black", "legacy-black", "24e95bdd5151222561370bb67ad4bb0366410f9186dd00ca4d45c6feb8419eac",
            ItemUtil.getMaterial("STAINED_GLASS", "BLACK_STAINED_GLASS"), 15, Color.BLACK);

    private final String defaultDisplayName;
    private final String uniqueIdentifier;
    private final String skin;
    private ItemStack displayItem;
    private final ItemStack helmet;
    private final Color color;

    /**
     * @param defaultDisplayName it's the color default display name which will be automatically saved to language files.
     * @param uniqueIdentifier   is used to identify this color. It is used as well as part of translation path (Ex: {@link Message#COLOR_NAME_PATH_} + legacy-color-red) and maybe more.
     */
    LegacyPlayerColor(String defaultDisplayName, String uniqueIdentifier, String skin, String material, int data, Color color) {
        this.defaultDisplayName = defaultDisplayName;
        this.uniqueIdentifier = uniqueIdentifier;
        this.skin = "https://textures.minecraft.net/texture/" + skin;
        this.helmet = CommonManager.getINSTANCE().getItemSupport().createItem(material, 1, (byte) data);
        this.color = color;
    }

    @Override
    public void apply(Player player, Arena arena) {
        String displayName = SteveSusAPI.getInstance().getLocaleHandler().getDefaultLocale().getMsg(player, Message.COLOR_NAME_PATH_.toString() + getUniqueIdentifier());
        player.setDisplayName(displayName);
        //player.setPlayerListName(displayName);
        player.getInventory().setHelmet(helmet);

        ItemStack content = new ItemStack(Material.LEATHER_CHESTPLATE);

        LeatherArmorMeta meta = (LeatherArmorMeta) content.getItemMeta();
        meta.setColor(color);
        content.setItemMeta(meta);

        player.getInventory().setChestplate(content);

        content = new ItemStack(Material.LEATHER_LEGGINGS);
        content.setItemMeta(meta);
        player.getInventory().setLeggings(content);

        content = new ItemStack(Material.LEATHER_BOOTS);
        content.setItemMeta(meta);
        player.getInventory().setBoots(content);
    }

    @Override
    public @NotNull ItemStack getPlayerHead(Player player, Arena arena) {
        return displayItem == null ? displayItem = ItemUtil.createSkullWithSkin(skin, getDefaultDisplayName()) : displayItem;
    }

    @Override
    public @NotNull String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    @Override
    public @NotNull String getDefaultDisplayName() {
        return defaultDisplayName;
    }

    @Override
    public @NotNull String getDisplayColor(Player player) {
        return player.getDisplayName();
    }
}
