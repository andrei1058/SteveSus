package com.andrei1058.stevesus.commanditem;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.meeting.MeetingStage;
import com.andrei1058.stevesus.api.arena.sabotage.SabotageBase;
import com.andrei1058.stevesus.api.arena.sabotage.SabotageCooldown;
import com.andrei1058.stevesus.api.arena.team.Team;
import com.andrei1058.stevesus.api.locale.Locale;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.api.server.PlayerCoolDown;
import com.andrei1058.stevesus.arena.ArenaManager;
import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.api.arena.GameState;
import com.andrei1058.stevesus.common.command.CommonCmdManager;
import com.andrei1058.stevesus.common.gui.ItemUtil;
import com.andrei1058.stevesus.common.selector.SelectorManager;
import com.andrei1058.stevesus.config.MainConfig;
import com.andrei1058.stevesus.language.LanguageManager;
import com.andrei1058.stevesus.server.ServerManager;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class CommandItemsManager {

    public static final String INTERACT_NBT_TAG_PLAYER_CMDS = "aumi-1058-p";
    public static final String INTERACT_NBT_TAG_PLAYER_INTERACT = "interact";
    public static final String INTERACT_NBT_TAG_CONSOLE_CMDS = "aumi-1058-c";

    private static CommandItemsManager INSTANCE;
    public static final String CATEGORY_MAIN_LOBBY = "multi-arena-lobby";
    public static final String CATEGORY_WAITING = "game-waiting";
    public static final String CATEGORY_STARTING = "game-starting";
    public static final String CATEGORY_SPECTATING = "game-spectating";
    public static final String CATEGORY_VOTING = "game-voting";
    public static final String CATEGORY_IMPOSTOR = "game-impostor";
    public static final String CATEGORY_IMPOSTOR_GHOST = "game-impostor-ghost";

    private static final String MATERIAL = ".material";
    private static final String DATA = ".data";
    private static final String ENCHANTED = ".enchanted";
    private static final String PERMISSION = ".permission";
    private static final String SLOT = ".slot";
    private static final String COMMANDS_AS_PLAYER = ".run-commands.as-player";
    private static final String COMMANDS_AS_CONSOLE = ".run-commands.as-console";
    private static final String CUSTOM_NBT = ".run-commands.custom-action";

    private YamlConfiguration yml;
    private File config;
    private boolean firstTime = false;

    // custom NBT on items interact handler
    private final InteractEvent interactEvent = (player, cancellable) -> {
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack == null || itemStack.getType() == Material.AIR) return;
        String tag = CommonManager.getINSTANCE().getItemSupport().getTag(itemStack, CommandItemsManager.INTERACT_NBT_TAG_PLAYER_INTERACT);
        if (tag != null) {
            // stop op cool down?
            if (player.getCooldown(itemStack.getType()) != 0) return;
            Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer(player);
            if (arena == null) return;
            String[] data = tag.split(":");
            if (data.length == 0) return;

            // check if item has sabotage
            SabotageBase sabotageBase = null;
            if (data.length > 1 && data[0].equals("sabotage")) {
                String[] subData = data[1].split(",");
                if (subData.length < 2) return;
                sabotageBase = arena.getLoadedSabotage(subData[0], subData[1]);
            }

            // check item
            if (sabotageBase == null) {
                if (tag.equals("kill")) {
                    Player nearest = null;
                    double distance = arena.getLiveSettings().getKillDistance().getCurrentValue();
                    Team playerTeam = arena.getPlayerTeam(player);
                    for (Player inGame : arena.getPlayers()) {
                        if (player.equals(inGame)) continue;
                        double currentDistance;
                        if ((currentDistance = player.getLocation().distance(inGame.getLocation())) < distance && playerTeam.canKill(inGame)) {
                            nearest = inGame;
                            distance = currentDistance;
                        }
                    }
                    if (nearest != null) {
                        arena.killPlayer(player, nearest);
                    }
                }
            } else {
                // usage cool down
                SabotageCooldown sabotageCooldown = arena.getSabotageCooldown();
                if (sabotageCooldown == null) {
                    return;
                }
                if (sabotageBase.isActive()) {
                    return;
                }
                if (arena.getMeetingStage() != MeetingStage.NO_MEETING){
                    return;
                }
                if (arena.getGameState() != GameState.IN_GAME){
                    return;
                }
                if (sabotageCooldown.isPaused()) {
                    sabotageCooldown.updateCooldownOnItems(player, player.getInventory());
                    return;
                }
                if (sabotageCooldown.getCurrentSeconds() != 0) {
                    return;
                }
                sabotageBase.activate(player);
            }
        }
    };

    /**
     * Initialize configuration from given path.
     *
     * @param itemsDir directory where to get the yml config from.
     */
    private CommandItemsManager(File itemsDir) {
        if (!itemsDir.exists()) {
            if (!itemsDir.mkdir()) {
                SteveSus.getInstance().getLogger().log(Level.SEVERE, "Could not create " + SelectorManager.getINSTANCE().getSelectorDirectory().getPath());
                return;
            }
        }

        config = new File(itemsDir, "layout_items.yml");
        if (!config.exists()) {
            firstTime = true;
            SteveSus.getInstance().getLogger().log(Level.INFO, "Creating " + config.getPath());
            try {
                if (!config.createNewFile()) {
                    SteveSus.getInstance().getLogger().log(Level.SEVERE, "Could not create " + config.getPath());
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        yml = YamlConfiguration.loadConfiguration(config);
        yml.options().copyDefaults(true);

        if (isFirstTime()) {
            saveCommandItem(CATEGORY_MAIN_LOBBY, "stats", CommonCmdManager.getINSTANCE().getMainCmd().getName() + " stats", "", "", false, getMaterial("SKULL_ITEM", "PLAYER_HEAD"), 3, 0, "&bYour Stats", Arrays.asList(" ", "&fRight click to see your stats!"), null);
            saveCommandItem(CATEGORY_MAIN_LOBBY, "selector", CommonCmdManager.getINSTANCE().getMainCmd().getName() + " selector", "", "", false, getMaterial("CHEST", "CHEST"), 0, 4, "&b&lGame Selector", Arrays.asList(" ", "&fRight click to select a game!"), null);
            saveCommandItem(CATEGORY_MAIN_LOBBY, "leave", "leave", "", "", false, getMaterial("BED", "RED_BED"), 0, 8, "&bBack to Lobby", Arrays.asList(" ", "&fRight click to exit!"), null);

            saveCommandItem(CATEGORY_WAITING, "stats", CommonCmdManager.getINSTANCE().getMainCmd().getName() + " stats", "", "", false, getMaterial("SKULL_ITEM", "PLAYER_HEAD"), 3, 0, "&bYour Stats", Arrays.asList(" ", "&fRight click to see your stats!"), null);
            saveCommandItem(CATEGORY_WAITING, "leave", "leave", "", "", false, getMaterial("BED", "RED_BED"), 0, 8, "&bBack to Lobby", Arrays.asList(" ", "&fRight click to exit!"), null);

            saveCommandItem(CATEGORY_STARTING, "stats", CommonCmdManager.getINSTANCE().getMainCmd().getName() + " stats", "", "", false, getMaterial("SKULL_ITEM", "PLAYER_HEAD"), 3, 0, "&bYour Stats", Arrays.asList(" ", "&fRight click to see your stats!"), null);
            saveCommandItem(CATEGORY_STARTING, "leave", "leave", "", "", false, getMaterial("BED", "RED_BED"), 0, 8, "&bBack to Lobby", Arrays.asList(" ", "&fRight click to exit!"), null);

            saveCommandItem(CATEGORY_SPECTATING, "stats", CommonCmdManager.getINSTANCE().getMainCmd().getName() + " stats", "", "", false, getMaterial("SKULL_ITEM", "PLAYER_HEAD"), 3, 0, "&bYour Stats", Arrays.asList(" ", "&fRight click to see your stats!"), null);
            saveCommandItem(CATEGORY_SPECTATING, "teleporter", CommonCmdManager.getINSTANCE().getMainCmd().getName() + " teleporter", "", "", false, getMaterial("COMPASS", "COMPASS"), 0, 4, "&b&lTeleporter", Arrays.asList(" ", "&fRight click to select a target!"), null);
            saveCommandItem(CATEGORY_SPECTATING, "leave", "leave", "", "", false, getMaterial("BED", "RED_BED"), 0, 8, "&bBack to Lobby", Arrays.asList(" ", "&fRight click to exit!"), null);

            saveCommandItem(CATEGORY_VOTING, "vote", "ss vote", "", "", false, CommonManager.SERVER_VERSION < 13 ? "ELYTRA" : "ELYTRA", 0, 4, "&a&lVote", Arrays.asList(" ", "&fRight click to open!"), null);

            saveCommandItem(CATEGORY_IMPOSTOR, "kill", "", "", "", false, "FLINT", 0, 0, "&cKill", Arrays.asList(" ", "&fRight click on nearby", "&fplayers turn red."), "kill");
            saveCommandItem(CATEGORY_IMPOSTOR, "oxygen", "", "", "", false, ItemUtil.getMaterial("SAPLING", "ACACIA_SAPLING"), 0, 3, "&c&lSabotage Oxygen", Arrays.asList(" ", "&fRight click to", "&fsabotage oxygen."), "sabotage:" + SteveSus.getInstance().getName() + ",oxygen");
            saveCommandItem(CATEGORY_IMPOSTOR, "lights", "", "", "", false, ItemUtil.getMaterial("REDSTONE_LAMP_OFF", "REDSTONE_LAMP"), 0, 4, "&c&lSabotage Lights", Arrays.asList(" ", "&fRight click to", "&fsabotage lights."), "sabotage:" + SteveSus.getInstance().getName() + ",lights");

            saveCommandItem(CATEGORY_IMPOSTOR_GHOST, "oxygen", "", "", "", false, ItemUtil.getMaterial("SAPLING", "ACACIA_SAPLING"), 0, 3, "&c&lSabotage Oxygen", Arrays.asList(" ", "&fRight click to", "&fsabotage oxygen."), "sabotage:" + SteveSus.getInstance().getName() + ",oxygen");
            saveCommandItem(CATEGORY_IMPOSTOR_GHOST, "lights", "", "", "", false, ItemUtil.getMaterial("REDSTONE_LAMP_OFF", "REDSTONE_LAMP"), 0, 4, "&c&lSabotage Lights", Arrays.asList(" ", "&fRight click to", "&fsabotage lights."), "sabotage:" + SteveSus.getInstance().getName() + ",lights");
        }

        save();
    }

    /**
     * Save config changes to file
     */
    private void save() {
        try {
            yml.save(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if file was generated for the first time.
     * Used for defaults.
     */
    private boolean isFirstTime() {
        return firstTime;
    }

    /**
     * Get yml instance
     */
    public YamlConfiguration getYml() {
        return yml;
    }

    /**
     * Save a join item to config.
     */
    public void saveCommandItem(String parent, String name, String cmdPlayer, String cmdConsole, String permission, boolean enchanted, String material, int data, int slot, String localeName, List<String> localeLore, String customNBT) {
        if (isFirstTime()) {
            getYml().addDefault(parent + "." + name + MATERIAL, material);
            getYml().addDefault(parent + "." + name + DATA, data);
            if (enchanted) {
                getYml().addDefault(parent + "." + name + ENCHANTED, true);
            }
            getYml().addDefault(parent + "." + name + SLOT, slot);
            getYml().addDefault(parent + "." + name + PERMISSION, permission);
            if (!cmdConsole.isEmpty()) {
                getYml().addDefault(parent + "." + name + COMMANDS_AS_CONSOLE, cmdConsole);
            }
            if (!cmdPlayer.isEmpty()) {
                getYml().addDefault(parent + "." + name + COMMANDS_AS_PLAYER, cmdPlayer);
            }
            if (customNBT != null && !customNBT.isEmpty()) {
                getYml().addDefault(parent + "." + name + CUSTOM_NBT, customNBT);
            }
            save();
        }
        LanguageManager.getINSTANCE().getDefaultLocale().setMsg(Message.JOIN_ITEM_NAME_PATH.toString().replace("{c}", parent).replace("{i}", name), localeName);
        LanguageManager.getINSTANCE().getDefaultLocale().setList(Message.JOIN_ITEM_LORE_PATH.toString().replace("{c}", parent).replace("{i}", name), localeLore);
    }

    public static void sendCommandItems(@NotNull Player player, @NotNull String category) {
        sendCommandItems(player, category, true);
    }

    /**
     * Send command items to a player.
     */
    public static void sendCommandItems(@NotNull Player player, @NotNull String category, boolean clearArmor) {
        YamlConfiguration yml = INSTANCE.getYml();
        if (yml.get(category) == null) return;
        if (yml.getConfigurationSection(category) == null) return;
        SteveSus.debug("Giving command items " + category + " to " + player.getUniqueId() + ".");
        if (clearArmor) {
            player.getInventory().clear();
        } else {
            for (ItemStack itemStack : player.getInventory().getStorageContents()) {
                if (itemStack == null) continue;
                player.getInventory().remove(itemStack);
            }
        }
        for (String item : yml.getConfigurationSection(category).getKeys(false)) {
            String permPath = category + "." + item + PERMISSION;

            // if player has permission
            if (yml.get(permPath) == null || yml.getString(permPath).isEmpty() || player.hasPermission(yml.getString(permPath))) {
                String materialPath = category + "." + item + MATERIAL;
                if (yml.get(materialPath) != null) {
                    boolean enchantPath = yml.getBoolean(category + "." + item + ENCHANTED);
                    int data = yml.getInt(category + "." + item + DATA);
                    int slot = yml.getInt(category + "." + item + SLOT);

                    List<String> tags = new ArrayList<>();
                    String cmdConsPath = category + "." + item + COMMANDS_AS_CONSOLE;
                    String cmdPlPath = category + "." + item + COMMANDS_AS_PLAYER;
                    String nbtPath = category + "." + item + CUSTOM_NBT;
                    if (yml.get(cmdConsPath) != null) {
                        String consoleCMDs = yml.getString(cmdConsPath);
                        if (consoleCMDs != null && !consoleCMDs.trim().isEmpty()) {
                            tags.add(INTERACT_NBT_TAG_CONSOLE_CMDS);
                            tags.add(consoleCMDs);
                        }
                    }
                    if (yml.get(cmdPlPath) != null) {
                        String playerCMDs = yml.getString(cmdPlPath);
                        if (playerCMDs != null && !playerCMDs.trim().isEmpty()) {
                            tags.add(INTERACT_NBT_TAG_PLAYER_CMDS);
                            tags.add(playerCMDs);
                        }
                    }
                    if (yml.get(nbtPath) != null) {
                        String playerCMDs = yml.getString(nbtPath);
                        if (playerCMDs != null && !playerCMDs.trim().isEmpty()) {
                            tags.add(INTERACT_NBT_TAG_PLAYER_INTERACT);
                            tags.add(playerCMDs);
                            // if giving kill item apply delay
                            if (playerCMDs.equals("kill")) {
                                Material material;
                                try {
                                    material = Material.valueOf(yml.getString(materialPath));
                                } catch (Exception ignored) {
                                    break;
                                }
                                Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer(player);
                                if (arena != null) {
                                    PlayerCoolDown cooldown = PlayerCoolDown.getOrCreatePlayerData(player);
                                    cooldown.updateCoolDown("kill", arena.getLiveSettings().getKillCooldown().getMinValue());
                                    player.setCooldown(material, cooldown.getCoolDown("kill") * 20);
                                }
                            }
                        }
                    }

                    ItemStack itemStack = ItemUtil.createItem(yml.getString(materialPath), (byte) data, 1, enchantPath, tags);
                    if (CommonManager.getINSTANCE().getItemSupport().isPlayerHead(itemStack)) {
                        itemStack = CommonManager.getINSTANCE().getItemSupport().applyPlayerSkinOnHead(player, itemStack);
                    }

                    ItemMeta meta = itemStack.getItemMeta();
                    if (meta != null) {
                        String nameMsg = Message.JOIN_ITEM_NAME_PATH.toString().replace("{c}", category).replace("{i}", item);
                        String loreMsg = Message.JOIN_ITEM_LORE_PATH.toString().replace("{c}", category).replace("{i}", item);

                        // save path to fallback language if not exists
                        if (!LanguageManager.getINSTANCE().getDefaultLocale().hasPath(nameMsg)) {
                            LanguageManager.getINSTANCE().getDefaultLocale().setMsg(nameMsg, "&cName not set.");
                        }
                        if (!LanguageManager.getINSTANCE().getDefaultLocale().hasPath(loreMsg)) {
                            LanguageManager.getINSTANCE().getDefaultLocale().setList(loreMsg, Arrays.asList(" ", "&cLore not set in", "&cdefault language file."));
                        }

                        Locale playerLocale = LanguageManager.getINSTANCE().getLocale(player);
                        meta.setDisplayName(playerLocale.getMsg(player, nameMsg));
                        meta.setLore(playerLocale.getMsgList(player, loreMsg));
                        itemStack.setItemMeta(meta);
                    }
                    player.getInventory().setItem(slot, itemStack);
                } else {
                    SteveSus.getInstance().getLogger().warning("Invalid material in join-items config at: " + materialPath);
                }
            }
        }
    }

    /**
     * Get material for current server version.
     */
    private String getMaterial(String mat1_12, String mat1_13) {
        return CommonManager.SERVER_VERSION < 13 ? mat1_12 : mat1_13;
    }


    /**
     * Initialize join-items.
     */
    public static void init() {
        if (INSTANCE == null) {
            File itemsDir = SteveSus.getInstance().getDataFolder();
            String customJoinItemsPath;
            if (!(customJoinItemsPath = ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.JOIN_ITEMS_PATH)).isEmpty()) {
                File newPath = new File(customJoinItemsPath);
                if (newPath.isDirectory()) {
                    itemsDir = newPath;
                    SteveSus.getInstance().getLogger().info("Set join-items configuration path to: " + itemsDir);
                } else {
                    SteveSus.getInstance().getLogger().warning("Tried to set join-items configuration path to: " + itemsDir + " but it does not seem like a directory.");
                }
            }
            INSTANCE = new CommandItemsManager(itemsDir);
        }
    }

    public static CommandItemsManager getINSTANCE() {
        return INSTANCE;
    }

    public InteractEvent getInteractEvent() {
        return interactEvent;
    }
}

