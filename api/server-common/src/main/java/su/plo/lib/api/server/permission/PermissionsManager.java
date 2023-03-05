package su.plo.lib.api.server.permission;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Manages universal permissions
 *
 * <p>Universal permissions are server implementation independent, so they will work on Paper/Forge/Fabric/etc.</p>
 */
public final class PermissionsManager {

    private static final PermissionDefault DEFAULT_PERMISSION = PermissionDefault.OP;

    private final Map<String, PermissionDefault> defaultPermissionByName = Maps.newHashMap();

    /**
     * Registers the universal permission
     *
     * @param name permission name
     * @param permissionDefault permission default value
     *
     * @throws IllegalArgumentException if permission with specified name is already exists
     */
    public void register(@NotNull String name, @NotNull PermissionDefault permissionDefault) {
        if (defaultPermissionByName.containsKey(name)) {
            throw new IllegalArgumentException("Permissions with name '" + name + "' already exist");
        }

        defaultPermissionByName.put(name, permissionDefault);
    }

    /**
     * @return true if the map contained the specified permission name
     */
    public boolean unregister(@NotNull String name) {
        return defaultPermissionByName.remove(name) != null;
    }

    /**
     * Clears all universal permissions
     */
    public void clear() {
        defaultPermissionByName.clear();
    }

    /**
     * Gets the default permission value by permission name
     *
     * @return value if exists or {@link PermissionDefault#OP}
     */
    public PermissionDefault getPermissionDefault(@NotNull String name) {
        return defaultPermissionByName.getOrDefault(name, DEFAULT_PERMISSION);
    }
}
