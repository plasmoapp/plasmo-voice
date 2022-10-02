package su.plo.lib.server.command;

import org.jetbrains.annotations.NotNull;

public interface MinecraftPermissionHolder {

    boolean hasPermission(@NotNull String permission);
}
