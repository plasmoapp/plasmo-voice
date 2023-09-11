package su.plo.voice.server.command;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.server.command.MinecraftCommand;
import su.plo.lib.api.server.command.MinecraftCommandSource;
import su.plo.lib.api.server.entity.MinecraftServerPlayerEntity;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.server.BaseVoiceServer;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class VoiceListCommand implements MinecraftCommand {

    private final BaseVoiceServer voiceServer;

    @Override
    public void execute(@NotNull MinecraftCommandSource source, @NotNull String[] arguments) {
        List<String> players;
        int totalPlayerCount = 0;

        if (source instanceof MinecraftServerPlayerEntity) {
            MinecraftServerPlayerEntity sourcePlayer = (MinecraftServerPlayerEntity) source;

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

        source.sendMessage(MinecraftTextComponent.translatable(
                "pv.command.list.message",
                players.size(),
                totalPlayerCount,
                players.size() > 0
                        ? String.join(", ", players)
                        : MinecraftTextComponent.translatable("pv.command.list.empty")
        ));
    }

    @Override
    public boolean hasPermission(@NotNull MinecraftCommandSource source, @Nullable String[] arguments) {
        return source.hasPermission("pv.list");
    }
}
