package su.plo.voice.api.server.event.command;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.command.MinecraftCommandManager;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.server.PlasmoVoiceServer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Called when the server is ready to register commands.
 */
public final class CommandsRegisterEvent implements Event {

    @Getter
    private final PlasmoVoiceServer voiceServer;
    @Getter
    private final MinecraftCommandManager commandManager;

    public CommandsRegisterEvent(@NotNull PlasmoVoiceServer voiceServer,
                                 @NotNull MinecraftCommandManager commandManager) {
        this.voiceServer = checkNotNull(voiceServer, "voiceServer cannot be null");
        this.commandManager = checkNotNull(commandManager, "commandManager cannot be null");
    }
}
