package su.plo.voice.server.command;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.slib.api.command.McCommand;
import su.plo.slib.api.command.McCommandSource;
import su.plo.slib.api.server.entity.player.McServerPlayer;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.server.BaseVoiceServer;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class VoiceListCommand implements McCommand {

    private final BaseVoiceServer voiceServer;

    @Override
    public void execute(@NotNull McCommandSource source, @NotNull String[] arguments) {
        List<String> players;
        int totalPlayerCount;
        if (source instanceof McServerPlayer) {
            McServerPlayer sourcePlayer = (McServerPlayer) source;

            players = voiceServer.getPlayerManager().getPlayers()
                    .stream()
                    .filter(VoicePlayer::hasVoiceChat)
                    .filter(player -> sourcePlayer.canSee(player.getInstance()))
                    .map((player) -> player.getInstance().getName())
                    .sorted()
                    .collect(Collectors.toList());
            totalPlayerCount = (int) voiceServer.getPlayerManager()
                    .getPlayers()
                    .stream()
                    .filter(player -> sourcePlayer.canSee(player.getInstance()))
                    .count();
        } else {
            players = voiceServer.getPlayerManager().getPlayers()
                    .stream()
                    .filter(VoicePlayer::hasVoiceChat)
                    .map((player) -> player.getInstance().getName())
                    .sorted()
                    .collect(Collectors.toList());
            totalPlayerCount = voiceServer.getPlayerManager().getPlayers().size();
        }

        source.sendMessage(
                McTextComponent.translatable(
                        "pv.command.list.message",
                        players.size(),
                        totalPlayerCount,
                        players.size() > 0
                                ? String.join(", ", players)
                                : McTextComponent.translatable("pv.command.list.empty")
                )
        );
    }

    @Override
    public boolean hasPermission(@NotNull McCommandSource source, @Nullable String[] arguments) {
        return source.hasPermission("pv.list");
    }
}
