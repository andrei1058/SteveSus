package com.andrei1058.stevesus.arena.ability.kill;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.GameListener;
import com.andrei1058.stevesus.api.arena.PlayerCorpse;
import com.andrei1058.stevesus.api.arena.meeting.MeetingStage;
import com.andrei1058.stevesus.api.arena.team.Team;
import com.andrei1058.stevesus.api.arena.vent.Vent;
import com.andrei1058.stevesus.api.server.PlayerCoolDown;
import com.andrei1058.stevesus.common.CommonManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.UUID;

public class KillListener implements GameListener {

    private final LinkedHashMap<UUID, Integer> cachedKillCoolDown = new LinkedHashMap<>();

    @Override
    public void onPlayerKill(Arena arena, Player killer, Player victim, Team destinationTeam, PlayerCorpse corpse) {
        updateKillItem(arena, killer, arena.getLiveSettings().getKillCooldown().getCurrentValue());
    }

    @Override
    public void onMeetingStageChange(Arena arena, MeetingStage oldStage, MeetingStage newStage) {
        if (newStage == MeetingStage.NO_MEETING) {
            // apply cool down
            for (Team team : arena.getGameTeams()) {
                if (!team.isInnocent()) {
                    for (Player player : team.getMembers()) {
                        updateKillItem(arena, player, arena.getLiveSettings().getKillCooldown().getCurrentValue());
                    }
                }
            }
        } else {
            cachedKillCoolDown.clear();
        }
    }


    @Override
    public void onPlayerVent(Arena arena, Player player, Vent vent) {
         // cache player kill cool down before venting
        cachedKillCoolDown.clear();
        PlayerCoolDown coolDown = PlayerCoolDown.getPlayerData(player);
        if (coolDown != null) {
            cachedKillCoolDown.put(player.getUniqueId(), coolDown.getCoolDown("kill"));
        }
    }

    @Override
    public void onPlayerUnVent(Arena arena, Player player, Vent vent) {
        // re-apply kill cool down because it is paused during venting
        if (cachedKillCoolDown.containsKey(player.getUniqueId())) {
            int seconds = cachedKillCoolDown.remove(player.getUniqueId());
            if (seconds > 0) {
                updateKillItem(arena, player, seconds);
            }
        }
    }

    @Override
    public void onPlayerLeave(Arena arena, Player player, boolean spectator) {
        cachedKillCoolDown.remove(player.getUniqueId());
    }

    public static void updateKillItem(Arena arena, Player player, int seconds) {
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
