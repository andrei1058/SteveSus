package com.andrei1058.stevesus.common.party.adapter;

import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.api.party.PartyAdapter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InternalPartyAdapter implements PartyAdapter {

    private final int offlineTolerance;
    private final int defaultPartySizeLimit;

    public InternalPartyAdapter(int offlineTolerance, int defaultPartySizeLimit) {
        this.offlineTolerance = offlineTolerance;
        this.defaultPartySizeLimit = defaultPartySizeLimit;
    }

    public int getOfflineTolerance() {
        return offlineTolerance;
    }

    public int getDefaultPartySizeLimit() {
        return defaultPartySizeLimit;
    }

    /**
     * Internal Party.
     */
    private static class Party {

        static final List<InternalPartyAdapter.Party> parties = new LinkedList<>();

        public static int getPartiesSize() {
            return parties.size();
        }

        public static List<Party> getParties() {
            return parties;
        }

        // party members
        final List<UUID> members = new LinkedList<>();
        // party owner
        UUID owner;

        /**
         * Create a party by owner.
         *
         * @param partyOwner party owner.
         */
        private Party(UUID partyOwner, List<UUID> members) {
            owner = partyOwner;
            addMembers(members);
            addMember(owner);
            parties.add(this);
        }

        /**
         * @return party owner.
         */
        private UUID getOwner() {
            return owner;
        }

        /**
         * @return true if party does not have members.
         */
        private boolean isEmpty() {
            return members.isEmpty() || members.size() == 1 && members.contains(owner);
        }

        /**
         * Add a member to the party.
         *
         * @param playerUUID uuid to be added.
         */
        private void addMember(UUID playerUUID) {
            if (!isMember(playerUUID)) {
                members.add(playerUUID);
            }
        }

        /**
         * Transfer party ownership.
         */
        private void setOwner(UUID newOwner) {
            this.owner = newOwner;
        }

        /**
         * Remove a player from the party.
         *
         * @param playerUUID uuid to be removed.
         * @return true if party got disbanded.
         */
        private boolean removeMember(UUID playerUUID) {
            members.remove(playerUUID);
            if (isEmpty()) {
                disband();
                return true;
            }
            return false;
        }

        /**
         * Disband this party.
         */
        private void disband() {
            parties.remove(this);
            members.clear();
        }

        /**
         * Add a list of members to the party.
         *
         * @param players UUIDs to be added.
         */
        private void addMembers(List<UUID> players) {
            players.forEach(this::addMember);
        }

        /**
         * Check if the given UUID is a member.
         * This will not include the party owner.
         *
         * @param playerUUID player to be checked.
         * @return true if the given param is a member. Will return false if is the party Owner.
         */
        private boolean isMember(UUID playerUUID) {
            return members.contains(playerUUID);
        }

        /**
         * Check if the given UUID is the party owner.
         *
         * @param playerUUID player to be checked.
         * @return true if the given UUID is the party owner..
         */
        private boolean isOwner(UUID playerUUID) {
            return owner.equals(playerUUID);
        }

        /**
         * Get party by owner.
         *
         * @param owner party owner.
         * @return null if party not found.
         */
        @Nullable
        private static Party getPartyByOwner(UUID owner) {
            return parties.stream().filter(party -> party.isOwner(owner)).findFirst().orElse(null);
        }

        /**
         * Get party by player (both member or owner).
         *
         * @param player player.
         * @return null if party not found.
         */
        @Nullable
        private static Party getPartyByPlayer(UUID player) {
            return parties.stream().filter(party -> party.isMember(player)).findFirst().orElse(null);
        }

        /**
         * Get party members list.
         */
        public List<UUID> getMembers() {
            return members;
        }
    }


    @Override
    public String getName() {
        return CommonManager.getINSTANCE().getPlugin().getName();
    }

    @Override
    public boolean hasParty(@Nullable UUID player) {
        if (player == null) return false;
        return Party.getPartyByPlayer(player) != null;
    }

    @Override
    public boolean addMember(UUID partyOwner, UUID toBeAdded) {
        if (partyOwner == null) return false;
        if (toBeAdded == null) return false;
        if (toBeAdded.equals(partyOwner)) return false;
        Party party = Party.getPartyByOwner(partyOwner);
        if (party == null) {
            new Party(partyOwner, Collections.singletonList(toBeAdded));
            return true;
        }
        party.addMember(toBeAdded);
        return true;
    }

    @Override
    public boolean createParty(UUID partyOwner, List<UUID> members) {
        if (hasParty(partyOwner)) return false;
        new Party(partyOwner, members);
        return false;
    }

    @Override
    public boolean removeFromParty(UUID player) {
        Party party = Party.getPartyByPlayer(player);
        if (party == null) return false;
        return party.removeMember(player);
    }

    @Override
    public int getLoadedParties() {
        return Party.getPartiesSize();
    }

    @Override
    public boolean isOwner(UUID player) {
        return Party.getPartyByOwner(player) != null;
    }

    @Override
    public @NotNull List<UUID> getMembers(UUID partyMemberOrOwner) {
        Party foundParty = Party.getPartyByPlayer(partyMemberOrOwner);
        if (foundParty != null) {
            return foundParty.getMembers();
        }
        return Collections.emptyList();
    }

    @Override
    public UUID getOwner(UUID member) {
        Party foundParty = Party.getPartyByPlayer(member);
        return foundParty == null ? null : foundParty.getOwner();
    }

    @Override
    public void disband(UUID player) {
        Party foundParty = Party.getPartyByPlayer(player);
        if (foundParty != null) {
            foundParty.disband();
        }
    }

    @Override
    public boolean isSelfTeamUpAtRemoteJoin() {
        return false;
    }

    @Override
    public boolean transferOwnership(UUID partyOwner, UUID partyMember) {
        Party party = Party.getPartyByOwner(partyOwner);
        if (party == null) return false;
        if (!party.isMember(partyMember)) return false;
        party.addMember(partyOwner);
        party.setOwner(partyMember);
        return true;
    }

    @Override
    public boolean isPartySizeLimitReached(UUID partyOwnerOrMember) {
        Party party = Party.getPartyByPlayer(partyOwnerOrMember);
        if (party == null) return false;
        return party.getMembers().size() == getDefaultPartySizeLimit();
    }

    public static class OfflineToleranceTask implements Runnable {

        private static final ConcurrentHashMap<UUID, Long> playerAndRemoveTime = new ConcurrentHashMap<>();
        private static final LinkedList<UUID> usersToRemove = new LinkedList<>();

        /**
         * Remove a player data.
         * If he joins back, if the party gets disbanded etc.
         * This should be called async.
         */
        public static void removeCachedTolerance(@NotNull UUID player) {
            OfflineToleranceTask.playerAndRemoveTime.remove(player);
        }

        /**
         * Start counting before player party removal.
         * This should be called async.
         */
        public static void startTolerance(@NotNull UUID player, int toleranceSeconds) {
            OfflineToleranceTask.playerAndRemoveTime.put(player, System.currentTimeMillis() + (toleranceSeconds * 1000));
        }

        public OfflineToleranceTask() {
            playerAndRemoveTime.clear();
            usersToRemove.clear();
        }

        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();
            playerAndRemoveTime.forEach((uuid, time) -> {
                if (currentTime > time) {
                    usersToRemove.add(uuid);
                }
            });
            if (!usersToRemove.isEmpty()) {
                Bukkit.getScheduler().runTask(CommonManager.getINSTANCE().getPlugin(), () -> {
                    usersToRemove.forEach(player -> {
                        Party party = Party.getPartyByPlayer(player);
                        if (party != null) {
                            party.removeMember(player);
                        }
                        playerAndRemoveTime.remove(player);
                    });
                    usersToRemove.clear();
                });

                // this should prevent concurrent modification on usersToRemove since this is an ASYNC task
                // actually we don't even need that. the task runs every 20 ticks so this should not occur.
                /*try {
                    Thread.sleep(200L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            }
        }
    }
}
