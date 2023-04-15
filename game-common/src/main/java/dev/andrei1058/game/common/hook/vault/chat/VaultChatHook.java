package dev.andrei1058.game.common.hook.vault.chat;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface VaultChatHook {

    /**
     * Get a group prefix in the given world.
     *
     * @param world world.
     * @param group group name.
     * @return empty string if not found etc.
     */
    String getGroupPrefix(String world, String group);

    /**
     * Get a group prefix in the given world.
     *
     * @param world world.
     * @param group group name.
     * @return empty string if not found etc.
     */
    String getGroupPrefix(World world, String group);

    /**
     * Get a group suffix in the given world.
     *
     * @param world world.
     * @param group group name.
     * @return empty string if not found etc.
     */
    String getGroupSuffix(String world, String group);

    /**
     * Get a group suffix in the given world.
     *
     * @param world world.
     * @param group group name.
     * @return empty string if not found etc.
     */
    String getGroupSuffix(World world, String group);

    /**
     * Returns a list of all known groups.
     *
     * @return empty array if not found etc.
     */
    String[] getGroups();

    /**
     * Get the list of groups that this player has.
     *
     * @return empty array if not found etc.
     */
    String[] getPlayerGroups(Player player);

    /**
     * Get the list of groups that this player has.
     *
     * @return empty array if not found etc.
     */
    String[] getPlayerGroups(String world, OfflinePlayer player);

    /**
     * Get players prefix from the world they are currently in.
     *
     * @return empty string if not found etc.
     */
    @NotNull
    String getPlayerPrefix(@Nullable Player player);

    /**
     * Get a players prefix in the given world Use NULL for world if requesting a global prefix.
     *
     * @return empty string if not found etc.
     */
    String getPlayerPrefix(String world, OfflinePlayer player);

    /**
     * Get players suffix in the world they are currently in.
     *
     * @return empty string if not found etc.
     */
    @NotNull
    String getPlayerSuffix(@Nullable Player player);

    /**
     * Get players suffix in the specified world.
     *
     * @return empty string if not found etc.
     */
    String getPlayerSuffix(String world, OfflinePlayer player);

    /**
     * Get players primary group.
     *
     * @return empty string if not found etc.
     */
    String getPrimaryGroup(Player player);

    /**
     * Get players primary group.
     *
     * @return empty string if not found etc.
     */
    String getPrimaryGroup(String world, OfflinePlayer player);

    /**
     * Check if player is member of a group.
     */
    boolean playerInGroup(Player player, String group);

    /**
     * Check if player is member of a group.
     */
    boolean playerInGroup(String world, OfflinePlayer player, String group);

    /**
     * Set group prefix.
     */
    void setGroupPrefix(String world, String group, String prefix);

    /**
     * Set group prefix.
     */
    void setGroupPrefix(World world, String group, String prefix);

    /**
     * Set group suffix.
     */
    void setGroupSuffix(String world, String group, String suffix);

    /**
     * Set group suffix.
     */
    void setGroupSuffix(World world, String group, String suffix);

    /**
     * Set players prefix in the world they are currently in.
     */
    void setPlayerPrefix(Player player, String prefix);

    /**
     * Sets players prefix in the given world.
     */
    void setPlayerPrefix(String world, OfflinePlayer player, String prefix);

    /**
     * Set players suffix in the world they currently occupy.
     */
    void setPlayerSuffix(Player player, String suffix);

    /**
     * Set players suffix for the world specified.
     */
    void setPlayerSuffix(String world, OfflinePlayer player, String suffix);
}
