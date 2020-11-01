package com.andrei1058.amongusmc.server.bungee;

import com.andrei1058.amongusmc.AmongUsMc;
import com.andrei1058.amongusmc.api.arena.Arena;
import com.andrei1058.amoungusmc.common.api.arena.GameState;
import com.andrei1058.amongusmc.api.locale.Message;
import com.andrei1058.amongusmc.api.locale.Locale;
import com.andrei1058.amongusmc.arena.ArenaManager;
import com.andrei1058.amongusmc.common.command.CommonCmdManager;
import com.andrei1058.amoungusmc.common.api.locale.CommonMessage;
import com.andrei1058.amongusmc.language.LanguageManager;
import com.andrei1058.amongusmc.common.party.PartyManager;
import com.andrei1058.amongusmc.server.common.JoinCommonListener;
import com.andrei1058.amongusmc.server.bungee.party.PreLoadedParty;
import com.andrei1058.amoungusmc.common.api.server.CommonPermission;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class JoinQuitListenerBungee implements Listener {

    @EventHandler
    public void onLogin(PlayerLoginEvent e) {
        final Player p = e.getPlayer();

        ProxyUser proxyUser = ProxyUser.getPreLoaded(p.getUniqueId());

        // If is NOT logging in trough BedWarsProxy
        if (proxyUser == null) {
            if (!p.hasPermission("bw.setup")) {
                e.disallow(PlayerLoginEvent.Result.KICK_OTHER, LanguageManager.getINSTANCE().getMsg(p, Message.ARENA_JOIN_DENIED_NO_PROXY));
            }
        } else {
            // If is logging in trough BedWarsProxy
            Locale playerLang = proxyUser.getLanguage() == null ? LanguageManager.getINSTANCE().getDefaultLocale() : proxyUser.getLanguage();

            Arena arena = ArenaManager.getINSTANCE().getArenaById(proxyUser.getArenaId());
            // check if arena is not available, time out etc.
            if (arena == null || proxyUser.isTimedOut() || arena.getGameState() == GameState.ENDING) {
                e.disallow(PlayerLoginEvent.Result.KICK_OTHER, playerLang.getMsg(e.getPlayer(), CommonMessage.ARENA_STATUS_ENDING_NAME));
                proxyUser.destroy("Time out or game unavailable at PlayerLoginEvent");
                return;
            }

            // Player logic
            if (arena.getGameState() == GameState.STARTING || arena.getGameState() == GameState.WAITING) {
                // Vip join/ kick feature
                if (arena.isFull() && ArenaManager.getINSTANCE().hasVipJoin(p)) {
                    boolean canJoin = false;
                    for (Player inGame : arena.getPlayers()) {
                        if (!ArenaManager.getINSTANCE().hasVipJoin(inGame)) {
                            canJoin = true;
                            inGame.kickPlayer(LanguageManager.getINSTANCE().getMsg(inGame, Message.VIP_JOIN_KICKED));
                            break;
                        }
                    }
                    if (!canJoin) {
                        e.disallow(PlayerLoginEvent.Result.KICK_FULL, playerLang.getMsg(e.getPlayer(), Message.VIP_JOIN_DENIED));
                    }
                }
            } else if (arena.getGameState() == GameState.IN_GAME) {
                // Spectator logic
                if (!(!arena.getSpectatePermission().isEmpty() && p.hasPermission(arena.getSpectatePermission()))) {
                    e.disallow(PlayerLoginEvent.Result.KICK_OTHER, playerLang.getMsg(e.getPlayer(), CommonMessage.ARENA_JOIN_DENIED_SPECTATOR));
                }
            }

        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        e.setJoinMessage("");
        final Player p = e.getPlayer();

        ProxyUser proxyUser = ProxyUser.getPreLoaded(p.getUniqueId());

        // If didn't join trough BedWarsProxy
        if (proxyUser == null) {
            // If is an admin let him in to do the setup
            if (p.hasPermission(CommonPermission.ADMIN.get()) || p.hasPermission(CommonPermission.ALL.get())) {
                JoinCommonListener.displayCustomerDetails(p);
                Bukkit.dispatchCommand(p, CommonCmdManager.getINSTANCE().getMainCmd().getName());
                World mainWorld = Bukkit.getWorlds().get(0);
                if (mainWorld != null) {
                    p.teleport(mainWorld.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
                // hide admin to in game users
                for (Player inGame : Bukkit.getOnlinePlayers()) {
                    if (inGame.equals(p)) continue;
                    if (ArenaManager.getINSTANCE().isInArena(inGame)) {
                        inGame.hidePlayer(AmongUsMc.getInstance(), p);
                        p.hidePlayer(AmongUsMc.getInstance(), inGame);
                    }
                }
            } else {
                // The player is not an admin and he joined using /server or equivalent
                p.kickPlayer(LanguageManager.getINSTANCE().getMsg(p, Message.ARENA_JOIN_DENIED_NO_PROXY));
            }
        } else {
            // The player joined using BedWarsProxy
            Locale playerLang = proxyUser.getLanguage() == null ? LanguageManager.getINSTANCE().getDefaultLocale() : proxyUser.getLanguage();

            // There's nothing to re-join, so he might want to join an arena
            Arena arena = ArenaManager.getINSTANCE().getArenaById(proxyUser.getArenaId());

            // Check if the arena is still available or request time-out etc.
            if (arena == null || proxyUser.isTimedOut() || arena.getGameState() == GameState.ENDING) {
                p.kickPlayer(playerLang.getMsg(p, CommonMessage.ARENA_STATUS_ENDING_NAME));
                proxyUser.destroy("Time out or game unavailable at PlayerLoginEvent");
                return;
            }

            // Join allowed, cache player language
            LanguageManager.getINSTANCE().setPlayerLocale(p.getUniqueId(), playerLang, true);
            JoinCommonListener.displayCustomerDetails(p);

            // Join as player
            if (arena.getGameState() == GameState.STARTING || arena.getGameState() == GameState.WAITING) {
                //todo add sounds
                //Sounds.playSound("join-allowed", p);

                // Check for external party integrations
                // If party adapter does not handle remote join itslef
                if (!PartyManager.getINSTANCE().getPartyAdapter().isSelfTeamUpAtRemoteJoin()) {
                    boolean hasParty = PartyManager.getINSTANCE().getPartyAdapter().hasParty(p.getUniqueId());

                    // If party is NOT already created locally
                    if (!hasParty && proxyUser.getPartyOwnerOrSpectateTarget() != null) {

                        // received party owner
                        Player partyOwner = Bukkit.getPlayerExact(proxyUser.getPartyOwnerOrSpectateTarget());

                        // If party owner was not found on the server
                        if (partyOwner == null || !partyOwner.isOnline()) {
                            // If a party member joined before the party owner create a waiting list
                            // to-be-teamed-up players, when the owner will join
                            if (proxyUser.getPartyOwnerOrSpectateTarget() != null) {
                                PreLoadedParty preLoadedParty = PreLoadedParty.getPartyByOwner(proxyUser.getPartyOwnerOrSpectateTarget());
                                if (preLoadedParty == null) {
                                    preLoadedParty = new PreLoadedParty(proxyUser.getPartyOwnerOrSpectateTarget());
                                }
                                preLoadedParty.addMember(p);
                            }
                        } else {
                            // If party owner is connected
                            if (partyOwner.isOnline()) {
                                // If joiner is the party owner create the party and the party is not already created by an external party plugin
                                if (partyOwner.equals(p)) {
                                    // Handle to-be-teamed-up players. A list used if some party members join before the party owner.
                                    PreLoadedParty preLoadedParty = PreLoadedParty.getPartyByOwner(partyOwner.getName());
                                    if (preLoadedParty != null) {
                                        preLoadedParty.teamUp();
                                    }
                                } else {
                                    // Add to a existing party
                                    PartyManager.getINSTANCE().getPartyAdapter().addMember(partyOwner.getUniqueId(), p.getUniqueId());
                                }
                            }
                        }
                    }
                }
                arena.addPlayer(p, true);
            } else {
                // Join as spectator
                Location spectatorTarget = null;
                if (proxyUser.getPartyOwnerOrSpectateTarget() != null) {
                    Player targetPlayer = Bukkit.getPlayer(proxyUser.getPartyOwnerOrSpectateTarget());
                    if (targetPlayer != null) {
                        spectatorTarget = targetPlayer.getLocation();
                    }
                }
                if (!arena.addSpectator(p, spectatorTarget)) {
                    p.kickPlayer(LanguageManager.getINSTANCE().getMsg(p, CommonMessage.ARENA_JOIN_DENIED_SPECTATOR));
                }
            }
            proxyUser.destroy("Joined as player or spectator. PreLoaded user no longer needed.");
        }
    }
}

