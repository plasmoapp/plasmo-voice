package su.plo.voice.server.command;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.server.command.MinecraftCommand;
import su.plo.lib.api.server.command.MinecraftCommandSource;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.server.BaseVoiceServer;
import su.plo.voice.server.config.ServerLanguage;

@RequiredArgsConstructor
public final class VoiceReloadCommand implements MinecraftCommand {

    private final BaseVoiceServer voiceServer;

    @Override
    public void execute(@NotNull MinecraftCommandSource source, @NotNull String[] arguments) {
        ServerLanguage language = voiceServer.getLanguages().getLanguage(source);
        voiceServer.loadConfig();

        voiceServer.getPlayerManager().getPlayers()
                .stream()
                .filter(VoicePlayer::hasVoiceChat)
                .forEach((player) -> voiceServer.getTcpConnectionManager().sendConfigInfo(player));

        source.sendMessage(language.commands().reload().message());
    }

    @Override
    public boolean hasPermission(@NotNull MinecraftCommandSource source, @Nullable String[] arguments) {
        return source.hasPermission("voice.reload");
    }
}
