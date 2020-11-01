package com.andrei1058.amoungusmc.common.api.party;

public interface PartyHandler {

    /**
     * Get party support interface.
     *
     * @return current party adapter.
     */
    PartyAdapter getPartyAdapter();

    /**
     * Change server party adapter.
     * This is only possible if there aren't any created parties on the existing adapter.
     *
     * @param partyAdapter new adapter. Null to revert to default.
     * @return true if changed successfully.
     */
    boolean setPartyAdapter(PartyAdapter partyAdapter);
}
