package com.andrei1058.stevesus.common.party;

import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.party.adapter.DisabledPartyAdapter;
import com.andrei1058.stevesus.common.party.adapter.InternalPartyAdapter;
import com.andrei1058.stevesus.common.party.command.PartyCmd;
import com.andrei1058.stevesus.common.party.listener.PartyListeners;
import com.andrei1058.stevesus.common.api.party.PartyAdapter;
import com.andrei1058.stevesus.common.api.party.PartyHandler;
import org.bukkit.Bukkit;

public class PartyManager implements PartyHandler {

    private static PartyManager INSTANCE;

    private PartyAdapter partyAdapter = new DisabledPartyAdapter();
    private final boolean allowParties;
    private static int offlineTolerance;
    private static int defaultPartySizeLimit;
    private final boolean registerPartySubCmd;

    // -1 if not in use
    private int offlineToleranceTask = -1;

    private PartyManager(boolean enableParties, boolean registerPartySubCmd, int offlineTolerance, int defaultPartySizeLimit) {
        this.allowParties = enableParties;
        PartyManager.offlineTolerance = offlineTolerance;
        PartyManager.defaultPartySizeLimit = defaultPartySizeLimit;
        this.registerPartySubCmd = registerPartySubCmd;

        // register internal party related listeners
        // this should be registered even if the party adapter is not set to internal because
        // the adapter can be switched at any time and the internal adapter requires this
        Bukkit.getPluginManager().registerEvents(new PartyListeners(), CommonManager.getINSTANCE().getPlugin());

        // register party sub command if enabled
        if (registerPartySubCmd) {
            PartyCmd.register(CommonManager.getINSTANCE().getCommonProvider().getMainCommand());
        }
    }

    /**
     * Initialize party manager.
     *
     * @param enableParties       are parties allowed?
     * @param registerPartySubCmd register internal party sub command?
     * @param offlineTolerance    how many seconds before removing a player from a party if he disconnects?
     */
    public static void init(boolean enableParties, boolean registerPartySubCmd, int offlineTolerance, int defaultPartySizeLimit) {
        if (INSTANCE == null) {
            INSTANCE = new PartyManager(enableParties, registerPartySubCmd, offlineTolerance, defaultPartySizeLimit);

            // party adapter to internal
            // the method will check itself if parties are enabled
            INSTANCE.setPartyAdapter(new InternalPartyAdapter(offlineTolerance, defaultPartySizeLimit));
        }
    }

    /**
     * @return party manager.
     */
    public static PartyManager getINSTANCE() {
        return INSTANCE;
    }

    /**
     * @return current party adapter.
     */
    @Override
    public PartyAdapter getPartyAdapter() {
        return partyAdapter;
    }

    /**
     * Change party adapter.
     *
     * @return true if switched successfully.
     */
    @Override
    public boolean setPartyAdapter(PartyAdapter partyAdapter) {
        if (!isAllowParties()) return false;
        if (INSTANCE.partyAdapter.getLoadedParties() != 0) {
            return false;
        }
        if (partyAdapter == null) {
            INSTANCE.partyAdapter = new InternalPartyAdapter(offlineTolerance, defaultPartySizeLimit);
            return true;
        }
        if (partyAdapter.equals(INSTANCE.partyAdapter)) {
            return false;
        }
        // cancel offline tolerance task if is replacing internal adapter
        if (INSTANCE.partyAdapter instanceof InternalPartyAdapter && INSTANCE.offlineToleranceTask > -1) {
            Bukkit.getScheduler().cancelTask(INSTANCE.offlineToleranceTask);
            INSTANCE.offlineToleranceTask = -1;
        }
        INSTANCE.partyAdapter = partyAdapter;

        // if new adapter is internal adapter start offline tolerance task if needed
        if (INSTANCE.partyAdapter instanceof InternalPartyAdapter && offlineTolerance > 0) {
            INSTANCE.offlineToleranceTask = Bukkit.getScheduler().runTaskTimerAsynchronously(CommonManager.getINSTANCE().getPlugin(), new InternalPartyAdapter.OfflineToleranceTask(), 20 * 10, 20L).getTaskId();
        }
        CommonManager.getINSTANCE().getPlugin().getLogger().info("Set parties adapter to: " + partyAdapter.getName());
        return true;
    }

    /**
     * Check if parties are enabled.
     *
     * @return true if parties are allowed.
     */
    public boolean isAllowParties() {
        return allowParties;
    }

    /**
     * Check if party sub command is registered under the plugin's main command.
     *
     * @return if party sub command is registered.
     */
    @SuppressWarnings("unused")
    public boolean isRegisterPartySubCmd() {
        return registerPartySubCmd;
    }
}
