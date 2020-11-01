package com.andrei1058.amongusmc.common.hook.vault.chat;

import net.milkbowl.vault.chat.Chat;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VaultChatSupport implements VaultChatHook {

    private final Chat chat;

    public VaultChatSupport(Chat provider) {
        this.chat = provider;
    }

    @Override
    public String getGroupPrefix(String world, String group) {
        return chat.getGroupPrefix(world, group);
    }

    @Override
    public String getGroupPrefix(World world, String group) {
        return chat.getGroupPrefix(world, group);
    }

    @Override
    public String getGroupSuffix(String world, String group) {
        return chat.getGroupSuffix(world, group);
    }

    @Override
    public String getGroupSuffix(World world, String group) {
        return chat.getGroupSuffix(world, group);
    }

    @Override
    public String[] getGroups() {
        return chat.getGroups();
    }

    @Override
    public String[] getPlayerGroups(Player player) {
        return chat.getPlayerGroups(player);
    }

    @Override
    public String[] getPlayerGroups(String world, OfflinePlayer player) {
        return chat.getPlayerGroups(world, player);
    }

    @Override
    public @NotNull String getPlayerPrefix(Player player) {
        if (player == null) return "";
        return chat.getPlayerPrefix(player);
    }

    @Override
    public String getPlayerPrefix(String world, OfflinePlayer player) {
        return chat.getPlayerPrefix(world, player);
    }

    @Override
    public @NotNull String getPlayerSuffix(Player player) {
        if (player == null) return "";
        return chat.getPlayerSuffix(player);
    }

    @Override
    public String getPlayerSuffix(String world, OfflinePlayer player) {
        return chat.getPlayerSuffix(world, player);
    }

    @Override
    public String getPrimaryGroup(Player player) {
        return chat.getPrimaryGroup(player);
    }

    @Override
    public String getPrimaryGroup(String world, OfflinePlayer player) {
        return chat.getPrimaryGroup(world, player);
    }

    @Override
    public boolean playerInGroup(Player player, String group) {
        return chat.playerInGroup(player, group);
    }

    @Override
    public boolean playerInGroup(String world, OfflinePlayer player, String group) {
        return chat.playerInGroup(world, player, group);
    }

    @Override
    public void setGroupPrefix(String world, String group, String prefix) {
        chat.setGroupPrefix(world, group, prefix);
    }

    @Override
    public void setGroupPrefix(World world, String group, String prefix) {
        chat.setGroupPrefix(world, group, prefix);
    }

    @Override
    public void setGroupSuffix(String world, String group, String suffix) {
        chat.setGroupSuffix(world, group, suffix);
    }

    @Override
    public void setGroupSuffix(World world, String group, String suffix) {
        chat.setGroupSuffix(world, group, suffix);
    }

    @Override
    public void setPlayerPrefix(Player player, String prefix) {
        chat.setPlayerPrefix(player, prefix);
    }

    @Override
    public void setPlayerPrefix(String world, OfflinePlayer player, String prefix) {
        chat.setPlayerPrefix(world, player, prefix);
    }

    @Override
    public void setPlayerSuffix(Player player, String suffix) {
        chat.setPlayerSuffix(player, suffix);
    }

    @Override
    public void setPlayerSuffix(String world, OfflinePlayer player, String suffix) {
        chat.setPlayerSuffix(world, player, suffix);
    }
}
