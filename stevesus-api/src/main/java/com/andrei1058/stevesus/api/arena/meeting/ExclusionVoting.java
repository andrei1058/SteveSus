package com.andrei1058.stevesus.api.arena.meeting;

import com.andrei1058.stevesus.api.SteveSusAPI;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.team.Team;
import com.andrei1058.stevesus.api.locale.ChatUtil;
import com.andrei1058.stevesus.api.locale.Locale;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.api.server.GameSound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ExclusionVoting {

    // voter, candidate
    private final HashMap<Player, Player> votes = new HashMap<>();

    /**
     * @param voted null for skip.
     */
    public boolean addVote(@Nullable Player voted, Player voter, Arena arena) {
        if (hasVoted(voter)) return false;
        Team team = arena.getPlayerTeam(voter);
        if (team == null) return false;
        if (!team.canVote()) return false;
        if (voted == null) {
            votes.put(voter, null);
            GameSound.VOTE_SOUND.playToPlayers(arena.getPlayers());
            GameSound.VOTE_SOUND.playToPlayers(arena.getSpectators());
            if (arena.getLiveSettings().isAnonymousVotes()) {
                arena.getPlayers().forEach(player -> player.sendMessage(SteveSusAPI.getInstance().getLocaleHandler().getMsg(player, Message.EXCLUSION_CHAT_ANNOUNCEMENT_ANONYMOUS).replace("{player}", voter.getDisplayName())));
                arena.getSpectators().forEach(player -> player.sendMessage(SteveSusAPI.getInstance().getLocaleHandler().getMsg(player, Message.EXCLUSION_CHAT_ANNOUNCEMENT_ANONYMOUS).replace("{player}", voter.getDisplayName())));
            } else {
                arena.getPlayers().forEach(player -> player.sendMessage(SteveSusAPI.getInstance().getLocaleHandler().getMsg(player, Message.EXCLUSION_CHAT_ANNOUNCEMENT_SKIP).replace("{player}", voter.getDisplayName())));
                arena.getSpectators().forEach(player -> player.sendMessage(SteveSusAPI.getInstance().getLocaleHandler().getMsg(player, Message.EXCLUSION_CHAT_ANNOUNCEMENT_SKIP).replace("{player}", voter.getDisplayName())));
            }
            // short time if all voted
            if (arena.getPlayers().stream().noneMatch(inGame -> {
                Team playerTeam = arena.getPlayerTeam(inGame);
                return playerTeam != null && playerTeam.canVote() && !hasVoted(inGame);
            })) {
                arena.setCountdown(5);
            }
            return true;
        }
        if (!voted.isOnline()) return false;
        if (!arena.isPlayer(voted)) return false;
        votes.put(voter, voted);
        if (arena.getLiveSettings().isAnonymousVotes()) {
            arena.getPlayers().forEach(player -> player.sendMessage(SteveSusAPI.getInstance().getLocaleHandler().getMsg(player, Message.EXCLUSION_CHAT_ANNOUNCEMENT_ANONYMOUS).replace("{player}", voter.getDisplayName())));
            arena.getSpectators().forEach(player -> player.sendMessage(SteveSusAPI.getInstance().getLocaleHandler().getMsg(player, Message.EXCLUSION_CHAT_ANNOUNCEMENT_ANONYMOUS).replace("{player}", voter.getDisplayName())));
        } else {
            arena.getPlayers().forEach(player -> player.sendMessage(SteveSusAPI.getInstance().getLocaleHandler().getMsg(player, Message.EXCLUSION_CHAT_ANNOUNCEMENT_REGULAR).replace("{player}", voter.getDisplayName()).replace("{target}", voted.getDisplayName())));
            arena.getSpectators().forEach(player -> player.sendMessage(SteveSusAPI.getInstance().getLocaleHandler().getMsg(player, Message.EXCLUSION_CHAT_ANNOUNCEMENT_REGULAR).replace("{player}", voter.getDisplayName()).replace("{target}", voted.getDisplayName())));
        }
        GameSound.VOTE_SOUND.playToPlayers(arena.getPlayers());
        GameSound.VOTE_SOUND.playToPlayers(arena.getSpectators());

        // short time if all voted
        if (arena.getPlayers().stream().noneMatch(inGame -> {
            Team playerTeam = arena.getPlayerTeam(inGame);
            return playerTeam != null && playerTeam.canVote() && !hasVoted(inGame);
        })) {
            arena.setCountdown(5);
        }
        return true;
    }

    public boolean hasVoted(Player player) {
        return votes.containsKey(player);
    }

    /**
     * @param player null to get players who voted to skip.
     */
    public Set<Player> getVotes(@Nullable Player player, Arena arena) {
        if (player == null) {
            Set<Player> skipped = new HashSet<>();
            for (Map.Entry<Player, Player> votes : votes.entrySet()) {
                if (votes.getValue() == null) {
                    skipped.add(votes.getKey());
                }
            }
            for (Player inGame : arena.getPlayers()) {
                Team team = arena.getPlayerTeam(inGame);
                if (team.canVote()) {
                    if (!hasVoted(inGame)) {
                        skipped.add(inGame);
                    }
                }
            }
            return skipped;
        } else {
            return votes.entrySet().stream().filter(e -> e.getValue() != null && e.getValue().equals(player)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).keySet();
        }
    }

    /**
     * You can retrieve the player that is going to be ejected using {@link #getMostVoted(Arena)} and use it to assign the eject-to-team.
     *
     * @param excludeToThisTeam null if you want to automatically assign to its equivalent ghost team.
     *                          For crew members it will search a team named "crew-ghost", for impostor team will search "impostor-ghost" etc.
     *                          So if you want to exclude to a custom team create a team ending with "-ghost".
     *                          If no team is found the excluded player will be switched to spectator.
     */
    public void performExclusion(Arena arena, Team excludeToThisTeam) {
        Player votedOff = getMostVoted(arena);
        Team playerTeam = null;
        if (votedOff != null) {
            playerTeam = arena.getPlayerTeam(votedOff);
            if (playerTeam == null) {
                throw new IllegalStateException("Player team MUSTN'T be null? How did it happen?");
            }
        }

        LinkedHashMap<Player, Integer> results = new LinkedHashMap<>();
        int skip = getVotes(null, arena).size();
        int mostVotes = 0;
        for (Player voter : votes.keySet()) {
            int currentVotes = getVotes(voter, arena).size();
            results.put(voter, currentVotes);
            // used for draw check
            if (currentVotes > mostVotes) {
                mostVotes = currentVotes;
            }
        }
        // check if draw
        boolean tie = false;
        if (mostVotes > 0 && skip <= mostVotes) {
            if (skip == mostVotes) {
                tie = true;
            } else {
                int finalMostVotes = mostVotes;
                int times = (int) results.values().stream().filter(value -> value == finalMostVotes).count();
                if (times > 1) {
                    tie = true;
                }
            }
        }
        // skip
        if (skip > mostVotes) {
            votedOff = null;
            playerTeam = null;
        }

        Team finalPlayerTeam = playerTeam;
        for (Player player : arena.getPlayers()) {
            Locale lang = SteveSusAPI.getInstance().getLocaleHandler().getLocale(player);
            List<String> messages = new ArrayList<>();
            for (String string : lang.getMsgList(player, Message.EXCLUSION_RESULT_CHAT)) {
                if (string.contains("{votes}")) {
                    for (Map.Entry<Player, Integer> entry : results.entrySet()) {
                        if (entry.getValue() != 0) {
                            messages.add(ChatUtil.centerMessage(lang.getMsg(player, Message.EXCLUSION_RESULT_FORMAT_VOTE).replace("{player}", entry.getKey().getDisplayName()).replace("{amount}", String.valueOf(entry.getValue()))));
                        }
                    }
                    if (skip != 0) {
                        messages.add(ChatUtil.centerMessage(lang.getMsg(player, Message.EXCLUSION_RESULT_FORMAT_VOTED_SKIP).replace("{amount}", String.valueOf(skip))));
                    }
                } else if (string.contains("{exclusion}")) {
                    if (tie) {
                        messages.add(ChatUtil.centerMessage(lang.getMsg(player, Message.EXCLUSION_RESULT_FORMAT_EXCLUSION_TIE)));
                    } else if (votedOff == null) {
                        messages.add(ChatUtil.centerMessage(lang.getMsg(player, Message.EXCLUSION_RESULT_FORMAT_EXCLUSION_SKIP)));
                    } else {
                        if (!arena.getLiveSettings().isConfirmEjects()) {
                            if (player.equals(votedOff)) {
                                messages.add(ChatUtil.centerMessage(lang.getMsg(player, Message.EXCLUSION_RESULT_FORMAT_EXCLUSION_EJECTED_SELF).replace("{player}", votedOff.getDisplayName())));
                            } else {
                                messages.add(ChatUtil.centerMessage(lang.getMsg(player, Message.EXCLUSION_RESULT_FORMAT_EXCLUSION_EJECTED_ANONYMOUS).replace("{player}", votedOff.getDisplayName())));
                            }
                        } else {
                            if (player.equals(votedOff)) {
                                messages.add(ChatUtil.centerMessage(lang.getMsg(player, Message.EXCLUSION_RESULT_FORMAT_EXCLUSION_EJECTED_SELF).replace("{player}", votedOff.getDisplayName())));
                            } else if (finalPlayerTeam.isInnocent()) {
                                messages.add(ChatUtil.centerMessage(lang.getMsg(player, Message.EXCLUSION_RESULT_FORMAT_EXCLUSION_EJECTED_INNOCENT).replace("{player}", votedOff.getDisplayName())));
                            } else {
                                messages.add(ChatUtil.centerMessage(lang.getMsg(player, Message.EXCLUSION_RESULT_FORMAT_EXCLUSION_EJECTED_IMPOSTOR).replace("{player}", votedOff.getDisplayName())));
                            }
                        }
                    }
                } else {
                    messages.add(ChatUtil.centerMessage(string));
                }
            }
            if (tie) {
                votedOff = null;
                player.sendTitle(lang.getMsg(player, Message.EXCLUSION_RESULT_TITLE_TIE), lang.getMsg(player, Message.EXCLUSION_RESULT_SUBTITLE_TIE), 8, 70, 8);
            } else if (votedOff == null) {
                player.sendTitle(lang.getMsg(player, Message.EXCLUSION_RESULT_TITLE_SKIPPED), lang.getMsg(player, Message.EXCLUSION_RESULT_SUBTITLE_SKIPPED), 8, 70, 8);
            } else if (!arena.getLiveSettings().isConfirmEjects()) {
                if (player.equals(votedOff)) {
                    player.sendTitle(lang.getMsg(player, Message.EXCLUSION_RESULT_TITLE_SELF).replace("{player}", votedOff.getDisplayName()), lang.getMsg(player, Message.EXCLUSION_RESULT_SUBTITLE_SELF).replace("{player}", votedOff.getDisplayName()), 8, 70, 8);
                } else {
                    player.sendTitle(lang.getMsg(player, Message.EXCLUSION_RESULT_TITLE_ANONYMOUS).replace("{player}", votedOff.getDisplayName()), lang.getMsg(player, Message.EXCLUSION_RESULT_SUBTITLE_ANONYMOUS).replace("{player}", votedOff.getDisplayName()), 8, 70, 8);
                }
            } else {
                if (player.equals(votedOff)) {
                    player.sendTitle(lang.getMsg(player, Message.EXCLUSION_RESULT_TITLE_SELF).replace("{player}", votedOff.getDisplayName()), lang.getMsg(player, Message.EXCLUSION_RESULT_SUBTITLE_SELF).replace("{player}", votedOff.getDisplayName()), 8, 70, 8);
                } else if (finalPlayerTeam.isInnocent()) {
                    player.sendTitle(lang.getMsg(player, Message.EXCLUSION_RESULT_TITLE_INNOCENT).replace("{player}", votedOff.getDisplayName()), lang.getMsg(player, Message.EXCLUSION_RESULT_SUBTITLE_INNOCENT).replace("{player}", votedOff.getDisplayName()), 8, 70, 8);
                } else {
                    player.sendTitle(lang.getMsg(player, Message.EXCLUSION_RESULT_TITLE_IMPOSTOR).replace("{player}", votedOff.getDisplayName()), lang.getMsg(player, Message.EXCLUSION_RESULT_SUBTITLE_IMPOSTOR).replace("{player}", votedOff.getDisplayName()), 8, 70, 8);
                }
            }
            for (String message : messages) {
                player.sendMessage(message);
            }
        }
        if (votedOff == null) {
            GameSound.VOTE_EJECT_NONE.playToPlayers(arena.getPlayers());
            GameSound.VOTE_EJECT_NONE.playToPlayers(arena.getSpectators());
        } else {
            if (!arena.getLiveSettings().isConfirmEjects()) {
                GameSound.VOTE_EJECT_ANONYMOUS.playToPlayers(arena.getPlayers());
                GameSound.VOTE_EJECT_ANONYMOUS.playToPlayers(arena.getSpectators());
            } else if (playerTeam.isInnocent()) {
                GameSound.VOTE_EJECT_INNOCENT.playToPlayers(arena.getPlayers());
                GameSound.VOTE_EJECT_INNOCENT.playToPlayers(arena.getSpectators());
            } else {
                GameSound.VOTE_EJECT_IMPOSTOR.playToPlayers(arena.getPlayers());
                GameSound.VOTE_EJECT_IMPOSTOR.playToPlayers(arena.getSpectators());
            }
            playerTeam.removePlayer(votedOff);
            String playerTeamIdentifier = playerTeam.getIdentifier();
            if (excludeToThisTeam == null) {
                Team ghostTeam = arena.getGameTeams().stream().filter(team -> team.getIdentifier().equals(playerTeamIdentifier + "-ghost")).findFirst().orElse(null);
                if (ghostTeam == null) {
                    arena.switchToSpectator(votedOff);
                } else {
                    ghostTeam.addPlayer(votedOff, false);
                }
            } else {
                excludeToThisTeam.addPlayer(votedOff, false);
            }
        }
        votes.clear();
    }

    /**
     * If most voted has left will skip.
     * Tie is handled at {@link #performExclusion(Arena, Team)}.
     *
     * @return null if skip is the most voted.
     */
    @Nullable
    public Player getMostVoted(Arena arena) {
        final Player[] mostVoted = {null};
        final int[] votes = {(int) arena.getPlayers().stream().filter(player -> !hasVoted(player)).count()};
        this.votes.values().stream().distinct().forEach(voted -> {
            int currentVotes = (int) this.votes.values().stream().filter(current -> voted == null ? current == null : current == null || current.equals(voted)).count();
            if (votes[0] < currentVotes) {
                mostVoted[0] = voted;
                votes[0] = currentVotes;
            }
        });
        return mostVoted[0] == null ? null : mostVoted[0].isOnline() ? mostVoted[0] : null;
    }
}
