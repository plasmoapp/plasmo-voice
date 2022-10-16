package su.plo.lib.api.server.command;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.permission.PermissionTristate;

public interface MinecraftPermissionHolder {

    boolean hasPermission(@NotNull String permission);

    @NotNull PermissionTristate getPermission(@NotNull String permission);
}
