package com.andrei1058.stevesus.arena.gametask.wiring;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.task.GameTask;
import com.andrei1058.stevesus.api.arena.task.TaskHandler;
import com.andrei1058.stevesus.api.arena.task.TaskTriggerType;
import com.andrei1058.stevesus.api.arena.task.TaskType;
import com.andrei1058.stevesus.api.server.GameSound;
import com.andrei1058.stevesus.api.setup.SetupListener;
import com.andrei1058.stevesus.api.setup.SetupSession;
import com.andrei1058.stevesus.arena.ArenaHandler;
import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.gui.ItemUtil;
import com.andrei1058.stevesus.config.properties.OrphanLocationProperty;
import com.andrei1058.stevesus.server.multiarena.InventoryBackup;
import com.andrei1058.stevesus.setup.command.AddCommand;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Function;

public class FixWiringHandler extends TaskHandler {

    private static FixWiringHandler instance;

    private FixWiringHandler() {
    }

    public static FixWiringHandler getInstance() {
        if (instance == null) {
            instance = new FixWiringHandler();
        }
        return instance;
    }

    @Override
    public String getDefaultDisplayName() {
        return "Fix Wiring";
    }

    @Override
    public String getIdentifier() {
        return "fix_wiring";
    }

    @Override
    public Plugin getProvider() {
        return SteveSus.getInstance();
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.COMMON;
    }

