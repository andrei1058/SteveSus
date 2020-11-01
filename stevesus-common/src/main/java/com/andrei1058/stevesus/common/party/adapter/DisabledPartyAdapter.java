package com.andrei1058.stevesus.common.party.adapter;

import com.andrei1058.stevesus.common.api.party.PartyAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DisabledPartyAdapter implements PartyAdapter {
    @Override
    public String getName() {
        return "PartiesDisabled";
    }

    @Override
    public boolean hasParty(@Nullable UUID player) {
        return false;
    }

    @Override
    public boolean addMember(UUID partyOwner, UUID toBeAdded) {
        return false;
    }

    @Override
    public boolean createParty(UUID partyOwner, List<UUID> members) {
        return false;
    }

    @Override
    public boolean removeFromParty(UUID player) {
        return false;
    }

    @Override
    public int getLoadedParties() {
        return 0;
    }

    @Override
    public boolean isOwner(UUID player) {
        return false;
    }

    @Override
    public @NotNull List<UUID> getMembers(UUID partyOwner) {
        return new ArrayList<>();
    }

    @Override
    public UUID getOwner(UUID member) {
        return null;
    }

    @Override
    public void disband(UUID owner) {

    }

    @Override
    public boolean isSelfTeamUpAtRemoteJoin() {
        return true;
    }

    @Override
    public boolean transferOwnership(UUID partyOwner, UUID partyMember) {
        return false;
    }

    @Override
    public boolean isPartySizeLimitReached(UUID partyOwnerOrMember) {
        return false;
    }
}
