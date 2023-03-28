package su.plo.lib.api.server.command;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MinecraftCommand {

    void execute(@NotNull MinecraftCommandSource source, @NotNull String[] arguments);

    default List<String> suggest(@NotNull MinecraftCommandSource source, @NotNull String[] arguments) {
        return ImmutableList.of();
    }

    /**
     * Note: Arguments are not null only on execution
     *
     * @return true if command source has permission to execute this command
     */
    default boolean hasPermission(@NotNull MinecraftCommandSource source, @Nullable String[] arguments) {
        return true;
    }
}
