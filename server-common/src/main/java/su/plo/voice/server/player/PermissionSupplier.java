package su.plo.voice.server.player;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.permission.PermissionTristate;

public interface PermissionSupplier {

    boolean hasPermission(@NotNull Object player, @NotNull String permission);

    @NotNull PermissionTristate getPermission(@NotNull Object player, @NotNull String permission);
}
