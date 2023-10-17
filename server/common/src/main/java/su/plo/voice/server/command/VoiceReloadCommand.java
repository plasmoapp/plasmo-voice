package su.plo.voice.server.command;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.slib.api.command.McCommand;
import su.plo.slib.api.command.McCommandSource;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.server.BaseVoiceServer;

@RequiredArgsConstructor
public final class VoiceReloadCommand implements McCommand {

    private final BaseVoiceServer voiceServer;

    @Override
    public void execute(@NotNull McCommandSource source, @NotNull String[] arguments) {
        voiceServer.loadConfig(true);

        voiceServer.getPlayerManager().getPlayers()
                .stream()
                .filter(VoicePlayer::hasVoiceChat)
                .forEach((player) -> voiceServer.getTcpPacketManager().sendConfigInfo(player));

        source.sendMessage(McTextComponent.translatable("pv.command.reload.message"));
    }

    @Override
    public boolean hasPermission(@NotNull McCommandSource source, @Nullable String[] arguments) {
        return source.hasPermission("pv.reload");
    }
}
