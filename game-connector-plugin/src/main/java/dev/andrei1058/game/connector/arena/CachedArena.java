package dev.andrei1058.game.connector.arena;

import dev.andrei1058.game.common.CommonManager;
import dev.andrei1058.game.common.api.arena.GameState;
import dev.andrei1058.game.common.api.locale.CommonLocale;
import dev.andrei1058.game.common.api.locale.CommonMessage;
import dev.andrei1058.game.common.api.packet.DefaultChannel;
import dev.andrei1058.game.common.api.packet.RawSocket;
import dev.andrei1058.game.common.api.selector.ArenaHolderConfig;
import dev.andrei1058.game.common.party.PartyManager;
import dev.andrei1058.game.connector.SteveSusConnector;
import dev.andrei1058.game.connector.api.arena.RemoteArena;
import dev.andrei1058.game.connector.api.event.GameStateChangeEvent;
import dev.andrei1058.game.connector.api.event.PlayerGameJoinEvent;
import dev.andrei1058.game.connector.language.LanguageManager;
import dev.andrei1058.game.connector.socket.packet.PlayerJoinPacket;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class CachedArena implements RemoteArena {

    private final RawSocket server;
    private final String template;
    private String displayName;
    private final int gameId;
    private GameState gameState;
    private String spectatePerm;
    private int maxPlayers;
    private int minPlayers;
    private int currentPlayers;
    private int currentSpectators;
    private int vips;
    private ItemStack displayItem;

    public CachedArena(RawSocket server, int gameId, String template, String displayName, GameState gameState, String spectatePerm, int maxPlayers, int minPlayers, int players, int spectators, int vips, @Nullable ItemStack displayItem) {
        this.server = server;
        this.template = template;
        this.displayName = displayName;
        this.gameId = gameId;
        this.gameState = gameState;
        this.spectatePerm = spectatePerm;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.currentSpectators = spectators;
        this.currentPlayers = players;
        this.vips = vips;
        setDisplayItem(displayItem);
    }

    @Override
    public RawSocket getServer() {
        return server;
    }

    @Override
    public int getGameId() {
        return gameId;
    }

    @Override
    public void setDisplayName(String name) {
        if (!this.displayName.equals(name)) {
            this.displayName = name;
            //todo call event
        }
    }

    @Override
    public void setGameState(GameState gameState) {
        if (getGameState() != gameState) {
            GameState copy = getGameState();
            this.gameState = gameState;
            Bukkit.getPluginManager().callEvent(new GameStateChangeEvent(this, copy, gameState));
        }
    }

    @Override
    public void setSpectatePermission(String perm) {
        if (!getSpectatePermission().equals(perm)) {
            this.spectatePerm = perm;
            //todo call update event
        }
    }

    @Override
    public void setMinPlayers(int minPlayers) {
        if (this.minPlayers != minPlayers) {
            this.minPlayers = minPlayers;
            //todo call update event
        }
    }

    @Override
    public void setMaxPlayers(int maxPlayers) {
        if (this.maxPlayers != maxPlayers) {
            this.maxPlayers = maxPlayers;
            //todo call update event
        }
    }

    @Override
    public void setCurrentPlayers(int players) {
        this.currentPlayers = players;
    }

    @Override
    public void setCurrentSpectators(int spectators) {
        this.currentSpectators = spectators;
    }

    @Override
    public GameState getGameState() {
        return gameState;
    }

    @Override
    public boolean isFull() {
        return currentPlayers == maxPlayers;
    }

    @Override
    public String getSpectatePermission() {
        return spectatePerm;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public int getMaxPlayers() {
        return maxPlayers;
    }

    @Override
    public int getMinPlayers() {
        return minPlayers;
    }

    @Override
    public int getCurrentPlayers() {
        return currentPlayers;
    }

    @Override
    public int getCurrentSpectators() {
        return currentSpectators;
    }

    @Override
    public String getTemplateWorld() {
        return template;
    }

    @Override
    public String getTag() {
        return getServer().getName() + ":" + getGameId();
    }

    @Override
    public boolean joinPlayer(Player player, boolean ignoreParty) {
        if (getServer() == null) return false;
        if (CommonManager.getINSTANCE().getCommonProvider().isInGame(player)) return false;
        if (getGameState() == GameState.LOADING || getGameState() == GameState.ENDING || getGameState() == GameState.IN_GAME) {
            return false;
        }

        UUID partyOwner = PartyManager.getINSTANCE().getPartyAdapter().getOwner(player.getUniqueId());
        Player partyOwnerPlayer = null;

        if (!ignoreParty && partyOwner != null) {
            partyOwnerPlayer = Bukkit.getPlayer(partyOwner);
            if (!partyOwner.equals(player.getUniqueId())) {
                // owners only can chose a game
                player.sendMessage(LanguageManager.getINSTANCE().getMsg(player, CommonMessage.ARENA_JOIN_DENIED_NO_PARTY_LEADER));
                return false;
            }
        }

        int requiredSlots = 1;
        int partyVips = CommonManager.getINSTANCE().hasVipJoin(player) ? 1 : 0;
        requiredSlots -= partyVips;

        // Handle party adapter and add members to this game if possible
        if (!ignoreParty) {
            if (PartyManager.getINSTANCE().getPartyAdapter().hasParty(player.getUniqueId()) && partyOwner != null) {
                if (partyOwner.equals(player.getUniqueId())) {
                    for (UUID member : PartyManager.getINSTANCE().getPartyAdapter().getMembers(partyOwner)) {
                        if (member.equals(player.getUniqueId())) continue;
                        Player playerMember = Bukkit.getPlayer(member);
                        if (playerMember != null && playerMember.isOnline() && !CommonManager.getINSTANCE().getCommonProvider().isInGame(playerMember)) {
                            requiredSlots++;
                            if (CommonManager.getINSTANCE().hasVipJoin(player)) {
                                partyVips++;
                            }
                        }
                    }
                }
            }
        }

        int actualFreeSlots = maxPlayers - (currentPlayers - vips);

        if (actualFreeSlots >= requiredSlots) {
            // join allowed
            PlayerGameJoinEvent playerGameJoinEvent = new PlayerGameJoinEvent(this, player, false);
            Bukkit.getPluginManager().callEvent(playerGameJoinEvent);
            if (playerGameJoinEvent.isCancelled()) return false;

            player.closeInventory();

            CommonManager.getINSTANCE().getCommonProvider().getPacketsHandler().sendPacket(getServer(), DefaultChannel.PLAYER_JOIN_CHANNEL.toString(), new PlayerJoinPacket(player, partyOwnerPlayer == null ? null : partyOwnerPlayer.getName(), this), false);
            SteveSusConnector.debug("Sending " + player.getName() + " to game: " + getTag());

            //noinspection UnstableApiUsage
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(getServer().getName());
            player.sendPluginMessage(SteveSusConnector.getInstance(), "BungeeCord", out.toByteArray());


            if (!ignoreParty && partyOwner != null) {
                for (UUID member : PartyManager.getINSTANCE().getPartyAdapter().getMembers(partyOwner)) {
                    if (member.equals(player.getUniqueId())) continue;

                    // Add party members to current game if they are in lobby
                    Player playerMember = Bukkit.getPlayer(member);
                    if (playerMember != null && playerMember.isOnline() && !CommonManager.getINSTANCE().getCommonProvider().isInGame(playerMember)) {
                        joinPlayer(playerMember, true);
                        playerMember.sendMessage(LanguageManager.getINSTANCE().getMsg(playerMember, CommonMessage.ARENA_JOIN_VIA_PARTY).replace("{arena}", getDisplayName()));
                    }
                }
            }

            return true;
        }
        return false;
    }

    @SuppressWarnings("unused")
    public int getVips() {
        return vips;
    }

    public void setVips(int vips) {
        this.vips = vips;
    }

    @Override
    public boolean joinSpectator(Player player, @Nullable String target) {
        if (!(getSpectatePermission().trim().isEmpty() || player.hasPermission(getSpectatePermission()))) return false;
        if (CommonManager.getINSTANCE().getCommonProvider().isInGame(player)) return false;
        if (getGameState() == GameState.LOADING || getGameState() == GameState.ENDING) return false;

        PlayerGameJoinEvent playerGameJoinEvent = new PlayerGameJoinEvent(this, player, true);
        Bukkit.getPluginManager().callEvent(playerGameJoinEvent);
        if (playerGameJoinEvent.isCancelled()) return false;

        player.closeInventory();

        CommonManager.getINSTANCE().getCommonProvider().getPacketsHandler().sendPacket(getServer(), DefaultChannel.PLAYER_JOIN_CHANNEL.toString(), new PlayerJoinPacket(player, target, this), false);
        SteveSusConnector.debug("Sending " + player.getName() + " (as spectator) to game: " + getTag());

        //noinspection UnstableApiUsage
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(getServer().getName());
        player.sendPluginMessage(SteveSusConnector.getInstance(), "BungeeCord", out.toByteArray());
        return true;
    }

    @Override
    public ItemStack getDisplayItem(CommonLocale lang) {
        if (displayItem == null) return null;
        if (lang == null) return displayItem;

        ItemStack item = displayItem;
        ItemMeta meta = item.getItemMeta();
        String displayName;
        if (lang.hasPath(ArenaHolderConfig.getNameForState(getGameState()) + "-" + getTemplateWorld())) {
            // check custom name for template
            displayName = strReplaceArenaPlaceholders(lang.getMsg(null, ArenaHolderConfig.getNameForState(getGameState()) + "-" + getTemplateWorld()), lang);
        } else {
            displayName = strReplaceArenaPlaceholders(lang.getMsg(null, ArenaHolderConfig.getNameForState(getGameState())), lang);
        }
        meta.setDisplayName(displayName);

        if (lang.hasPath(ArenaHolderConfig.getLoreForState(getGameState()) + "-" + getTemplateWorld())) {
            meta.setLore(lang.getMsgList(null, ArenaHolderConfig.getLoreForState(getGameState()) + "-" + getTemplateWorld(),
                    new String[]{"{name}", getDisplayName(), "{template}", getTemplateWorld(), "{status}", lang.getMsg(null, getGameState().getTranslatePath()),
                    "{on}", String.valueOf(getCurrentPlayers()), "{max}", String.valueOf(getMaxPlayers()), "{allowSpectate}", String.valueOf(getSpectatePermission()),
                            "{spectating}", String.valueOf(getCurrentSpectators())}));
        } else {
            meta.setLore(lang.getMsgList(null, ArenaHolderConfig.getLoreForState(getGameState()), new String[]{"{name}", getDisplayName(), "{template}", getTemplateWorld(), "{status}", lang.getMsg(null, getGameState().getTranslatePath()),
                    "{on}", String.valueOf(getCurrentPlayers()), "{max}", String.valueOf(getMaxPlayers()), "{allowSpectate}", String.valueOf(getSpectatePermission()), "{spectating}", String.valueOf(getCurrentSpectators())}));
        }
        item.setItemMeta(meta);
        return item;
    }

    private String strReplaceArenaPlaceholders(String in, CommonLocale lang) {
        return in.replace("{name}", getDisplayName()).replace("{template}", getTemplateWorld()).replace("{status}", lang.getMsg(null, getGameState().getTranslatePath()))
                .replace("{on}", String.valueOf(getCurrentPlayers())).replace("{max}", String.valueOf(getMaxPlayers()))
                        .replace("{spectating}", String.valueOf(getCurrentSpectators())).replace("{game_tag}", getTag()).replace("{game_id}", String.valueOf(getGameId()));
    }

    public void setDisplayItem(ItemStack displayItem) {
        if (displayItem == null) {
            this.displayItem = null;
            return;
        }
        ItemStack temp = displayItem;
        if (CommonManager.getINSTANCE().getItemSupport().hasTag(displayItem, CommonManager.getINSTANCE().getCommonProvider().getDisplayableArenaNBTTagKey())) {
            temp = (CommonManager.getINSTANCE().getItemSupport().removeTag(temp, CommonManager.getINSTANCE().getCommonProvider().getDisplayableArenaNBTTagKey()));
        }
        this.displayItem = CommonManager.getINSTANCE().getItemSupport().addTag(temp, CommonManager.getINSTANCE().getCommonProvider().getDisplayableArenaNBTTagKey(), getTag());
    }

    @Override
    public boolean isLocal() {
        return false;
    }
}
