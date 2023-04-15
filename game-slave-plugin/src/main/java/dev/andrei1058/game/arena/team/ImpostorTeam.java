package dev.andrei1058.game.arena.team;

import dev.andrei1058.game.api.SteveSusAPI;
import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.arena.team.Team;
import dev.andrei1058.game.api.locale.ChatUtil;
import dev.andrei1058.game.api.locale.Locale;
import dev.andrei1058.game.api.locale.Message;
import dev.andrei1058.game.api.server.GameSound;
import dev.andrei1058.game.arena.ability.kill.KillListener;
import dev.andrei1058.game.commanditem.CommandItemsManager;
import dev.andrei1058.game.common.api.arena.GameState;
import dev.andrei1058.game.language.LanguageManager;
import org.bukkit.entity.Player;

import java.util.*;

public class ImpostorTeam implements Team {

    private final LinkedList<Player> members = new LinkedList<>();
    private boolean canVote = true;
    private final GameArena gameArena;

    public ImpostorTeam(GameArena gameArena) {
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
        Team team = gameArena.getPlayerTeam(player);
        return !isMember(player) && team != null && team.isInnocent() && !team.getIdentifier().endsWith("-ghost");
    }

    @Override
    public boolean addPlayer(Player player, boolean gameStartAssign) {
        if (!gameStartAssign) return false;
        if (gameArena.getPlayers().size() >= 10) {
            if (gameArena.getLiveSettings().getImpostors().getCurrentValue() == getMembers().size()) {
                return false;
            }
        } else {
            if (gameArena.getLiveSettings().getImpostors().getMinValue() == getMembers().size()){
                return false;
            }
        }
        if (getArena().getPlayerTeam(player) != null) return false;
        if (getArena().getGameState() != GameState.IN_GAME) return false;
        members.removeIf(member -> member.getUniqueId().equals(player.getUniqueId()));
        CommandItemsManager.sendCommandItems(player, CommandItemsManager.CATEGORY_IMPOSTOR);
        GameSound.GAME_START_IMPOSTOR.playToPlayer(player);
        KillListener.updateKillItem(getArena(), player, getArena().getLiveSettings().getKillCooldown().getMinValue());
        Locale lang = SteveSusAPI.getInstance().getLocaleHandler().getLocale(player);
        player.sendTitle(lang.getMsg(player, Message.GAME_START_IMPOSTOR_TITLE), lang.getMsg(player, Message.GAME_START_IMPOSTOR_SUBTITLE), 10, 30, 10);
        for (String string : lang.getMsgList(player, Message.GAME_START_IMPOSTOR_CHAT)) {
            string = ChatUtil.centerMessage(string);
            player.sendMessage(string);
        }
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
        return "impostor";
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
        return true;
    }

    @Override
    public boolean canUseMeetingButton() {
        return true;
    }

    @Override
    public GameArena getArena() {
        return gameArena;
    }

    @Override
    public boolean chatFilter(Player player) {
        return false;
    }

    @Override
    public boolean isInnocent() {
        return false;
    }

    @Override
    public boolean canBeVoted() {
        return true;
    }
}
