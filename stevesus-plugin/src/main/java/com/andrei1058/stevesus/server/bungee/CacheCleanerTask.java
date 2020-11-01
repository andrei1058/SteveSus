package com.andrei1058.stevesus.server.bungee;

import com.andrei1058.stevesus.server.bungee.party.PreLoadedParty;

import java.util.LinkedList;
import java.util.List;

public class CacheCleanerTask implements Runnable {

    private final List<ProxyUser> toRemove = new LinkedList<>();
    private final List<PreLoadedParty> toRemove2 = new LinkedList<>();

    @Override
    public void run() {
        // remove timed-out proxy-users
        ProxyUser.getLoaded().values().forEach(proxyUser -> {
            if (proxyUser.isTimedOut()) {
                // time out
                toRemove.add(proxyUser);
            }
        });
        toRemove.forEach(proxyUser -> proxyUser.destroy("Removed by cleaner task."));
        toRemove.clear();

        // remove timed-out pre-loaded-parties

        PreLoadedParty.getPreLoadedParties().values().forEach(preLoadedParty -> {
            if (preLoadedParty.isTimedOut()){
                toRemove2.add(preLoadedParty);
            }
        });
        toRemove2.forEach(PreLoadedParty::clean);
        toRemove2.clear();
    }
}
