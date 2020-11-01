package com.andrei1058.stevesus.common.party.listener;

import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.party.adapter.InternalPartyAdapter;
import com.andrei1058.stevesus.common.party.PartyManager;
import com.andrei1058.stevesus.common.party.request.PartyRequest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PartyListeners implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        final Player p = e.getPlayer();
        // offline tolerance
        if (PartyManager.getINSTANCE().getPartyAdapter() instanceof InternalPartyAdapter) {
            // remove from party immediately if no tolerance
            if (((InternalPartyAdapter)PartyManager.getINSTANCE().getPartyAdapter()).getOfflineTolerance() == 0) {
                PartyManager.getINSTANCE().getPartyAdapter().removeFromParty(p.getUniqueId());
            } else {
                // cache for offline tolerance
                Bukkit.getScheduler().runTaskAsynchronously(CommonManager.getINSTANCE().getPlugin(), () -> {
                    InternalPartyAdapter.OfflineToleranceTask.startTolerance(p.getUniqueId(), ((InternalPartyAdapter)PartyManager.getINSTANCE().getPartyAdapter()).getOfflineTolerance());
                });
            }
        }

        // clear party requests data
        PartyRequest.onPlayerQuit(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e){
        final Player p = e.getPlayer();

        // remove from offline tolerance
        if (PartyManager.getINSTANCE().getPartyAdapter() instanceof InternalPartyAdapter) {
            if (((InternalPartyAdapter)PartyManager.getINSTANCE().getPartyAdapter()).getOfflineTolerance() == 0) {
                Bukkit.getScheduler().runTaskAsynchronously(CommonManager.getINSTANCE().getPlugin(), () -> {
                    InternalPartyAdapter.OfflineToleranceTask.removeCachedTolerance(p.getUniqueId());
                });
            }
        }
    }
}
