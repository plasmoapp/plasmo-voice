package su.plo.lib.api.proxy.event.command;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.command.MinecraftCommandSource;
import su.plo.voice.api.event.Event;

import static com.google.common.base.Preconditions.checkNotNull;

public class MinecraftCommandExecuteEvent implements Event {

    @Getter
    private final MinecraftCommandSource commandSource;
    @Getter
    private final String command;

    public MinecraftCommandExecuteEvent(@NotNull MinecraftCommandSource commandSource,
                                        @NotNull String command) {
        this.commandSource = checkNotNull(commandSource);
        this.command = checkNotNull(command);
    }

}