    @Override
    public TaskTriggerType getTriggerType() {
        return TaskTriggerType.INTERACT_ENTITY;
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
        InventoryBackup.createInventoryBackup(player);

        // Chat details
        Arrays.asList(
                " ",
                " ",
                ChatColor.AQUA + getDefaultDisplayName(),
                " ",
                ChatColor.AQUA + "1. " + ChatColor.RESET + "You can adjust how many times a player has to fix wiring panels. They are known as " + ChatColor.AQUA + "stages" + ChatColor.RESET + ".",
                ChatColor.AQUA + "2. " + ChatColor.RESET + "Place the item frame from your inventory where you want a panel. By right-clicking an item frame you can set a flag. You can mark for instance an item-frame as " + ChatColor.AQUA + FixWiring.PanelFlag.NEVER_FIRST.getDescription() + ChatColor.RESET + " if wiring should never begin with it. (Ex: Fix Wiring never begins with Cafeteria or Security). Stage order and locations is randomised for each player.",
                ChatColor.AQUA + "3. " + ChatColor.RESET + "Use the items in your inventory to adjust how many wires should have the next placed panel."
        ).forEach(player::sendMessage);

        // how many wires panel were set. stages set equivalent.
        final int[] panelsSet = {0};

        final int[] wiresAmount = {4};
        int minWires = 1;
        int maxWires = 6;

        final int[] stagesAmount = {3};
        int minStages = 1;
        int maxStages = 20;

        ItemStack minusWires = ItemUtil.createItem(ItemUtil.getMaterial("CONCRETE", "LIGHT_BLUE_CONCRETE"), (byte) 3, 1, false, Arrays.asList("fixWireItem", "wires_amount_minus"));
        ItemMeta minusWireMeta = minusWires.getItemMeta();
        minusWireMeta.setDisplayName(ChatColor.BLUE + "Decrease Wires amount (for next panel) [Right click]");
        minusWires.setItemMeta(minusWireMeta);

        ItemStack plusWires = ItemUtil.createItem(ItemUtil.getMaterial("CONCRETE", "RED_BLUE_CONCRETE"), (byte) 14, 1, false, Arrays.asList("fixWireItem", "wires_amount_plus"));
        ItemMeta plusWiresMeta = plusWires.getItemMeta();
        plusWiresMeta.setDisplayName(ChatColor.RED + "Increase Wires amount (for next panel) [Right click]");
        plusWires.setItemMeta(plusWiresMeta);

        ItemStack minusStages = ItemUtil.createItem(ItemUtil.getMaterial("CONCRETE", "YELLOW_CONCRETE"), (byte) 4, 1, false, Arrays.asList("fixWireItem", "stages_amount_minus"));
        ItemMeta minusStagesItemMeta = minusStages.getItemMeta();
        minusStagesItemMeta.setDisplayName(ChatColor.GOLD + "Decrease Stages amount [Right click]");
        minusStages.setItemMeta(minusStagesItemMeta);

        ItemStack plusStages = ItemUtil.createItem(ItemUtil.getMaterial("CONCRETE", "MAGENTA_CONCRETE"), (byte) 2, 1, false, Arrays.asList("fixWireItem", "stages_amount_plus"));
        ItemMeta plusStagesItemMeta = plusStages.getItemMeta();
        plusStagesItemMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Increase Stages amount [Right click]");
        plusStages.setItemMeta(plusStagesItemMeta);

        ItemStack wiresPanel = ItemUtil.createItem("ITEM_FRAME", (byte) 0, 1, false, Arrays.asList("wiresPanel", "panel"));
        ItemMeta panelMeta = wiresPanel.getItemMeta();
        panelMeta.setDisplayName(ChatColor.YELLOW + "Place this where to spawn wires panel.");
        wiresPanel.setItemMeta(panelMeta);

        ItemStack wiringClose = ItemUtil.createItem("BOOK", (byte) 0, 1, true, Arrays.asList("fixWireItem", "wires_save"));
        ItemMeta wiringMeta = wiringClose.getItemMeta();
        wiringMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Save and close: " + ChatColor.RESET + getDefaultDisplayName());
        wiringClose.setItemMeta(wiringMeta);

        player.getInventory().setItem(0, wiringClose);
        player.getInventory().setItem(1, wiresPanel);
        player.getInventory().setItem(3, plusStages);
        player.getInventory().setItem(4, minusStages);
        player.getInventory().setItem(7, plusWires);
        player.getInventory().setItem(8, minusWires);
        player.getInventory().setHeldItemSlot(2);

        // Cached wire panels
        LinkedList<Hanging> panelEntities = new LinkedList<>();

        // save and close task setup.
        final boolean[] preventCalledTwice = {false};
        Function<Void, Void> saveAndCloseTaskSetup = (Void o) -> {
            if (preventCalledTwice[0]) return null;
            preventCalledTwice[0] = true;
            JsonArray panels = new JsonArray();
            panelEntities.forEach(panelEntity -> {
                JsonObject entry = new JsonObject();
                entry.addProperty("location", (String) new OrphanLocationProperty().toExportValue(panelEntity.getLocation()));
                entry.addProperty("wires", panelEntity.getMetadata("wiring_wires").get(0).asInt());
                entry.addProperty("flag", panelEntity.getMetadata("wiring_flag").get(0).asString());
                panelEntity.removeMetadata("wiring_wires", SteveSus.getInstance());
                panelEntity.removeMetadata("wiring_flag", SteveSus.getInstance());
                panels.add(entry);
            });
            player.sendTitle(getDefaultDisplayName(), ChatColor.GOLD + "Saved!", 0, 60, 0);
            HashMap<String, Object> taskData = new HashMap<>();
            taskData.put("stages", stagesAmount[0]);
            taskData.put("panels", panels);
            ArenaHandler.getINSTANCE().saveTaskData(this, setupSession, localName, new JSONObject(taskData));
            setupSession.setAllowCommands(true);
            InventoryBackup.restoreInventory(player);
            GameSound.JOIN_SOUND_CURRENT.playToPlayer(player);
            player.sendMessage(ChatColor.GRAY + "Command usage is now enabled!");
            setupSession.removeSetupListener("fix_wiring_setup");

            // protect new added panels during setup
            registerItemFrameProtector(setupSession, localName);
            return null;
        };

        setupSession.addSetupListener("fix_wiring_setup", new SetupListener() {
            @Override
            public void onPlayerInteract(PlayerInteractEvent event) {
                ItemStack itemStack = CommonManager.getINSTANCE().getItemSupport().getInHand(event.getPlayer());
                if (itemStack == null) return;
                if (itemStack.getType() == Material.AIR) return;
                if (CommonManager.getINSTANCE().getItemSupport().hasTag(itemStack, "wiresPanel")) return;
                String tag = CommonManager.getINSTANCE().getItemSupport().getTag(itemStack, "fixWireItem");
                if (tag == null) return;
                event.setCancelled(true);

                if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    switch (tag) {
                        case "wires_save":
                            SteveSus.newChain().delay(2).sync(() -> saveAndCloseTaskSetup.apply(null)).execute();
                            break;
                        case "wires_amount_plus":
                            if (wiresAmount[0] < maxWires) {
                                wiresAmount[0]++;
                            }
                            event.getPlayer().sendTitle(ChatColor.BLUE + "" + wiresAmount[0], ChatColor.WHITE + "Wires amount", 0, 40, 0);
                            GameSound.JOIN_SOUND_SELF.playToPlayer(player);
                            break;
                        case "wires_amount_minus":
                            if (wiresAmount[0] > minWires) {
                                wiresAmount[0]--;
                            }
                            event.getPlayer().sendTitle(ChatColor.BLUE + "" + wiresAmount[0], ChatColor.WHITE + "Wires amount", 0, 40, 0);
                            GameSound.JOIN_SOUND_SELF.playToPlayer(player);
                            break;
                        case "stages_amount_plus":
                            if (stagesAmount[0] < maxStages) {
                                stagesAmount[0]++;
                            }
                            event.getPlayer().sendTitle(ChatColor.GOLD + "" + stagesAmount[0], ChatColor.WHITE + "Stages amount", 0, 40, 0);
                            GameSound.JOIN_SOUND_SELF.playToPlayer(player);
                            break;
                        case "stages_amount_minus":
                            if (stagesAmount[0] > minStages && stagesAmount[0] > panelsSet[0]) {
                                stagesAmount[0]--;
                            }
                            event.getPlayer().sendTitle(ChatColor.GOLD + "" + stagesAmount[0], ChatColor.WHITE + "Stages amount", 0, 40, 0);
                            GameSound.JOIN_SOUND_SELF.playToPlayer(player);
                            break;
                    }
                }
            }

            @Override
            public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
                if (event.getRightClicked() == null) return;
                if (event.getRightClicked().hasMetadata("wiring_flag")) {
                    event.setCancelled(true);
                    FixWiring.PanelFlag newFlag = FixWiring.PanelFlag.valueOf(event.getRightClicked().getMetadata("wiring_flag").get(0).asString());
                    newFlag = newFlag.next();
                    event.getRightClicked().removeMetadata("wiring_flag", SteveSus.getInstance());
                    event.getRightClicked().setMetadata("wiring_flag", new FixedMetadataValue(SteveSus.getInstance(), newFlag.toString()));

                    ItemStack itemPassenger = new ItemStack(Material.REDSTONE);
                    ItemMeta itemMeta = itemPassenger.getItemMeta();
                    itemMeta.setDisplayName(ChatColor.YELLOW + getDefaultDisplayName() + " (" + ChatColor.GRAY + localName + ChatColor.YELLOW + ") - Flag: " + newFlag.getDescription());
                    itemPassenger.setItemMeta(itemMeta);
                    ItemFrame itemFrame = (ItemFrame) event.getRightClicked();
                    itemFrame.setItem(itemPassenger);
                }
            }

            @Override
            public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
                event.setCancelled(true);
            }

