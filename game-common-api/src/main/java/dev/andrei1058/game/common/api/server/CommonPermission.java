package dev.andrei1058.game.common.api.server;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public enum CommonPermission {

    ADMIN("admin", false),
    ALL("*", false),
    VIP_JOIN_FEATURE("vipjoin", false);

    private static final String PERMISSION_ROOT = "stevesus";
    private final String permission;
    private final String children;
    private final boolean playerDefault;

    CommonPermission(String perm, boolean playerDefault){
        this.permission = CommonPermission.PERMISSION_ROOT + "." + perm;
        this.children = perm;
        this.playerDefault = playerDefault;
    }

    @Override
    public String toString() {
        return permission;
    }

    public String get(){
        return permission;
    }

    public String getChildren() {
        return children;
    }

    public boolean isPlayerDefault() {
        return playerDefault;
    }

    public static void init() {
        Permission permission = Bukkit.getServer().getPluginManager().getPermission(PERMISSION_ROOT);
        if (permission == null) {
            permission = new Permission(PERMISSION_ROOT, "Plugin main permission.", PermissionDefault.TRUE);
            Bukkit.getPluginManager().addPermission(permission);
        }
        for (CommonPermission pluginPermission : values()) {
            if (pluginPermission == ALL) continue;
            if (!permission.getChildren().containsKey(pluginPermission.getChildren())) {
                permission.getChildren().put(pluginPermission.getChildren(), pluginPermission.isPlayerDefault());
            }
        }
        permission.recalculatePermissibles();
    }
}
