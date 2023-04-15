package dev.andrei1058.game.arena.gametask.manifolds;

import com.andrei1058.hologramapi.Hologram;
import com.andrei1058.hologramapi.HologramPage;
import com.andrei1058.hologramapi.content.LineTextContent;
import dev.andrei1058.game.SteveSus;
import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.arena.task.GameTask;
import dev.andrei1058.game.api.arena.task.TaskProvider;
import dev.andrei1058.game.api.arena.task.TaskType;
import dev.andrei1058.game.api.server.GameSound;
import dev.andrei1058.game.api.server.multiarena.InventoryBackup;
import dev.andrei1058.game.api.setup.SetupListener;
import dev.andrei1058.game.api.setup.SetupSession;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.common.CommonManager;
import dev.andrei1058.game.common.gui.ItemUtil;
import dev.andrei1058.game.config.properties.OrphanLocationProperty;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class UnlockManifoldsProvider extends TaskProvider {

    private static UnlockManifoldsProvider instance;

    private UnlockManifoldsProvider() {
    }


    public static UnlockManifoldsProvider getInstance() {
        if (instance == null) {
            instance = new UnlockManifoldsProvider();
        }
        return instance;
    }

    @Override
    public String getDefaultDisplayName() {
        return "&8Unlock Manifolds";
    }

    @Override
    public String getDefaultDescription() {
        return "Count to ten.";
    }

    @Override
    public String getIdentifier() {
        return "unlock_manifolds";
    }

    @Override
    public Plugin getProvider() {
        return SteveSus.getInstance();
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.SHORT;
    }

    @Override
    public boolean isVisual() {
        return false;
    }

    @Override
    public boolean canSetup(Player player, SetupSession setupSession) {
        return true;
    }

    @Override
    public void onSetupRequest(Player player, SetupSession setupSession, String localName) {
        InventoryBackup inventoryBackup = new InventoryBackup(player);
        player.sendMessage("Place the shulker box to set its location.");
        setupSession.setAllowCommands(false);

        ItemStack save = ItemUtil.createItem("BOOK", (byte) 0, 1, true, Arrays.asList("manifoldsTag", "save"), ChatColor.RED + "" + ChatColor.BOLD + "Save and close: " + ChatColor.RESET + getDefaultDisplayName(), null);
        player.getInventory().setItem(0, save);

        ItemStack shulker = ItemUtil.createItem(ItemUtil.getMaterial("BLACK_SHULKER_BOX", "BLACK_SHULKER_BOX"), (byte) 0, 1, false, Arrays.asList("manifoldsTag", "shulker"), "&dPlace this to set its location.", null);
        player.getInventory().setItem(2, shulker);

        player.getInventory().setHeldItemSlot(5);

        final Shulker[] shulkerEntity = {null};
        final Hologram[] hologram = {null};

        setupSession.addSetupListener("unlockManifolds_" + localName, new SetupListener() {
            @Override
            public void onPlayerInteract(SetupSession setupSession, PlayerInteractEvent event) {
                ItemStack itemStack = CommonManager.getINSTANCE().getItemSupport().getInHand(event.getPlayer());
                if (itemStack == null) return;
                if (itemStack.getType() == Material.AIR) return;
                String tag = CommonManager.getINSTANCE().getItemSupport().getTag(itemStack, "manifoldsTag");
                if (tag == null) return;
                if (tag.equals("save")) {
                    event.setCancelled(true);
                    if (player.getCooldown(itemStack.getType()) > 0) return;
                    player.setCooldown(itemStack.getType(), 40);
                    SteveSus.newChain().delay(10).sync(() -> {
                        if (shulkerEntity[0] == null) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + getDefaultDisplayName() + " &cwasn't saved because you didn't place the item."));
                            setupSession.setAllowCommands(true);
                            player.sendTitle(ChatColor.translateAlternateColorCodes('&', getDefaultDisplayName()), ChatColor.RED + "Not saved!", 0, 60, 0);
                            player.getInventory().clear();
                            inventoryBackup.restore(player);
                        } else {
                            JsonObject config = new JsonObject();
                            player.getInventory().clear();
                            inventoryBackup.restore(player);
                            config.addProperty("location", new OrphanLocationProperty().toExportValue(shulkerEntity[0].getLocation()).toString());
                            setupSession.setAllowCommands(true);
                            GameSound.JOIN_SOUND_CURRENT.playToPlayer(player);
                            player.sendMessage(ChatColor.GRAY + "Command usage is now enabled!");
                            ArenaManager.getINSTANCE().saveTaskData(getInstance(), setupSession, localName, config);
                            player.sendTitle(getDefaultDisplayName(), ChatColor.GOLD + "Saved!", 0, 60, 0);
                        }
                        setupSession.removeSetupListener("unlockManifolds_" + localName);
                    }).execute();
                }
            }

            @Override
            public void onBlockPlace(SetupSession setupSession, BlockPlaceEvent event) {
                ItemStack itemStack = CommonManager.getINSTANCE().getItemSupport().getInHand(event.getPlayer());
                if (itemStack == null) return;
                if (itemStack.getType() == Material.AIR) return;
                String tag = CommonManager.getINSTANCE().getItemSupport().getTag(itemStack, "manifoldsTag");
                if (tag == null) return;
                if (tag.equals("shulker")) {
                    if (shulkerEntity[0] != null) {
                        shulkerEntity[0].remove();
                    }
                    if (hologram[0] != null) {
                        hologram[0].remove();
                    }
                    shulkerEntity[0] = player.getWorld().spawn(event.getBlockPlaced().getLocation(), Shulker.class);
                    shulkerEntity[0].setInvulnerable(true);
                    shulkerEntity[0].setAI(false);
                    shulkerEntity[0].setSilent(true);
                    shulkerEntity[0].setMetadata(localName, new FixedMetadataValue(SteveSus.getInstance(), localName));
                    player.sendMessage("Task location set!");
                    hologram[0] = new Hologram(event.getBlockPlaced().getLocation(), 1);
                    HologramPage page = hologram[0].getPage(0);
                    assert page != null;
                    page.setLineContent(0, new LineTextContent(s -> ChatColor.translateAlternateColorCodes('&', getDefaultDisplayName()) + "(" + localName + ")"));
                    hologram[0].refreshLines();
                    event.setCancelled(true);
                }
            }

            @Override
            public void onEntityDamageByEntity(SetupSession setupSession, EntityDamageByEntityEvent event) {
                if (event.getEntity() instanceof Shulker) {
                    if (event.getEntity().hasMetadata(localName)) {
                        event.getEntity().remove();
                        shulkerEntity[0] = null;
                        if (hologram[0] != null) {
                            hologram[0].remove();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onSetupLoad(SetupSession setupSession, String localName, JsonObject configData) {
        if (!validateElements(configData, "location")) {
            return;
        }
        String loc = configData.get("location").getAsString();
        Location location = new OrphanLocationProperty().convert(loc, null);
        if (location == null) return;
        location.setWorld(Bukkit.getWorld(setupSession.getWorldName()));

        Hologram hologram = new Hologram(location, 1);
        HologramPage page = hologram.getPage(0);
        assert page != null;
        page.setLineContent(0, new LineTextContent(s -> ChatColor.translateAlternateColorCodes('&', getDefaultDisplayName()) + "(" + localName + ")"));
        hologram.refreshLines();
    }

    @Override
    public void onSetupClose(SetupSession setupSession, String localName, JsonObject configData) {

    }

    @Override
    public void onRemove(SetupSession setupSession, String localName, JsonObject configData) {

    }

    @Override
    public @Nullable GameTask onGameInit(GameArena gameArena, JsonObject configuration, String localName) {
        if (!validateElements(configuration, "location")) {
            return null;
        }
        String loc = configuration.get("location").getAsString();
        Location location = new OrphanLocationProperty().convert(loc, null);
        if (location == null) return null;
        location.setWorld(gameArena.getWorld());
        return new UnlockManifoldsTask(localName, gameArena, location);
    }
}