            @Override
            public void onPlayerDropItem(PlayerDropItemEvent event) {
                event.setCancelled(true);
            }

            @Override
            public void onPlayerPickupItem(EntityPickupItemEvent event) {
                event.setCancelled(true);
            }

            @Override
            public void onBlockBreak(BlockBreakEvent event) {

            }

            @Override
            public void onBlockPlace(BlockPlaceEvent event) {

            }

            @Override
            public void onHangingPlace(HangingPlaceEvent event) {
                ItemStack inHand = CommonManager.getINSTANCE().getItemSupport().getInHand(event.getPlayer());
                if (inHand == null) return;
                if (inHand.getType() == Material.AIR) return;
                if (CommonManager.getINSTANCE().getItemSupport().hasTag(inHand, "wiresPanel")) {
                    if (event.isCancelled()) {
                        event.setCancelled(false);
                    }
                    panelsSet[0]++;
                    event.getEntity().setMetadata("wiring_wires", new FixedMetadataValue(SteveSus.getInstance(), wiresAmount));
                    event.getEntity().setMetadata("wiring_flag", new FixedMetadataValue(SteveSus.getInstance(), FixWiring.PanelFlag.REGULAR.toString()));
                    event.getEntity().setMetadata("wiring_name", new FixedMetadataValue(SteveSus.getInstance(), localName));

                    ItemStack itemPassenger = new ItemStack(Material.REDSTONE);
                    ItemMeta itemMeta = itemPassenger.getItemMeta();
                    itemMeta.setDisplayName(ChatColor.YELLOW + getDefaultDisplayName() + " (" + ChatColor.GRAY + localName + ChatColor.YELLOW + ") - Flag: " + FixWiring.PanelFlag.REGULAR.getDescription());
                    itemPassenger.setItemMeta(itemMeta);
                    ItemFrame itemFrame = (ItemFrame) event.getEntity();
                    itemFrame.setItem(itemPassenger);

                    panelEntities.add(event.getEntity());
                    player.sendTitle(" ", ChatColor.YELLOW + "Click the item frame to change type", 0, 100, 0);
                    GameSound.JOIN_SOUND_SELF.playToPlayer(player);
                }
            }

