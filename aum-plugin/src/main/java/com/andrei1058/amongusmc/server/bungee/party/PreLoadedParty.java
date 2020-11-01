package com.andrei1058.amongusmc.server.bungee.party;

import com.andrei1058.amongusmc.common.party.PartyManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PreLoadedParty {

    private final String owner;
    private final List<UUID> members = new ArrayList<>();
    private final long timeOut;

    private static final ConcurrentHashMap<String, PreLoadedParty> preLoadedParties = new ConcurrentHashMap<>();

    public PreLoadedParty(String owner) {
        PreLoadedParty plp = getPartyByOwner(owner);
        if (plp != null) {
            plp.clean();
        }
        this.owner = owner;
        timeOut = System.currentTimeMillis() + 7000L;
        preLoadedParties.put(owner, this);
    }

    public static PreLoadedParty getPartyByOwner(String owner) {
        return preLoadedParties.getOrDefault(owner, null);
    }

    public void addMember(Player player) {
        if (!members.contains(player.getUniqueId())) {
            members.add(player.getUniqueId());
        }
    }

    public void teamUp() {
        if (this.owner == null) return;
        Player owner = Bukkit.getPlayer(this.owner);
        if (owner == null) return;
        if (!owner.isOnline()) return;
        members.removeIf(member -> !Bukkit.getPlayer(member).isOnline());
        PartyManager.getINSTANCE().getPartyAdapter().createParty(owner.getUniqueId(), members);
        preLoadedParties.remove(this.owner);
    }

    public boolean isTimedOut() {
        return timeOut <= System.currentTimeMillis();
    }

    public static ConcurrentHashMap<String, PreLoadedParty> getPreLoadedParties() {
        return preLoadedParties;
    }

    public void clean() {
        preLoadedParties.remove(this.owner);
    }
}
