package dev.andrei1058.game.arena.team;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.arena.team.Team;
import dev.andrei1058.game.api.locale.Locale;
import dev.andrei1058.game.api.locale.Message;
import dev.andrei1058.game.commanditem.CommandItemsManager;
import dev.andrei1058.game.common.api.arena.GameState;
import dev.andrei1058.game.language.LanguageManager;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class GhostImpostorTeam implements Team {

    private final LinkedList<Player> members = new LinkedList<>();
    private boolean canVote = false;
    private final GameArena gameArena;
    private int teamSize;

    public GhostImpostorTeam(GameArena gameArena) {
        this.gameArena = gameArena;
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
        if (gameStartAssign) return false;
        if (getArena().getGameState() != GameState.IN_GAME) return false;
        if (getArena().getPlayerTeam(player) != null) return false;
        members.removeIf(member -> member.getUniqueId().equals(player.getUniqueId()));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
        player.getInventory().clear();
        CommandItemsManager.sendCommandItems(player, CommandItemsManager.CATEGORY_IMPOSTOR_GHOST);
        return members.add(player);
    }

    @Override
    public void removePlayer(Player player) {
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
        return "impostor-ghost";
    }

    @Override
    public boolean canVote() {
        return canVote;
    }

    @Override
    public boolean canHaveTasks() {
        return false;
    }

    @Override
    public void setCanVote(boolean toggle) {
        this.canVote = toggle;
    }

    @Override
    public boolean canReportBody() {
        return false;
    }

    @Override
    public boolean canUseMeetingButton() {
        return false;
    }

    @Override
    public GameArena getArena() {
        return gameArena;
    }

    @Override
    public boolean chatFilter(Player player) {
        Team playerTeam = getArena().getPlayerTeam(player);
        return playerTeam == null || !playerTeam.getIdentifier().endsWith("-ghost") && playerTeam.isInnocent();
    }

    @Override
    public boolean isInnocent() {
        return false;
    }

    @Override
    public boolean canBeVoted() {
        return false;
    }

    public int getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(int teamSize) {
        this.teamSize = teamSize;
    }
}
