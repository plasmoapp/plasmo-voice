package su.plo.lib.server.permission;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class PermissionsManager {

    private static final PermissionDefault DEFAULT_PERMISSION = PermissionDefault.OP;

    private final Map<String, PermissionDefault> defaultPermissionByName = Maps.newHashMap();

    public void register(@NotNull String name, @NotNull PermissionDefault permissionDefault) {
        if (defaultPermissionByName.containsKey(name)) {
            throw new IllegalArgumentException("Permissions with name '" + name + "' already exist");
        }

        defaultPermissionByName.put(name, permissionDefault);
    }

    public boolean unregister(@NotNull String name) {
        return defaultPermissionByName.remove(name) != null;
    }

    public void clear() {
        defaultPermissionByName.clear();
    }

    public PermissionDefault getPermissionDefault(@NotNull String name) {
        return defaultPermissionByName.getOrDefault(name, DEFAULT_PERMISSION);
    }
}
