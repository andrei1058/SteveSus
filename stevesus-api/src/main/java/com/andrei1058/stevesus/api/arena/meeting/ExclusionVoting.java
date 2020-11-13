package com.andrei1058.stevesus.api.arena.meeting;

import com.andrei1058.stevesus.api.SteveSusAPI;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.team.Team;
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

    public ExclusionVoting(Arena arena) {

    }

    /**
     * @param voted null for skip.
     */
    public boolean addVote(@Nullable Player voted, Player voter, Arena arena) {
        if (hasVoted(voter)) return false;
        if (voted == null) {
            votes.put(voter, null);
            GameSound.VOTE_SOUND.playToPlayers(arena.getPlayers());
            GameSound.VOTE_SOUND.playToPlayers(arena.getSpectators());
            if (arena.isAnonymousVotes()) {
                arena.getPlayers().forEach(player -> player.sendMessage(SteveSusAPI.getInstance().getLocaleHandler().getMsg(player, Message.EXCLUSION_CHAT_ANNOUNCEMENT_ANONYMOUS).replace("{player}", voter.getDisplayName())));
                arena.getSpectators().forEach(player -> player.sendMessage(SteveSusAPI.getInstance().getLocaleHandler().getMsg(player, Message.EXCLUSION_CHAT_ANNOUNCEMENT_ANONYMOUS).replace("{player}", voter.getDisplayName())));
            } else {
                arena.getPlayers().forEach(player -> player.sendMessage(SteveSusAPI.getInstance().getLocaleHandler().getMsg(player, Message.EXCLUSION_CHAT_ANNOUNCEMENT_SKIP).replace("{player}", voter.getDisplayName())));
                arena.getSpectators().forEach(player -> player.sendMessage(SteveSusAPI.getInstance().getLocaleHandler().getMsg(player, Message.EXCLUSION_CHAT_ANNOUNCEMENT_SKIP).replace("{player}", voter.getDisplayName())));
            }
            return true;
        }
        if (!voted.isOnline()) return false;
        if (!arena.isPlayer(voted)) return false;
        votes.put(voter, voted);
        if (arena.isAnonymousVotes()) {
            arena.getPlayers().forEach(player -> player.sendMessage(SteveSusAPI.getInstance().getLocaleHandler().getMsg(player, Message.EXCLUSION_CHAT_ANNOUNCEMENT_ANONYMOUS).replace("{player}", voter.getDisplayName())));
            arena.getSpectators().forEach(player -> player.sendMessage(SteveSusAPI.getInstance().getLocaleHandler().getMsg(player, Message.EXCLUSION_CHAT_ANNOUNCEMENT_ANONYMOUS).replace("{player}", voter.getDisplayName())));
        } else {
            arena.getPlayers().forEach(player -> player.sendMessage(SteveSusAPI.getInstance().getLocaleHandler().getMsg(player, Message.EXCLUSION_CHAT_ANNOUNCEMENT_REGULAR).replace("{player}", voter.getDisplayName()).replace("{target}", voted.getDisplayName())));
            arena.getSpectators().forEach(player -> player.sendMessage(SteveSusAPI.getInstance().getLocaleHandler().getMsg(player, Message.EXCLUSION_CHAT_ANNOUNCEMENT_REGULAR).replace("{player}", voter.getDisplayName()).replace("{target}", voted.getDisplayName())));
        }
        GameSound.VOTE_SOUND.playToPlayers(arena.getPlayers());
        GameSound.VOTE_SOUND.playToPlayers(arena.getSpectators());
        return true;
    }

    public boolean hasVoted(Player player) {
        return votes.containsKey(player);
    }

    /**
     * @param player null to get players who voted to skip.
     */
    public Set<Player> getVotes(@Nullable Player player) {
        if (player == null) {
            return votes.entrySet().stream().filter(e -> e.getValue() == null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getKey)).keySet();
        } else {
            return votes.entrySet().stream().filter(e -> e.getValue() != null && e.getValue().equals(player)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).keySet();
        }
    }

    /**
     * You can retrieve the player that is going to be ejected using {@link #getMostVoted(Arena)} and use it to assign the eject-to-team.
     */
    public void performExclusion(Arena arena, Team excludeToThisTeam) {
        if (excludeToThisTeam == null) throw new IllegalStateException("Provided eject team is null!");

        Player votedOff = getMostVoted(arena);
        Team playerTeam = null;
        if (votedOff != null) {
            playerTeam = arena.getPlayerTeam(votedOff);
            if (playerTeam == null) {
                throw new IllegalStateException("Player team MUSTN'T be null? How did it happen?");
            }
        }

        Team finalPlayerTeam = playerTeam;
        arena.getPlayers().forEach(player -> {
            Locale lang = SteveSusAPI.getInstance().getLocaleHandler().getLocale(player);
            List<String> messages = new ArrayList<>();
            lang.getMsgList(player, Message.EXCLUSION_RESULT_CHAT).forEach(string -> {
                if (string.contains("{votes}")) {
                    votes.forEach((voter, vote) -> {
                        int currentVotes = getVotes(voter).size();
                        if (currentVotes != 0) {
                            messages.add(lang.getMsg(player, Message.EXCLUSION_RESULT_FORMAT_VOTE).replace("{player}", voter.getDisplayName()).replace("{amount}", String.valueOf(currentVotes)));
                        }
                    });
                } else if (string.contains("{exclusion}")) {
                    if (votedOff == null) {
                        messages.add(lang.getMsg(player, Message.EXCLUSION_RESULT_FORMAT_EXCLUSION_SKIP));
                        player.sendTitle(lang.getMsg(player, Message.EXCLUSION_RESULT_TITLE_SKIPPED), lang.getMsg(player, Message.EXCLUSION_RESULT_SUBTITLE_SKIPPED), 8, 50, 8);
                        //TODO SOUND
                    } else {
                        if (arena.isAnonymousVotes()) {
                            messages.add(lang.getMsg(player, Message.EXCLUSION_RESULT_FORMAT_EXCLUSION_EJECTED_ANONYMOUS).replace("{player}", votedOff.getDisplayName()));
                            player.sendTitle(lang.getMsg(player, Message.EXCLUSION_RESULT_TITLE_ANONYMOUS).replace("{player}", votedOff.getDisplayName()), lang.getMsg(player, Message.EXCLUSION_RESULT_SUBTITLE_ANONYMOUS).replace("{player}", votedOff.getDisplayName()), 8, 70, 8);
                            //TODO SOUND
                        } else {
                            if (finalPlayerTeam.isInnocent()) {
                                messages.add(lang.getMsg(player, Message.EXCLUSION_RESULT_FORMAT_EXCLUSION_EJECTED_INNOCENT).replace("{player}", votedOff.getDisplayName()));
                                player.sendTitle(lang.getMsg(player, Message.EXCLUSION_RESULT_TITLE_INNOCENT).replace("{player}", votedOff.getDisplayName()), lang.getMsg(player, Message.EXCLUSION_RESULT_SUBTITLE_INNOCENT).replace("{player}", votedOff.getDisplayName()), 8, 70, 8);
                                //TODO SOUND
                            } else {
                                //TODO SOUND
                                messages.add(lang.getMsg(player, Message.EXCLUSION_RESULT_FORMAT_EXCLUSION_EJECTED_IMPOSTOR).replace("{player}", votedOff.getDisplayName()));
                                player.sendTitle(lang.getMsg(player, Message.EXCLUSION_RESULT_TITLE_IMPOSTOR).replace("{player}", votedOff.getDisplayName()), lang.getMsg(player, Message.EXCLUSION_RESULT_SUBTITLE_IMPOSTOR).replace("{player}", votedOff.getDisplayName()), 8, 70, 8);
                            }
                        }
                    }
                } else {
                    messages.add(string);
                }
            });
            messages.forEach(player::sendMessage);
        });
        if (votedOff != null) {
            playerTeam.removePlayer(votedOff, false);
            excludeToThisTeam.addPlayer(votedOff, false);
        }
    }

    /**
     * If most voted has left SKIP please!
     *
     * @return null if skip is the most voted.
     */
    @Nullable
    public Player getMostVoted(Arena arena) {
        final Player[] mostVoted = {null};
        final int[] votes = {(int) arena.getPlayers().stream().filter(player -> !hasVoted(player)).count()};
        this.votes.values().stream().distinct().forEach(voted -> {
            int currentVotes = (int) this.votes.values().stream().filter(current -> current.equals(voted)).count();
            if (votes[0] < currentVotes) {
                mostVoted[0] = voted;
                votes[0] = currentVotes;
            }
        });
        return mostVoted[0];
    }
}
