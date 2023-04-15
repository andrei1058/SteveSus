package com.andrei1058.stevesus.api.arena.sabotage;

import com.andrei1058.stevesus.api.arena.meeting.MeetingStage;
import com.andrei1058.stevesus.api.arena.team.Team;
import com.andrei1058.stevesus.common.CommonManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SabotageCooldown {

    // it can be paused when there is a single impostor venting etc or when both impostors
    private boolean paused = false;
    private final Team team;
    private final Team ghostTeam;
    private int coolDownRate;
    // how many seconds at paused state
    private int secondsAtPaused;
    private long nextAllowed;

    // paseaza team-ul pentru a verfica cand dai pause sau unpause daca mai e cineva in vent din acest team
    public SabotageCooldown(Team aliveTeam, Team ghostTeam, int coolDownRate) {
        this.team = aliveTeam;
        this.ghostTeam = ghostTeam;
        this.coolDownRate = coolDownRate;
    }

    public void tryUnPause() {
        if (paused) {
            // do not unPause if there are active sabotages
            if (getTeam().getArena().getActiveSabotages() != 0) {
                return;
            }
            if (getTeam().getArena().getVentHandler() != null) {
                // do not un-pause if there is a teammate venting
                if (getTeam().getMembers().stream().anyMatch(player -> getTeam().getArena().getVentHandler().isVenting(player))) {
                    return;
                }
            }

            paused = false;
            nextAllowed = System.currentTimeMillis() + (secondsAtPaused * 1000);
            updateCooldownOnItems();
        }
    }

    public void tryPause() {
        if (!paused) {
            // skip checks and pause if there are active sabotages
            // skip if no meeting as well
            if (getTeam().getArena().getActiveSabotages() == 0 && getTeam().getArena().getMeetingStage() == MeetingStage.NO_MEETING) {
                if (getTeam().getArena().getVentHandler() != null) {
                    // do not pause if not all impostors are venting
                    if (!getTeam().getMembers().stream().allMatch(player -> getTeam().getArena().getVentHandler().isVenting(player))) {
                        return;
                    }
                }
            }
            // this before pausing
            secondsAtPaused = getCurrentSeconds();
            paused = true;
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public int getCoolDownRate() {
        return coolDownRate;
    }

    public Team getTeam() {
        return team;
    }

    public void setCoolDownRate(int coolDownRate) {
        this.coolDownRate = coolDownRate;
    }

    public int getCurrentSeconds() {
        if (paused) {
            return secondsAtPaused;
        }
        int current = (int) ((nextAllowed - System.currentTimeMillis()) / 1000);
        return Math.max(current, 0);
    }

    public void applyCooldown() {
        nextAllowed = System.currentTimeMillis() + (coolDownRate * 1000);

        for (Player player : getTeam().getMembers()) {
            updateCooldownOnItems(player, player.getInventory());
        }
        for (Player player : ghostTeam.getMembers()) {
            updateCooldownOnItems(player, player.getInventory());
        }
    }

    public void applyStartCooldown() {
        nextAllowed = System.currentTimeMillis() + (getTeam().getArena().getLiveSettings().getSabotageCooldown().getMinValue() * 1000);

        for (Player player : getTeam().getMembers()) {
            updateCooldownOnItems(player, player.getInventory());
        }
        for (Player player : ghostTeam.getMembers()) {
            updateCooldownOnItems(player, player.getInventory());
        }
    }

    /**
     * Open sabotage cool down on inventory items.
     * Like sabotages GUI.
     * There is no need to use this after {@link #applyCooldown()} on impostor inventories.
     *
     * @param player    cool down receiver.
     * @param inventory inventory where to check items.
     */
    public void updateCooldownOnItems(Player player, Inventory inventory) {
        int seconds = getCurrentSeconds();
        for (ItemStack itemStack : inventory.getContents()) {
            String tag = CommonManager.getINSTANCE().getItemSupport().getTag(itemStack, "interact");
            if (tag != null && tag.startsWith("sabotage")) {
                // * 20 because it is in ticks
                player.setCooldown(itemStack.getType(), seconds * 20);
            }
        }
    }

    public void updateCooldownOnItems() {
        for (Player player : getTeam().getMembers()) {
            updateCooldownOnItems(player, player.getInventory());
        }
        for (Player player : ghostTeam.getMembers()) {
            updateCooldownOnItems(player, player.getInventory());
        }
    }
}
