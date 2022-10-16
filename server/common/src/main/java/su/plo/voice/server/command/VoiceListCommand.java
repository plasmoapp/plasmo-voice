package su.plo.voice.server.command;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.server.command.MinecraftCommand;
import su.plo.lib.api.server.command.MinecraftCommandSource;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.server.BaseVoiceServer;
import su.plo.voice.server.config.ServerLanguage;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class VoiceListCommand implements MinecraftCommand {

    private final BaseVoiceServer voiceServer;

    @Override
    public void execute(@NotNull MinecraftCommandSource source, @NotNull String[] arguments) {
        List<String> players = voiceServer.getPlayerManager().getPlayers()
                .stream()
                .filter(VoicePlayer::hasVoiceChat)
                .map((player) -> player.getInstance().getName())
                .sorted()
                .collect(Collectors.toList());

        ServerLanguage language = voiceServer.getLanguages().getLanguage(source);
        source.sendMessage(String.format(
                language.commands().list().message(),
                players.size(),
                voiceServer.getPlayerManager().getPlayers().size(),
                players.size() > 0
                        ? String.join(", ", players)
                        : language.commands().list().empty()
        ));
    }

    @Override
    public boolean hasPermission(@NotNull MinecraftCommandSource source, @Nullable String[] arguments) {
        return source.hasPermission("voice.list");
    }
}
