package com.andrei1058.stevesus.common.party.request;

import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.api.locale.CommonLocale;
import com.andrei1058.stevesus.common.api.locale.CommonMessage;
import com.andrei1058.stevesus.common.party.PartyManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class PartyRequest {

    private static final LinkedList<PartyRequest> activeRequests = new LinkedList<>();

    // who send the invitations (name)
    private final String requester;

    // invited players (names)
    List<UUID> invites = new LinkedList<>();


    private PartyRequest(String requester, List<Player> invites) {
        this.requester = requester;
        invites.forEach(player -> this.invites.add(player.getUniqueId()));
        activeRequests.add(this);
    }

    private void addInvitations(List<Player> invites) {
        invites.forEach(player -> this.invites.add(player.getUniqueId()));
    }

    /**
     * Remove track of this invitation instance.
     */
    private void destroyData() {
        activeRequests.remove(this);
        invites.clear();
    }

    /**
     * Remove player from active invitations.
     */
    private static void removePlayerData(Player player) {
        activeRequests.removeIf(request -> {
            request.invites.removeIf(invited -> invited.equals(player.getUniqueId()));
            return request.invites.isEmpty();
        });
    }

    /**
     * Store invitations.
     * This will send messages as well.
     */
    public static void performInvite(Player requester, List<String> invitations, String acceptCommand) {
        List<Player> filteredRequests = new LinkedList<>();

        CommonLocale requesterLocale = CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getLocale(requester);

        invitations.forEach(playerName -> {
            Player online = Bukkit.getPlayer(playerName);
            if (online != null && online.isOnline() && !online.getUniqueId().equals(requester.getUniqueId())) {
                filteredRequests.add(online);
                requester.sendMessage(requesterLocale.getMsg(requester, CommonMessage.CMD_PARTY_INV_SENT).replace("{player}", online.getDisplayName()));
                TextComponent clickable = new TextComponent(CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getLocale(online).getMsg(online, CommonMessage.CMD_PARTY_INV_RECEIVED).replace("{name}", requester.getName()).replace("{player}", requester.getDisplayName()));
                clickable.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, acceptCommand + " " + requester.getName()));
                online.spigot().sendMessage(clickable);
            } else {
                requester.sendMessage(requesterLocale.getMsg(requester, CommonMessage.CMD_PARTY_INV_FAILED).replace("{player}", playerName));
            }
        });

        if (!filteredRequests.isEmpty()) {
            PartyRequest request = getRequestSource(requester.getName());
            if (request == null) {
                new PartyRequest(requester.getName(), filteredRequests);
            } else {
                request.addInvitations(filteredRequests);
            }
        }
    }

    /**
     * Accept a party request.
     *
     * @param invited      player who is accepting the request.
     * @param inviteSource player who sent the request.
     */
    public static void performAcceptInvite(Player invited, String inviteSource) {
        Player partyOwner = Bukkit.getPlayer(inviteSource);
        PartyRequest partyRequest = getRequestSource(partyOwner.getName());
        if (partyRequest == null || !partyOwner.isOnline()) {
            invited.sendMessage(CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getMsg(invited, CommonMessage.CMD_PARTY_ACC_FAILED).replace("{player}", inviteSource));
        } else {
            // destroy joining player's sent invites
            PartyRequest joiningPlayerSentRequests = getRequestSource(invited.getName());
            if (joiningPlayerSentRequests != null) {
                joiningPlayerSentRequests.destroyData();
            }

            // destroy joining player's received invitations
            removePlayerData(invited);

            // add to party
            if (PartyManager.getINSTANCE().getPartyAdapter().hasParty(partyOwner.getUniqueId())) {
                PartyManager.getINSTANCE().getPartyAdapter().addMember(partyOwner.getUniqueId(), invited.getUniqueId());
            } else {
                PartyManager.getINSTANCE().getPartyAdapter().createParty(partyOwner.getUniqueId(), Collections.singletonList(invited.getUniqueId()));
            }

            if (PartyManager.getINSTANCE().getPartyAdapter().isPartySizeLimitReached(partyOwner.getUniqueId())) {
                // if party limit is reached clear invite requests
                onPlayerQuit(partyOwner);
            }

            invited.sendMessage(CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getMsg(invited, CommonMessage.CMD_PARTY_ACC_SUCC).replace("{player}", partyOwner.getDisplayName()));
            for (UUID member : PartyManager.getINSTANCE().getPartyAdapter().getMembers(partyOwner.getUniqueId())) {
                if (member.equals(partyOwner.getUniqueId())) continue;
                Player playerMe = Bukkit.getPlayer(member);
                if (playerMe != null && playerMe.isOnline()) {
                    playerMe.sendMessage(CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getMsg(playerMe, CommonMessage.CMD_PARTY_ACC_BROADCAST).replace("{player}", invited.getDisplayName()).replace("{name}", invited.getName()));
                }
            }
            partyOwner.sendMessage(CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getMsg(partyOwner, CommonMessage.CMD_PARTY_ACC_BROADCAST).replace("{player}", invited.getDisplayName()).replace("{name}", invited.getName()));
        }
    }

    /**
     * To be use in {@link org.bukkit.event.player.PlayerQuitEvent} to clear
     * player sent and received requests.
     */
    public static void onPlayerQuit(Player player) {
        PartyRequest invitesSent = getRequestSource(player.getName());
        if (invitesSent != null) {
            invitesSent.destroyData();
        }
        removePlayerData(player);
    }

    /**
     * Get a request storage instance by its owner.
     *
     * @param owner the player who sent invitations.
     */
    @Nullable
    public static PartyRequest getRequestSource(@NotNull String owner) {
        return activeRequests.stream().filter(request -> request.requester.equals(owner)).findFirst().orElse(null);
    }

    /**
     * Get list of active requests.
     */
    public static List<String> getReceivedRequests(CommandSender s) {
        List<String> activeRequests = new LinkedList<>();
        if (s instanceof Player) {
            PartyRequest.activeRequests.stream().filter(request -> request.invites.contains((((Player) s).getUniqueId())))
                    .forEach(request -> activeRequests.add(request.requester));
        }
        return activeRequests;
    }
}
