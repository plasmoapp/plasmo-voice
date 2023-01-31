package su.plo.voice.server.command;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.server.command.MinecraftCommand;
import su.plo.lib.api.server.command.MinecraftCommandSource;
import su.plo.lib.api.server.entity.MinecraftServerPlayerEntity;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.server.BaseVoiceServer;

@RequiredArgsConstructor
public final class VoiceReconnectCommand implements MinecraftCommand {

    private final BaseVoiceServer voiceServer;

    @Override
    public void execute(@NotNull MinecraftCommandSource source, @NotNull String[] arguments) {
        if (!(source instanceof MinecraftServerPlayerEntity)) {
            source.sendMessage(MinecraftTextComponent.translatable("pv.error.player_only_command"));
            return;
        }

        MinecraftServerPlayerEntity player = (MinecraftServerPlayerEntity) source;
        VoiceServerPlayer voicePlayer = voiceServer.getPlayerManager().getPlayerById(player.getUUID())
                .orElseThrow(() -> new IllegalStateException("how?"));

        source.sendMessage(MinecraftTextComponent.translatable("pv.command.reconnect.message"));
        voiceServer.getUdpConnectionManager().removeConnection(voicePlayer);
        voiceServer.getTcpConnectionManager().requestPlayerInfo(voicePlayer);
    }

    @Override
    public boolean hasPermission(@NotNull MinecraftCommandSource source, @Nullable String[] arguments) {
        return source.hasPermission("pv.reconnect");
    }
}
