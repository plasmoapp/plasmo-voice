package su.plo.voice.api.server.event.command;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import su.plo.lib.api.server.command.MinecraftCommand;
import su.plo.lib.api.server.command.MinecraftCommandManager;
import su.plo.voice.api.addon.AddonManager;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.server.PlasmoVoiceServer;

/**
 * Called when the server is ready to register commands.
 * <br/>
 * Will not be called if addon was registered by {@link AddonManager#load(Object)}
 */
@RequiredArgsConstructor
public final class CommandsRegisterEvent implements Event {

    @Getter
    private final @NonNull PlasmoVoiceServer voiceServer;
    @Getter
    private final @NonNull MinecraftCommandManager<MinecraftCommand> commandManager;
}
