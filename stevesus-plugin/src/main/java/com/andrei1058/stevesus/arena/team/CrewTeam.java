package com.andrei1058.stevesus.arena.team;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.team.Team;
import com.andrei1058.stevesus.api.locale.Locale;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.common.api.arena.GameState;
import com.andrei1058.stevesus.language.LanguageManager;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CrewTeam implements Team {

    private final LinkedList<Player> members = new LinkedList<>();
    private boolean canVote = true;
    private final Arena arena;

    public CrewTeam(Arena arena){
        this.arena = arena;
    }

    @Override
    public List<Player> getMembers() {
        return Collections.unmodifiableList(members);
    }

    @Override
    public boolean isMember(Player player) {
        return members.contains(player);
    }

    @Override
    public boolean canKill(Player player) {
        return false;
    }

    @Override
    public boolean addPlayer(Player player, boolean gameStartAssign) {
        if (!gameStartAssign) return false;
        if (getArena().getGameState() != GameState.IN_GAME) return false;
        if (getArena().getPlayerTeam(player) != null) return false;
        members.removeIf(member -> member.getUniqueId().equals(player.getUniqueId()));
        return members.add(player);
    }

    @Override
    public void removePlayer(Player player, boolean abandon) {
        members.remove(player);
    }

    @Override
    public String getDisplayName(Player player) {
        return LanguageManager.getINSTANCE().getLocale(player).getMsg(player, Message.TEAM_NAME_PATH_ + getIdentifier());
    }

    @Override
    public String getDisplayName(Locale locale) {
        return locale.getMsg(null, Message.TEAM_NAME_PATH_ + getIdentifier());
    }

    @Override
    public String getIdentifier() {
        return "crew";
    }

    @Override
    public boolean canVote() {
        return canVote;
    }

    @Override
    public boolean canHaveTasks() {
        return true;
    }

    @Override
    public void setCanVote(boolean toggle) {
        this.canVote = toggle;
    }

    @Override
    public boolean canReportBody() {
        return true;
    }

    @Override
    public boolean canUseMeetingButton() {
        return true;
    }

    @Override
    public Arena getArena() {
        return arena;
    }
}
