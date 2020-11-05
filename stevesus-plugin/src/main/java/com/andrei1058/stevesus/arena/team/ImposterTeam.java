package com.andrei1058.stevesus.arena.team;

import com.andrei1058.stevesus.api.arena.Team;
import com.andrei1058.stevesus.api.locale.Locale;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.language.LanguageManager;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ImposterTeam implements Team {

    private final LinkedList<Player> members = new LinkedList<>();

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
        return !isMember(player);
    }

    @Override
    public void addPlayer(Player player) {
        members.removeIf(member -> member.getUniqueId().equals(player.getUniqueId()));
        members.add(player);
    }

    @Override
    public void removePlayer(Player player, boolean abandon) {
        members.remove(player);
    }

    @Override
    public String getDisplayName(Player player) {
        return LanguageManager.getINSTANCE().getLocale(player).getMsg(player, Message.TEAM_NAME_PATH + getIdentifier());
    }

    @Override
    public String getDisplayName(Locale locale) {
        return locale.getMsg(null, Message.TEAM_NAME_PATH + getIdentifier());
    }

    @Override
    public String getIdentifier() {
        return "imposter";
    }
}
