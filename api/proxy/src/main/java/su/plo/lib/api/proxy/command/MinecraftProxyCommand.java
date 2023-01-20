package su.plo.lib.api.proxy.command;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.command.MinecraftCommand;
import su.plo.lib.api.server.command.MinecraftCommandSource;

public interface MinecraftProxyCommand extends MinecraftCommand {

    /**
     * Pass command to backend server
     * <p>
     * Only works with Velocity
     */
    default boolean passToBackendServer(@NotNull MinecraftCommandSource source, @NotNull String[] args) {
        return false;
    }
}
