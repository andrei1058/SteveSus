package com.andrei1058.stevesus.common;

import com.andrei1058.stevesus.common.api.CommonProvider;
import com.andrei1058.stevesus.common.api.arena.DisplayableArena;
import com.andrei1058.stevesus.common.api.arena.GameState;
import com.andrei1058.stevesus.common.party.PartyManager;
import com.andrei1058.stevesus.common.api.server.CommonPermission;
import com.andrei1058.spigot.versionsupport.ItemStackSupport;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.stream.Stream;

public class CommonManager {

    private static CommonManager INSTANCE;
    private final Plugin plugin;
    private final ItemStackSupport itemStackSupport;
    private final CommonProvider commonProvider;
    public static final byte SERVER_VERSION = Byte.parseByte(Bukkit.getServer().getClass().getName().split("\\.")[3].split("_")[1]);

    private CommonManager(CommonProvider commonProvider, Plugin plugin) {
        this.commonProvider = commonProvider;
        this.plugin = plugin;
        itemStackSupport = ItemStackSupport.SupportBuilder.load();

        // server version not supported
        if (itemStackSupport == null) {
            throw new IllegalStateException("Server version not supported! (ItemStackSupport)");
        }
    }

    public static void init(CommonProvider commonProvider, Plugin plugin) {
        if (INSTANCE == null) {
            INSTANCE = new CommonManager(commonProvider, plugin);
        }

        // Initialize common permissions
        CommonPermission.init();
    }

    //todo must be reached somehow trough the API
    public ItemStackSupport getItemSupport() {
        return itemStackSupport;
    }

    public static CommonManager getINSTANCE() {
        return INSTANCE;
    }

    public CommonProvider getCommonProvider() {
        return commonProvider;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * I keep this here since using this in different adapters will make it a duplicate.
     */
    //todo must be reached somehow trough the API
    public @Nullable DisplayableArena requestGame(Player player, @Nullable String template) {
        if (getINSTANCE().getCommonProvider().getArenas().isEmpty()) return null;

        int requiredSlots;

        UUID partyOwner = PartyManager.getINSTANCE().getPartyAdapter().getOwner(player.getUniqueId());
        if (partyOwner == null) {
            requiredSlots = getINSTANCE().getCommonProvider().hasVipJoin(player) ? 0 : 1;
        } else {
            // part where checking party members so they can play together
            int partySize = CommonManager.getINSTANCE().getCommonProvider().getPartyHandler().getPartyAdapter().getMembers(player.getUniqueId()).size();
            if (partySize == 0) {
                partySize = 1;
            }

            int vipKicks = 0;
            boolean ownerChecked = false;
            for (UUID partyMember : CommonManager.getINSTANCE().getCommonProvider().getPartyHandler().getPartyAdapter().getMembers(player.getUniqueId())) {
                if (partyOwner.equals(partyMember)) {
                    ownerChecked = true;
                }
                Player playerMember = Bukkit.getPlayer(partyMember);
                if (playerMember != null && playerMember.isOnline() && !CommonManager.getINSTANCE().getCommonProvider().isInGame(playerMember)) {
                    if (getINSTANCE().getCommonProvider().hasVipJoin(playerMember)) {
                        vipKicks++;
                    }
                } else {
                    partySize--;
                }
            }
            if (!ownerChecked && getINSTANCE().getCommonProvider().hasVipJoin(player) && partyOwner.equals(player.getUniqueId())) {
                vipKicks++;
            }

            requiredSlots = partySize - vipKicks;
        }

        // if arena free slots >= requiredSlots

        Stream<DisplayableArena> filer = getINSTANCE().getCommonProvider().getArenas().stream().filter(arena -> arena.getGameState() == GameState.WAITING || arena.getGameState() == GameState.STARTING);

        // filter template names
        if (template != null) {
            String[] templates = template.split("\\+");
            filer = filer.filter(arena -> {
                for (String map : templates) {
                    return map.equalsIgnoreCase(template);
                }
                return false;
            });
        }

        // return first arena where EMPTY SLOTS are greater or equal to the required slots
        return filer.filter(arena -> (arena.getMaxPlayers() - arena.getCurrentPlayers()) >= requiredSlots).findFirst().orElse(null);
    }

    /**
     * I keep this here since using this in different adapters will make it a duplicate.
     */
    //todo must be reached somehow trough the API
    public boolean hasVipJoin(Player player) {
        return player.hasPermission(CommonPermission.VIP_JOIN_FEATURE.get()) || player.hasPermission(CommonPermission.ALL.get());
    }
}
