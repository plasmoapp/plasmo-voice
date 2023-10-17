package su.plo.voice.server.command;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.slib.api.command.McCommand;
import su.plo.slib.api.command.McCommandSource;
import su.plo.slib.api.server.entity.player.McServerPlayer;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.server.BaseVoiceServer;

@RequiredArgsConstructor
public final class VoiceReconnectCommand implements McCommand {

    private final BaseVoiceServer voiceServer;

    @Override
    public void execute(@NotNull McCommandSource source, @NotNull String[] arguments) {
        if (!(source instanceof McServerPlayer)) {
            source.sendMessage(McTextComponent.translatable("pv.error.player_only_command"));
            return;
        }

        McServerPlayer player = (McServerPlayer) source;
        VoiceServerPlayer voicePlayer = voiceServer.getPlayerManager().getPlayerById(player.getUuid())
                .orElseThrow(() -> new IllegalStateException("how?"));

        source.sendMessage(McTextComponent.translatable("pv.command.reconnect.message"));
        voiceServer.getUdpConnectionManager().removeConnection(voicePlayer);
        voiceServer.getTcpPacketManager().requestPlayerInfo(voicePlayer);
    }

    @Override
    public boolean hasPermission(@NotNull McCommandSource source, @Nullable String[] arguments) {
        return source.hasPermission("pv.reconnect");
    }
}
