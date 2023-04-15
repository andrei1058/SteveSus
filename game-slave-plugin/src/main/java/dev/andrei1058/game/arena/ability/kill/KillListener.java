package dev.andrei1058.game.arena.ability.kill;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.arena.GameListener;
import dev.andrei1058.game.api.arena.PlayerCorpse;
import dev.andrei1058.game.api.arena.meeting.MeetingStage;
import dev.andrei1058.game.api.arena.team.Team;
import dev.andrei1058.game.api.arena.vent.Vent;
import dev.andrei1058.game.api.server.PlayerCoolDown;
import dev.andrei1058.game.common.CommonManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.UUID;

public class KillListener implements GameListener {

    private final LinkedHashMap<UUID, Integer> cachedKillCoolDown = new LinkedHashMap<>();

    @Override
    public void onPlayerKill(GameArena gameArena, Player killer, Player victim, Team destinationTeam, PlayerCorpse corpse) {
        updateKillItem(gameArena, killer, gameArena.getLiveSettings().getKillCooldown().getCurrentValue());
    }

    @Override
    public void onMeetingStageChange(GameArena gameArena, MeetingStage oldStage, MeetingStage newStage) {
        if (newStage == MeetingStage.NO_MEETING) {
            // apply cool down
            for (Team team : gameArena.getGameTeams()) {
                if (!team.isInnocent()) {
                    for (Player player : team.getMembers()) {
                        updateKillItem(gameArena, player, gameArena.getLiveSettings().getKillCooldown().getCurrentValue());
                    }
                }
            }
        } else {
            cachedKillCoolDown.clear();
        }
    }


    @Override
    public void onPlayerVent(GameArena gameArena, Player player, Vent vent) {
         // cache player kill cool down before venting
        cachedKillCoolDown.clear();
        PlayerCoolDown coolDown = PlayerCoolDown.getPlayerData(player);
        if (coolDown != null) {
            cachedKillCoolDown.put(player.getUniqueId(), coolDown.getCoolDown("kill"));
        }
    }

    @Override
    public void onPlayerUnVent(GameArena gameArena, Player player, Vent vent) {
        // re-apply kill cool down because it is paused during venting
        if (cachedKillCoolDown.containsKey(player.getUniqueId())) {
            int seconds = cachedKillCoolDown.remove(player.getUniqueId());
            if (seconds > 0) {
                updateKillItem(gameArena, player, seconds);
            }
        }
    }

    @Override
    public void onPlayerLeave(GameArena gameArena, Player player, boolean spectator) {
        cachedKillCoolDown.remove(player.getUniqueId());
    }

    public static void updateKillItem(GameArena gameArena, Player player, int seconds) {
        for (ItemStack item : player.getInventory()) {
            String tag = CommonManager.getINSTANCE().getItemSupport().getTag(item, "interact");
            if (tag != null && tag.equals("kill")) {
                PlayerCoolDown coolDown = PlayerCoolDown.getOrCreatePlayerData(player);
                player.setCooldown(item.getType(), seconds * 20);
                coolDown.updateCoolDown("kill", seconds);
            }
        }
    }
}
