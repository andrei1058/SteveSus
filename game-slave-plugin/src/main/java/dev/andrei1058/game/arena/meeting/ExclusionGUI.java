package dev.andrei1058.game.arena.meeting;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.arena.team.PlayerColorAssigner;
import dev.andrei1058.game.api.arena.team.Team;
import dev.andrei1058.game.api.locale.Locale;
import dev.andrei1058.game.api.locale.Message;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.common.CommonManager;
import dev.andrei1058.game.common.api.gui.BaseGUI;
import dev.andrei1058.game.common.api.gui.CustomHolder;
import dev.andrei1058.game.common.api.gui.slot.RefreshableSlotHolder;
import dev.andrei1058.game.common.gui.ItemUtil;
import dev.andrei1058.game.common.selector.SelectorManager;
import dev.andrei1058.game.language.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class ExclusionGUI extends BaseGUI {

    private static final String NBT_EXCLUSION_TARGET_UUID = "autt-1058-ex";
    private final LinkedHashMap<Integer, RefreshableSlotHolder> playerSlots = new LinkedHashMap<>();

    public ExclusionGUI(String guiName, List<String> pattern, Player player, Locale lang, GameArena gameArena) {
        super(pattern, lang, new ExclusionHolder(), (lang.hasPath(Message.EXCLUSION_GUI_NAME + "-" + guiName) ?
                lang.getMsg(player, Message.EXCLUSION_GUI_NAME + "-" + guiName) : lang.getMsg(player, Message.EXCLUSION_GUI_NAME))
                .replace("{spectator}", player.getDisplayName()).replace("{spectator_raw}", player.getName())
                .replace("{time}", String.valueOf(gameArena.getCountdown())));

        // load content
        YamlConfiguration yml = VoteGUIManager.getInstance().getConfig().getYml();
        String path = guiName + "." + VoteLayoutConfig.VOTE_GENERIC_REPLACE_PATH;

        for (String replacementString : yml.getConfigurationSection(path).getKeys(false)) {
            if (replacementString.toCharArray().length > 1) {
                CommonManager.getINSTANCE().getPlugin().getLogger().warning("Invalid char '" + replacementString + "' on voteGUI replacements: " + guiName);
                continue;
            }

            if (yml.getBoolean(path + "." + replacementString + ".vote")) {
                final int[] currentPlayer = {-1};
                List<Integer> slots = this.getReplacementSlots(replacementString.charAt(0));

                slots.forEach(slot -> {
                    final int currentPlayerFinal = ++currentPlayer[0];
                    RefreshableSlotHolder itemHolder = (slot1, lang12, filter) -> getItemStack(yml, path, gameArena, replacementString, currentPlayerFinal, player, guiName);
                    ItemStack itemStack = itemHolder.getSlotItem(slot, lang, null);
                    if (itemStack != null && itemStack.getType() != Material.AIR) {
                        playerSlots.put(slot, itemHolder);
                    }
                    this.getInventory().setItem(slot, itemStack);
                });
            } else {
                withReplacement(replacementString.charAt(0), (slot, lang1, filter) -> getItemStack(yml, path, gameArena, replacementString, 0, player, guiName));
            }
        }
    }

    private ItemStack getItemStack(YamlConfiguration yml, String path, GameArena gameArena, String replacementString, int currentPlayer, Player player, String guiName) {
        Player targetPlayer = null;
        PlayerColorAssigner.PlayerColor playerColor = null;

        List<Player> filteredPlayers = gameArena.getPlayers().stream().filter(pl -> {
            Team team = gameArena.getPlayerTeam(pl);
            if (team == null) return false;
            return team.canBeVoted();
        }).collect(Collectors.toList());

        // if current item should point to a player
        if (yml.getBoolean(path + "." + replacementString + ".vote")) {
            if (filteredPlayers.size() > currentPlayer) {
                targetPlayer = filteredPlayers.get(currentPlayer);
                if (gameArena.getPlayerColorAssigner() != null) {
                    playerColor = gameArena.getPlayerColorAssigner().getPlayerColor(targetPlayer);
                }
            } else {
                return new ItemStack(Material.AIR);
            }
        }

        // apply commands on click
        List<String> tags = new ArrayList<>();
        String cmdP = yml.getString(path + "." + replacementString + ".commands.as-player");
        if (targetPlayer != null) {
            tags.add(NBT_EXCLUSION_TARGET_UUID);
            tags.add(targetPlayer.getUniqueId().toString());
        }
        if (cmdP != null && !cmdP.isEmpty()) {
            tags.add(SelectorManager.NBT_P_CMD_KEY);
            String command = cmdP.replace("{player}", player.getDisplayName()).replace("{player_raw}", player.getName()).replace("{player_uuid}", player.getUniqueId().toString());
            if (targetPlayer != null) {
                command = command.replace("{target}", targetPlayer.getDisplayName()).replace("{target_raw}", targetPlayer.getName()).replace("{target_uuid}", targetPlayer.getUniqueId().toString());
            }
            tags.add(command);
        }
        String cmdC = yml.getString(path + "." + replacementString + ".commands.as-console");
        if (cmdC != null && !cmdC.isEmpty()) {
            tags.add(SelectorManager.NBT_C_CMD_KEY);
            String command = cmdC.replace("{player}", player.getDisplayName()).replace("{player_raw}", player.getName()).replace("{player_uuid}", player.getUniqueId().toString());
            if (targetPlayer != null) {
                command = command.replace("{target}", targetPlayer.getDisplayName()).replace("{target_raw}", targetPlayer.getName()).replace("{target_uuid}", targetPlayer.getUniqueId().toString());
            }
            tags.add(command);
        }

        String namePath = Message.EXCLUSION_REPLACEMENT_ITEM_NAME_PATH.toString().replace("{s}", guiName).replace("{r}", replacementString);
        String lorePath = Message.EXCLUSION_REPLACEMENT_ITEM_LORE_PATH.toString().replace("{s}", guiName).replace("{r}", replacementString);
        if (!CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getDefaultLocale().hasPath(namePath)) {
            // create path on default language
            CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getDefaultLocale().setMsg(namePath, "&7" + replacementString);
        }
        if (!CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getDefaultLocale().hasPath(lorePath)) {
            // create path on default language
            CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getDefaultLocale().setList(lorePath, new ArrayList<>());
        }
        ItemStack item = playerColor == null ? ItemUtil.createItem(yml.getString(path + "." + replacementString + ".item.material"),
                (byte) yml.getInt(path + "." + replacementString + ".item.data"), yml.getInt(path + "." + replacementString + ".item.amount"),
                yml.getBoolean(path + "." + replacementString + ".item.enchanted"), tags) : playerColor.getPlayerHead(targetPlayer, gameArena);

        if (playerColor != null) {
            for (int i = 0; i < tags.size(); i += 2) {
                item = CommonManager.getINSTANCE().getItemSupport().addTag(item, tags.get(i), tags.get(i + 1));
            }
        }

        // apply skin if head
        if (CommonManager.getINSTANCE().getItemSupport().isPlayerHead(item)) {
            // apply holder skin
            if (targetPlayer == null) {
                item = CommonManager.getINSTANCE().getItemSupport().applyPlayerSkinOnHead(player, item);
            } else if (playerColor == null) {
                // apply target skin
                item = CommonManager.getINSTANCE().getItemSupport().applyPlayerSkinOnHead(targetPlayer, item);
            }
        }

        ItemMeta meta = item.getItemMeta();

        // air does not have meta
        if (meta != null) {
            String displayName = getLang().getMsg(player, namePath).replace("{player}", player.getDisplayName()).replace("{player_raw}", player.getName()).replace("{player_uuid}", player.getUniqueId().toString());

            // if target item
            if (targetPlayer != null) {
                displayName = displayName.replace("{target}", targetPlayer.getDisplayName()).replace("{target_raw}", targetPlayer.getName()).replace("{target_uuid}", targetPlayer.getUniqueId().toString()).replace("{time}", String.valueOf(gameArena.getCountdown()));
            }
            meta.setDisplayName(displayName);

            if (targetPlayer == null) {
                meta.setLore(getLang().getMsgList(player, lorePath, new String[]{"{player}", player.getDisplayName(), "{player_raw}", player.getName(), "{player_uuid}", player.getUniqueId().toString(),
                        "{time}", String.valueOf(gameArena.getCountdown())}));
            } else {
                meta.setLore(getLang().getMsgList(targetPlayer, lorePath, new String[]{"{player}", player.getDisplayName(), "{player_raw}", player.getName(), "{player_uuid}", player.getUniqueId().toString(),
                        "{target}", targetPlayer.getDisplayName(), "{target_raw}", targetPlayer.getName(), "{target_uuid}", targetPlayer.getUniqueId().toString(), "{time}", String.valueOf(gameArena.getCountdown()),
                        "{status}", getStatusReplacement(LanguageManager.getINSTANCE().getLocale(player), player, targetPlayer, gameArena)}));
            }

            item.setItemMeta(meta);
        }
        return item;
    }

    private static String getStatusReplacement(Locale playerLocale, Player guiHolder, Player target, GameArena gameArena) {
        if (target.isOnline()) {
            StringBuilder result = new StringBuilder("\n");
            if (gameArena.getMeetingButton() != null) {
                if (gameArena.getMeetingButton().isLastRequester(target)) {
                    result.append(playerLocale.getMsg(guiHolder, Message.EXCLUSION_GUI_STATUS_REQUESTER)).append("\n");
                }
                if (gameArena.getCurrentVoting() != null && !gameArena.getLiveSettings().isAnonymousVotes()) {
                    Set<Player> votes = gameArena.getCurrentVoting().getVotes(target, gameArena);
                    if (!votes.isEmpty()) {
                        result.append(playerLocale.getMsg(guiHolder, Message.EXCLUSION_GUI_STATUS_VOTERS)).append("\n");
                        for (Player player : votes) {
                            result.append(playerLocale.getMsg(guiHolder, Message.EXCLUSION_GUI_STATUS_VOTE_LIST).replace("{player}", player.getDisplayName())).append("\n");
                        }
                    }
                }
            }
            return result.toString();
        } else {
            return playerLocale.getMsg(guiHolder, Message.EXCLUSION_GUI_STATUS_DISCONNECTED);
        }
    }

    public static class ExclusionHolder implements CustomHolder {

        private BaseGUI handler;

        @Override
        public Inventory getInventory() {
            return getGui().getInventory();
        }

        @Override
        public BaseGUI getGui() {
            return handler;
        }

        @Override
        public void setGui(BaseGUI gui) {
            handler = gui;
        }

        @Override
        public void onClick(Player player, ItemStack itemStack, ClickType clickType) {
            String tag = CommonManager.getINSTANCE().getItemSupport().getTag(itemStack, SelectorManager.NBT_P_CMD_KEY);
            if (tag != null && !tag.isEmpty()) {
                player.closeInventory();
                for (String cmd : tag.split("\\n")) {
                    Bukkit.dispatchCommand(player, cmd);
                }
            }
            tag = CommonManager.getINSTANCE().getItemSupport().getTag(itemStack, SelectorManager.NBT_C_CMD_KEY);
            if (tag != null && !tag.isEmpty()) {
                player.closeInventory();
                for (String cmd : tag.split("\\n")) {
                    // {player} is the one who clicks, {target} is the target's head.
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }
            }
            tag = CommonManager.getINSTANCE().getItemSupport().getTag(itemStack, NBT_EXCLUSION_TARGET_UUID);
            if (tag != null && !tag.isEmpty()) {
                GameArena gameArena = ArenaManager.getINSTANCE().getArenaByPlayer(player);
                if (gameArena != null) {
                    if (gameArena.getCurrentVoting() == null) return;

                    Team playerTeam = gameArena.getPlayerTeam(player);
                    if (playerTeam == null) return;
                    UUID targetUUID;
                    try {
                        targetUUID = UUID.fromString(tag);
                    } catch (IllegalArgumentException ex) {
                        return;
                    }
                    Player target = Bukkit.getPlayer(targetUUID);
                    if (target == null) {
                        return;
                    }
                    GameArena targetGameArena = ArenaManager.getINSTANCE().getArenaByPlayer(target);
                    // in case player stayed with open GUI a long time and target left
                    if (targetGameArena == null || !targetGameArena.equals(gameArena)) {
                        return;
                    }
                    if (gameArena.getCurrentVoting().addVote(target, player, gameArena)) {
                        player.closeInventory();
                    }
                }
            }
        }

        @Override
        public boolean isStatic() {
            return false;
        }
    }

    @Override
    public void refresh() {
        playerSlots.forEach((slot, holder) -> this.getInventory().setItem(slot, holder.getSlotItem(slot, getLang(), null)));
        super.refresh();
    }
}
