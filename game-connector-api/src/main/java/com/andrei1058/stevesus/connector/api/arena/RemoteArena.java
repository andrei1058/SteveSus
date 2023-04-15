package com.andrei1058.stevesus.connector.api.arena;

import com.andrei1058.stevesus.common.api.arena.DisplayableArena;
import com.andrei1058.stevesus.common.api.arena.GameState;
import com.andrei1058.stevesus.common.api.packet.RawSocket;
import org.bukkit.inventory.ItemStack;

public interface RemoteArena extends DisplayableArena {

    /**
     * Get bungee server of this arena.
     */
    RawSocket getServer();

    /**
     * Get game id of this arena on the remote server.
     */
    int getGameId();

    /**
     * Change display name.
     */
    void setDisplayName(String name);

    /**
     * Change arena game state.
     */
    void setGameState(GameState gameState);

    /**
     * Change full-join used slots.
     */
    void setVips(int vips);

    /**
     * Toggle spectate rule.
     * This will not update the spectate rule on the remote arena.
     * @param perm
     */
    void setSpectatePermission(String perm);

    void setMinPlayers(int minPlayers);

    void setMaxPlayers(int maxPlayers);

    void setCurrentPlayers(int players);

    void setCurrentSpectators(int spectators);

    void setDisplayItem(ItemStack displayItem);
}
