package su.plo.voice.server.command;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.chat.TextComponent;
import su.plo.lib.server.command.MinecraftCommand;
import su.plo.lib.server.command.MinecraftCommandSource;
import su.plo.lib.server.entity.MinecraftServerPlayer;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.player.VoicePlayer;

@RequiredArgsConstructor
public final class VoiceReconnectCommand implements MinecraftCommand {

    private final PlasmoVoiceServer voiceServer;

    @Override
    public void execute(@NotNull MinecraftCommandSource source, @NotNull String[] arguments) {
        if (!(source instanceof MinecraftServerPlayer)) {
            source.sendMessage(TextComponent.literal("Only player can execute this command"));
            return;
        }

        MinecraftServerPlayer player = (MinecraftServerPlayer) source;
        VoicePlayer voicePlayer = voiceServer.getPlayerManager().getPlayerById(player.getUUID())
                .orElseThrow(() -> new IllegalStateException("how?"));

        source.sendMessage(TextComponent.translatable("commands.plasmovoice.reconnect"));
        voiceServer.getUdpConnectionManager().removeConnection(voicePlayer);
        voiceServer.getTcpConnectionManager().connect(voicePlayer);
    }

    @Override
    public boolean hasPermission(@NotNull MinecraftCommandSource source, @Nullable String[] arguments) {
        return source.hasPermission("voice.reconnect");
    }
}
