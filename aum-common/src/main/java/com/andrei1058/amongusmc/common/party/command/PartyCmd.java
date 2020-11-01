package com.andrei1058.amongusmc.common.party.command;

import com.andrei1058.amongusmc.common.CommonManager;
import com.andrei1058.amongusmc.common.party.PartyManager;
import com.andrei1058.amongusmc.common.party.request.PartyRequest;
import com.andrei1058.amoungusmc.common.api.locale.CommonLocaleManager;
import com.andrei1058.amoungusmc.common.api.locale.CommonMessage;
import com.andrei1058.spigot.commandlib.FastRootCommand;
import com.andrei1058.spigot.commandlib.FastSubCommand;
import com.andrei1058.spigot.commandlib.FastSubRootCommand;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class PartyCmd /*extends FastRootCommand*/ {

    /*
    protected PartyCmd() {
        super("party");
        CommonLocaleManager localeManager = CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager();
        withPermAdditions((s) -> s instanceof Player && !CommonManager.getINSTANCE().getCommonProvider().isInGame((Player) s));
        withHeaderContent((s) -> localeManager.getMsg(s, CommonMessage.CMD_PARTY_USAGE_HEADER));
        withHeaderHover((s) -> localeManager.getMsg(s, CommonMessage.CMD_PARTY_USAGE_HEADER));
        withDisplayName((s) -> localeManager.getMsg(s, CommonMessage.CMD_PARTY_DISPLAY_NAME).replace("{name}", getName()));
        withDeniedMsg((s) -> CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getMsg(s, CommonMessage.CMD_PERMISSION_DENIED));
    }*/

    public static void register(FastRootCommand parent) {

        CommonLocaleManager localeManager = CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager();

        //todo disable in setup session
        FastSubRootCommand partyCmd = new FastSubRootCommand("party");
        partyCmd.withAliases(new String[]{"p"}).withPermAdditions((s) -> s instanceof Player)
                .withDescription((s) -> localeManager.getMsg(s, CommonMessage.CMD_PARTY_DESC)).withDisplayHover((s) -> localeManager.getMsg(s, CommonMessage.CMD_PARTY_DESC))
                .withHeaderContent((s) -> localeManager.getMsg(s, CommonMessage.CMD_PARTY_USAGE_HEADER))
                .withPermAdditions(s -> (s instanceof Player) && !CommonManager.getINSTANCE().getCommonProvider().isInSetupSession(s));
        parent.withSubNode(partyCmd);

        FastSubCommand invite = new FastSubCommand("invite");
        FastSubCommand accept = new FastSubCommand("accept");
        FastSubCommand kick = new FastSubCommand("kick");
        FastSubCommand leave = new FastSubCommand("leave");
        FastSubCommand members = new FastSubCommand("members");
        FastSubCommand transfer = new FastSubCommand("transfer");

        partyCmd
                .withSubNode(invite
                        .withAliases(new String[]{"inv", "i"})
                        .withPermAdditions((s) -> s instanceof Player &&
                                ((PartyManager.getINSTANCE().getPartyAdapter().hasParty(((Player) s).getUniqueId()) && PartyManager.getINSTANCE().getPartyAdapter().isOwner(((Player) s).getUniqueId()))
                                        || !PartyManager.getINSTANCE().getPartyAdapter().hasParty(((Player) s).getUniqueId())))
                        .withDescription((s) -> localeManager.getMsg(s, CommonMessage.CMD_PARTY_INV_DESC))
                        .withDisplayHover((s) -> localeManager.getMsg(s, CommonMessage.CMD_PARTY_INV_DESC))
                        .withClickAction(ClickEvent.Action.SUGGEST_COMMAND)
                        .withExecutor((sender, args) -> {
                            Player player = (Player) sender;

                            boolean sendDefault = args.length == 0;
                            if (!sendDefault) {

                                if (PartyManager.getINSTANCE().getPartyAdapter().isPartySizeLimitReached(player.getUniqueId())) {
                                    player.sendMessage(localeManager.getMsg(player, CommonMessage.CMD_PARTY_INV_FAILED2));
                                    return;
                                }

                                PartyRequest.performInvite(player, Arrays.asList(args), "/" + parent.getName() + " " + partyCmd.getName() + " " + accept.getName());
                                return;
                            }

                            player.sendMessage(localeManager.getMsg(player,
                                    CommonMessage.CMD_PARTY_INV_USAGE).replace("{root}", parent.getName()).replace("{cmd}", partyCmd.getName())
                                    .replace("{name}", invite.getName()));
                        })
                        .withTabSuggestions((s) -> {
                            List<String> players = new LinkedList<>();
                            Bukkit.getOnlinePlayers().forEach(p -> players.add(p.getName()));
                            return players;
                        })
                );

        partyCmd
                .withSubNode(accept
                        .withAliases(new String[]{"a", "acc"})
                        .withPermAdditions((s) -> s instanceof Player && !PartyManager.getINSTANCE().getPartyAdapter().hasParty(((Player) s).getUniqueId()))
                        .withDescription((s) -> localeManager.getMsg(s, CommonMessage.CMD_PARTY_ACC_DESC))
                        .withDisplayHover((s) -> localeManager.getMsg(s, CommonMessage.CMD_PARTY_ACC_DESC))
                        .withClickAction(ClickEvent.Action.SUGGEST_COMMAND)
                        .withExecutor((sender, args) -> {
                            Player player = (Player) sender;

                            boolean sendDefault = args.length == 0;
                            if (!sendDefault) {
                                PartyRequest.performAcceptInvite(player, args[0]);
                                return;
                            }

                            player.sendMessage(localeManager.getMsg(player,
                                    CommonMessage.CMD_PARTY_ACC_USAGE).replace("{root}", parent.getName()).replace("{cmd}", partyCmd.getName())
                                    .replace("{name}", invite.getName()));
                        })
                        .withTabSuggestions(PartyRequest::getReceivedRequests)
                );

        partyCmd
                .withSubNode(kick
                        .withAliases(new String[]{"k"})
                        .withPermAdditions((s) -> s instanceof Player && PartyManager.getINSTANCE().getPartyAdapter().isOwner(((Player) s).getUniqueId()))
                        .withDescription((s) -> localeManager.getMsg(s, CommonMessage.CMD_PARTY_KICK_DESC))
                        .withDisplayHover((s) -> localeManager.getMsg(s, CommonMessage.CMD_PARTY_KICK_DESC))
                        .withClickAction(ClickEvent.Action.SUGGEST_COMMAND)
                        .withExecutor((sender, args) -> {
                            Player player = (Player) sender;

                            boolean sendDefault = args.length != 1;
                            if (!sendDefault) {

                                Player toBeKicked = Bukkit.getPlayer(args[0]);
                                UUID targetsPartyOwner = toBeKicked == null ? null : PartyManager.getINSTANCE().getPartyAdapter().getOwner(toBeKicked.getUniqueId());
                                String rawName = toBeKicked == null ? args[0] : toBeKicked.getName();
                                String displayName = toBeKicked == null ? args[0] : toBeKicked.getDisplayName();

                                if (toBeKicked == null || targetsPartyOwner == null || !targetsPartyOwner.equals(player.getUniqueId())) {
                                    player.sendMessage(CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getMsg(player, CommonMessage.CMD_PARTY_KICK_FAILED).replace("{name}", rawName).replace("{name}", displayName));
                                    return;
                                }

                                for (UUID member : PartyManager.getINSTANCE().getPartyAdapter().getMembers(targetsPartyOwner)) {
                                    if (member.equals(targetsPartyOwner)) continue;
                                    Player playerMe = Bukkit.getPlayer(member);
                                    if (playerMe != null && playerMe.isOnline()) {
                                        playerMe.sendMessage(CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getMsg(playerMe, CommonMessage.CMD_PARTY_KICK_BROADCAST).replace("{player}", displayName).replace("{name}", rawName));
                                    }
                                }
                                player.sendMessage(CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getMsg(player, CommonMessage.CMD_PARTY_KICK_BROADCAST).replace("{player}", displayName).replace("{name}", rawName));

                                // if removing target from party returns a disband
                                if (PartyManager.getINSTANCE().getPartyAdapter().removeFromParty(toBeKicked.getUniqueId())) {
                                    player.sendMessage(CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getMsg(player, CommonMessage.CMD_PARTY_DISBAND_BROADCAST));
                                }
                                return;
                            }

                            player.sendMessage(localeManager.getMsg(player,
                                    CommonMessage.CMD_PARTY_KICK_USAGE).replace("{root}", parent.getName()).replace("{cmd}", partyCmd.getName())
                                    .replace("{name}", kick.getName()));
                        })
                        .withTabSuggestions((s) -> {
                            List<String> memberNames = new ArrayList<>();
                            if (s instanceof Player) {
                                PartyManager.getINSTANCE().getPartyAdapter().getMembers(((Player) s).getUniqueId()).forEach(uuid -> {
                                    Player playerMember = Bukkit.getPlayer(uuid);
                                    if (playerMember != null) {
                                        memberNames.add(playerMember.getName());
                                    }
                                });
                            }
                            return memberNames;
                        })
                );

        partyCmd
                .withSubNode(leave
                        .withAliases(new String[]{"l"})
                        .withPermAdditions((s) -> s instanceof Player && PartyManager.getINSTANCE().getPartyAdapter().hasParty(((Player) s).getUniqueId()))
                        .withDescription((s) -> localeManager.getMsg(s, CommonMessage.CMD_PARTY_LEAVE_DESC))
                        .withDisplayHover((s) -> localeManager.getMsg(s, CommonMessage.CMD_PARTY_LEAVE_DESC))
                        .withClickAction(ClickEvent.Action.SUGGEST_COMMAND)
                        .withExecutor((sender, args) -> {
                            Player player = (Player) sender;
                            UUID partyOwner = PartyManager.getINSTANCE().getPartyAdapter().getOwner(player.getUniqueId());
                            Player partyOwnerPlayer = Bukkit.getPlayer(partyOwner);

                            List<UUID> membersCopy = PartyManager.getINSTANCE().getPartyAdapter().getMembers(player.getUniqueId());

                            for (UUID member : membersCopy) {
                                if (member.equals(partyOwner)) continue;
                                // do not send him this message
                                if (player.getUniqueId().equals(member)) continue;
                                Player playerMe = Bukkit.getPlayer(member);
                                if (playerMe != null && playerMe.isOnline()) {
                                    playerMe.sendMessage(CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getMsg(playerMe, CommonMessage.CMD_PARTY_KICK_BROADCAST).replace("{player}", player.getDisplayName()).replace("{name}", player.getName()));
                                }
                            }

                            if (partyOwnerPlayer != null) {
                                partyOwnerPlayer.sendMessage(CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getMsg(partyOwnerPlayer, CommonMessage.CMD_PARTY_KICK_BROADCAST).replace("{player}", player.getDisplayName()).replace("{name}", player.getName()));
                            }

                            // if removing target from party returns a disband
                            if (PartyManager.getINSTANCE().getPartyAdapter().removeFromParty(player.getUniqueId())) {
                                for (UUID member : membersCopy) {
                                    if (member.equals(partyOwner)) continue;
                                    // do not send him this message
                                    if (player.getUniqueId().equals(member)) continue;
                                    Player playerMe = Bukkit.getPlayer(member);
                                    if (playerMe != null && playerMe.isOnline()) {
                                        playerMe.sendMessage(CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getMsg(playerMe, CommonMessage.CMD_PARTY_DISBAND_BROADCAST));
                                    }
                                }
                            }

                            if (partyOwnerPlayer != null) {
                                partyOwnerPlayer.sendMessage(CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getMsg(partyOwnerPlayer, CommonMessage.CMD_PARTY_DISBAND_BROADCAST));
                            }

                            player.sendMessage(localeManager.getMsg(player, CommonMessage.CMD_PARTY_LEAVE_SUCC));
                        })
                );

        partyCmd
                .withSubNode(members
                        .withAliases(new String[]{"m", "players", "member", "list"})
                        .withPermAdditions((s) -> s instanceof Player && PartyManager.getINSTANCE().getPartyAdapter().hasParty(((Player) s).getUniqueId()))
                        .withDescription((s) -> localeManager.getMsg(s, CommonMessage.CMD_PARTY_MEMBERS_DESC))
                        .withDisplayHover((s) -> localeManager.getMsg(s, CommonMessage.CMD_PARTY_MEMBERS_DESC))
                        .withClickAction(ClickEvent.Action.SUGGEST_COMMAND)
                        .withExecutor((sender, args) -> {
                            Player player = (Player) sender;

                            List<UUID> membersUUIDList = PartyManager.getINSTANCE().getPartyAdapter().getMembers(player.getUniqueId());
                            TextComponent membersMessage = new TextComponent(localeManager.getMsg(player, CommonMessage.CMD_PARTY_MEMBERS_MSG));
                            boolean isPartyOwner = PartyManager.getINSTANCE().getPartyAdapter().isOwner(player.getUniqueId());

                            int currentIndex = -1;
                            for (UUID memberUUID : membersUUIDList) {
                                currentIndex++;
                                Player memberPlayer = Bukkit.getPlayer(memberUUID);
                                if (memberPlayer == null) continue;
                                String displayName = memberPlayer.getDisplayName();
                                String playerName = memberPlayer.getName();

                                String currentString = (memberPlayer.isOnline() ? localeManager.getMsg(player, CommonMessage.CMD_PARTY_MEMBERS_FORMAT_ONLINE) :
                                        localeManager.getMsg(player, CommonMessage.CMD_PARTY_MEMBERS_FORMAT_OFFLINE)).replace("{player}", displayName).replace("{name}", playerName);

                                // replace last comma with a dot
                                if (currentIndex == membersUUIDList.size() - 1 && currentString.charAt(currentString.length() - 1) == ',') {
                                    currentString = currentString.substring(0, currentString.length() - 2) + '.';
                                }
                                TextComponent currentMember = new TextComponent(currentString);
                                if (isPartyOwner) {
                                    currentMember.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + parent.getName() + " " + partyCmd.getName() + " " + kick.getName() + " " + memberPlayer.getName()));
                                    currentMember.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent(localeManager.getMsg(player, CommonMessage.CMD_PARTY_MEMBERS_HOVER)
                                            .replace("{player}", displayName).replace("{name}", playerName))}));
                                    membersMessage.addExtra(currentMember);
                                }
                            }

                            player.spigot().sendMessage(membersMessage);
                        })
                );

        partyCmd
                .withSubNode(transfer
                        .withAliases(new String[]{"tran", "transferownership", "give"})
                        .withPermAdditions((s) -> s instanceof Player && PartyManager.getINSTANCE().getPartyAdapter().isOwner(((Player) s).getUniqueId()))
                        .withDescription((s) -> localeManager.getMsg(s, CommonMessage.CMD_PARTY_TRANSFER_DESC))
                        .withDisplayHover((s) -> localeManager.getMsg(s, CommonMessage.CMD_PARTY_TRANSFER_DESC))
                        .withClickAction(ClickEvent.Action.SUGGEST_COMMAND)
                        .withExecutor((sender, args) -> {
                            Player player = (Player) sender;

                            if (args.length != 1) {
                                player.sendMessage(localeManager.getMsg(player,
                                        CommonMessage.CMD_PARTY_TRANSFER_USAGE).replace("{root}", parent.getName()).replace("{cmd}", partyCmd.getName())
                                        .replace("{name}", transfer.getName()));
                                return;
                            }

                            Player givenPlayer = Bukkit.getPlayer(args[0]);
                            if (givenPlayer == null) {
                                player.sendMessage(localeManager.getMsg(player, CommonMessage.CMD_PARTY_TRANSFER_FAILED).replace("{player}", args[0]).replace("{name}", args[0]));
                                return;
                            }

                            // check if the given's player party owner is equal to the command sender
                            UUID targetsPartyOwner = PartyManager.getINSTANCE().getPartyAdapter().getOwner(givenPlayer.getUniqueId());
                            if (targetsPartyOwner != null && targetsPartyOwner.equals(player.getUniqueId())) {
                                if (PartyManager.getINSTANCE().getPartyAdapter().transferOwnership(targetsPartyOwner, givenPlayer.getUniqueId())) {
                                    for (UUID member : PartyManager.getINSTANCE().getPartyAdapter().getMembers(givenPlayer.getUniqueId())) {
                                        if (member.equals(givenPlayer.getUniqueId())) continue;
                                        Player playerMe = Bukkit.getPlayer(member);
                                        if (playerMe != null && playerMe.isOnline()) {
                                            playerMe.sendMessage(CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getMsg(playerMe, CommonMessage.CMD_PARTY_TRANSFER_BROADCAST).replace("{player}", givenPlayer.getDisplayName()).replace("{name}", givenPlayer.getName()));
                                        }
                                    }
                                    givenPlayer.sendMessage(CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getMsg(givenPlayer, CommonMessage.CMD_PARTY_TRANSFER_BROADCAST).replace("{player}", givenPlayer.getDisplayName()).replace("{name}", givenPlayer.getName()));
                                    return;
                                }
                            }
                            player.sendMessage(localeManager.getMsg(player, CommonMessage.CMD_PARTY_TRANSFER_FAILED).replace("{player}", givenPlayer.getDisplayName()).replace("{name}", givenPlayer.getName()));
                        })
                        .withTabSuggestions((s) -> {
                            List<String> memberNames = new ArrayList<>();
                            if (s instanceof Player) {
                                PartyManager.getINSTANCE().getPartyAdapter().getMembers(((Player) s).getUniqueId()).forEach(uuid -> {
                                    Player playerMember = Bukkit.getPlayer(uuid);
                                    if (playerMember != null) {
                                        memberNames.add(playerMember.getName());
                                    }
                                });
                            }
                            return memberNames;
                        })
                );
    }
}
