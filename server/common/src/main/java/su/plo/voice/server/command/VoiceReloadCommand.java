package su.plo.voice.server.command;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.server.command.MinecraftCommand;
import su.plo.lib.api.server.command.MinecraftCommandSource;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.server.BaseVoiceServer;

@RequiredArgsConstructor
public final class VoiceReloadCommand implements MinecraftCommand {

    private final BaseVoiceServer voiceServer;

    @Override
    public void execute(@NotNull MinecraftCommandSource source, @NotNull String[] arguments) {
        voiceServer.loadConfig(true);

        voiceServer.getPlayerManager().getPlayers()
                .stream()
                .filter(VoicePlayer::hasVoiceChat)
                .forEach((player) -> voiceServer.getTcpConnectionManager().sendConfigInfo(player));

        source.sendMessage(MinecraftTextComponent.translatable("pv.command.reload.message"));
    }

    @Override
    public boolean hasPermission(@NotNull MinecraftCommandSource source, @Nullable String[] arguments) {
        return source.hasPermission("pv.reload");
    }
}