            @Override
            public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
                event.setCancelled(true);
            }

            @Override
            public void onHangingBreak(HangingBreakEvent event) {
                event.setCancelled(true);
            }

            @Override
            public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {

            }
        });
    }

    @Override
    public void onSetupLoad(SetupSession setupSession, String localName, JSONObject configData) {
        registerItemFrameProtector(setupSession, localName);
    }

    @Override
    public void onRemove(Player player, SetupSession setupSession, String localName, JSONObject configData) {
        setupSession.removeSetupListener(localName + "_task_protect");
    }

    @Override
    public @Nullable GameTask init(Arena arena, JSONObject configuration) {
        return null;
    }

    private static void registerItemFrameProtector(SetupSession setupSession, String localTaskName) {
        setupSession.addSetupListener(localTaskName + "_task_protect", new SetupListener() {
            @Override
            public void onPlayerInteract(PlayerInteractEvent event) {
            }

            @Override
            public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
                if (event.getRightClicked() != null && event.getRightClicked().hasMetadata("wiring_name")) {
                    String taskName = event.getRightClicked().getMetadata("wiring_name").get(0).asString();
                    if (AddCommand.hasTaskWithRememberName(event.getPlayer(), taskName)) {
                        event.setCancelled(true);
                    }
                }
            }

            @Override
            public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {

            }

            @Override
            public void onPlayerDropItem(PlayerDropItemEvent event) {

            }

            @Override
            public void onPlayerPickupItem(EntityPickupItemEvent event) {

            }

            @Override
            public void onBlockBreak(BlockBreakEvent event) {

            }

            @Override
            public void onBlockPlace(BlockPlaceEvent event) {

            }

            @Override
            public void onHangingPlace(HangingPlaceEvent event) {

            }

            @Override
            public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
                if (event.getEntity() != null && event.getEntity().hasMetadata("wiring_name")) {
                    String taskName = event.getEntity().getMetadata("wiring_name").get(0).asString();
                    if (AddCommand.hasTaskWithRememberName(event.getRemover(), taskName)) {
                        event.setCancelled(true);
                    }
                }
            }

            @Override
            public void onHangingBreak(HangingBreakEvent event) {
                if (event.getEntity() != null && event.getEntity().hasMetadata("wiring_name")) {
                    String taskName = event.getEntity().getMetadata("wiring_name").get(0).asString();
                    if (AddCommand.hasTaskWithRememberName(event.getEntity().getWorld().getName(), taskName)) {
                        event.setCancelled(true);
                    }
                }
            }

            @Override
            public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
                if (event.getEntity() != null && event.getEntity().hasMetadata("wiring_name")) {
                    String taskName = event.getEntity().getMetadata("wiring_name").get(0).asString();
                    if (AddCommand.hasTaskWithRememberName(event.getEntity().getWorld().getName(), taskName)) {
                        event.setCancelled(true);
                    }
                }
            }
        });
    }
}
