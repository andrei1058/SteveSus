package dev.andrei1058.game.arena.team;

import dev.andrei1058.game.api.arena.Arena;
import dev.andrei1058.game.api.arena.team.Team;
import dev.andrei1058.game.api.locale.Locale;
import dev.andrei1058.game.api.locale.Message;
import dev.andrei1058.game.common.api.arena.GameState;
import dev.andrei1058.game.language.LanguageManager;
import org.bukkit.EntityEffect;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.LinkedList;
import java.util.List;

public class GhostCrewTeam implements Team {

    private final LinkedList<Player> members = new LinkedList<>();
    private final Arena arena;

    public GhostCrewTeam(Arena arena) {
        this.arena = arena;
    }

    @Override
    public List<Player> getMembers() {
        return members;
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
        Locale lang = LanguageManager.getINSTANCE().getLocale(player);
        player.sendTitle(lang.getMsg(player, Message.YOU_DIED_TITLE), lang.getMsg(player, Message.YOU_DIED_SUBTITLE), 10, 60, 10);
        lang.getMsgList(player, Message.YOU_DIED_CHAT_CREW).forEach(player::sendMessage);
        player.playEffect(EntityEffect.TOTEM_RESURRECT);
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
        return "crew-ghost";
    }

    @Override
    public boolean canVote() {
        return false;
    }

    @Override
    public boolean canHaveTasks() {
        return true;
    }

    @Override
    public void setCanVote(boolean toggle) {

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
    public Arena getArena() {
        return arena;
    }

    @Override
    public boolean chatFilter(Player player) {
        Team playerTeam = getArena().getPlayerTeam(player);
        return playerTeam == null || !playerTeam.equals(this);
    }

    @Override
    public boolean isInnocent() {
        return true;
    }

    @Override
    public boolean canBeVoted() {
        return false;
    }
}
