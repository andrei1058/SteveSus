package com.andrei1058.stevesus.api.server;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public enum PluginPermission {

    /**
     * Permission for using setup commands.
     */
    CMD_ADMIN("admin", false),
    CMD_FORCE_START("forcestart", false),
    CHAT_FILTER_BYPASS("chatfilterbypass", false),
    CMD_TELEPORTER("teleporter", true);

    private static final String PERMISSION_ROOT = "stevesus";
    private final String permission;
    private final String children;
    private final boolean playerDefault;

    PluginPermission(String perm, boolean playerDefault) {
        this.permission = PluginPermission.PERMISSION_ROOT + "." + perm;
        this.children = perm;
        this.playerDefault = playerDefault;
    }

    @Override
    public String toString() {
        return permission;
    }

    public String get() {
        return permission;
    }

    public boolean isPlayerDefault() {
        return playerDefault;
    }

    public String getChildren() {
        return children;
    }

    public static void init() {
        Permission permission = Bukkit.getServer().getPluginManager().getPermission(PERMISSION_ROOT);
        if (permission == null) {
            permission = new Permission(PERMISSION_ROOT, "Plugin main permission.", PermissionDefault.TRUE);
            Bukkit.getPluginManager().addPermission(permission);
        }
        for (PluginPermission pluginPermission : values()) {
            if (!permission.getChildren().containsKey(pluginPermission.getChildren())) {
                permission.getChildren().put(pluginPermission.getChildren(), pluginPermission.isPlayerDefault());
            }
        }
        permission.recalculatePermissibles();
    }
}
