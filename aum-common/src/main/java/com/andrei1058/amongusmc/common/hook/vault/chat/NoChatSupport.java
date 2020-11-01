package com.andrei1058.amongusmc.common.hook.vault.chat;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NoChatSupport implements VaultChatHook {

    private static final String[] EMPTY_ARRAY = new String[0];
    private static final String EMPTY_STRING = "";
    @Override
    public String getGroupPrefix(String world, String group) {
        return EMPTY_STRING;
    }

    @Override
    public String getGroupPrefix(World world, String group) {
        return EMPTY_STRING;
    }

    @Override
    public String getGroupSuffix(String world, String group) {
        return EMPTY_STRING;
    }

    @Override
    public String getGroupSuffix(World world, String group) {
        return EMPTY_STRING;
    }

    @Override
    public String[] getGroups() {
        return EMPTY_ARRAY;
    }

    @Override
    public String[] getPlayerGroups(Player player) {
        return EMPTY_ARRAY;
    }

    @Override
    public String[] getPlayerGroups(String world, OfflinePlayer player) {
        return EMPTY_ARRAY;
    }

    @Override
    public @NotNull String getPlayerPrefix(Player player) {
        return EMPTY_STRING;
    }

    @Override
    public String getPlayerPrefix(String world, OfflinePlayer player) {
        return EMPTY_STRING;
    }

    @Override
    public @NotNull String getPlayerSuffix(Player player) {
        return EMPTY_STRING;
    }

    @Override
    public String getPlayerSuffix(String world, OfflinePlayer player) {
        return EMPTY_STRING;
    }

    @Override
    public String getPrimaryGroup(Player player) {
        return EMPTY_STRING;
    }

    @Override
    public String getPrimaryGroup(String world, OfflinePlayer player) {
        return EMPTY_STRING;
    }

    @Override
    public boolean playerInGroup(Player player, String group) {
        return false;
    }

    @Override
    public boolean playerInGroup(String world, OfflinePlayer player, String group) {
        return false;
    }

    @Override
    public void setGroupPrefix(String world, String group, String prefix) {

    }

    @Override
    public void setGroupPrefix(World world, String group, String prefix) {

    }

    @Override
    public void setGroupSuffix(String world, String group, String suffix) {

    }

    @Override
    public void setGroupSuffix(World world, String group, String suffix) {

    }

    @Override
    public void setPlayerPrefix(Player player, String prefix) {

    }

    @Override
    public void setPlayerPrefix(String world, OfflinePlayer player, String prefix) {

    }

    @Override
    public void setPlayerSuffix(Player player, String suffix) {

    }

    @Override
    public void setPlayerSuffix(String world, OfflinePlayer player, String suffix) {

    }
}
