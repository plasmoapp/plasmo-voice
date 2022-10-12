package su.plo.lib.server.command;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.server.permission.PermissionTristate;

public interface MinecraftPermissionHolder {

    boolean hasPermission(@NotNull String permission);

    @NotNull PermissionTristate getPermission(@NotNull String permission);
}
