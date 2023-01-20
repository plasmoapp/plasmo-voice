package su.plo.voice.api.proxy.event.command;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.proxy.command.MinecraftProxyCommand;
import su.plo.lib.api.server.command.MinecraftCommandManager;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.proxy.PlasmoVoiceProxy;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Called when the server is ready to register commands.
 */
public final class CommandsRegisterEvent implements Event {

    @Getter
    private final PlasmoVoiceProxy voiceProxy;
    @Getter
    private final MinecraftCommandManager<MinecraftProxyCommand> commandManager;

    public CommandsRegisterEvent(@NotNull PlasmoVoiceProxy voiceProxy,
                                 @NotNull MinecraftCommandManager<MinecraftProxyCommand> commandManager) {
        this.voiceProxy = checkNotNull(voiceProxy, "voiceProxy cannot be null");
        this.commandManager = checkNotNull(commandManager, "commandManager cannot be null");
    }
}
