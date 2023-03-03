package su.plo.lib.api.server;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.chat.ServerTextConverter;
import su.plo.lib.api.server.command.MinecraftCommandManager;
import su.plo.lib.api.server.permission.PermissionsManager;

public interface MinecraftCommonServerLib {

    @NotNull ServerTextConverter<?> getTextConverter();

    @NotNull MinecraftCommandManager<?> getCommandManager();

    @NotNull PermissionsManager getPermissionsManager();
}
