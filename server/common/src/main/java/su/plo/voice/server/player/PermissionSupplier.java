package su.plo.voice.server.player;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface PermissionSupplier {

    boolean hasPermission(@NotNull Object player, @NotNull String permission);
}
