package su.plo.voice.server.command;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.server.MinecraftServerLib;
import su.plo.lib.api.server.command.MinecraftCommand;
import su.plo.lib.api.server.command.MinecraftCommandSource;
import su.plo.voice.api.server.mute.MuteManager;
import su.plo.voice.proto.data.player.MinecraftGameProfile;
import su.plo.voice.server.BaseVoiceServer;
import su.plo.voice.server.config.ServerLanguage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class VoiceUnmuteCommand implements MinecraftCommand {

    private final BaseVoiceServer voiceServer;
    private final MinecraftServerLib minecraftServer;

    @Override
    public void execute(@NotNull MinecraftCommandSource source, @NotNull String[] arguments) {
        ServerLanguage language = voiceServer.getLanguages().getLanguage(source);

        if (arguments.length == 0) {
            source.sendMessage(language.commands().unmute().usage());
            return;
        }

        Optional<MinecraftGameProfile> player;
        try {
            player = minecraftServer.getGameProfile(UUID.fromString(arguments[0]));
        } catch (Exception e) {
            player = minecraftServer.getGameProfile(arguments[0]);
        }

        if (!player.isPresent()) {
            source.sendMessage(language.playerNotFound());
            return;
        }

        MuteManager muteManager = voiceServer.getMuteManager();

        if (!muteManager.unmute(player.get().getId(), false).isPresent()) {
            source.sendMessage(String.format(
                    language.commands().unmute().notMuted(),
                    player.get().getName()
            ));
            return;
        }

        source.sendMessage(String.format(
                language.commands().unmute().unmuted(),
                player.get().getName()
        ));
    }

    @Override
    public boolean hasPermission(@NotNull MinecraftCommandSource source, @Nullable String[] arguments) {
        return source.hasPermission("voice.unmute");
    }

    @Override
    public List<String> suggest(@NotNull MinecraftCommandSource source, @NotNull String[] arguments) {
        if (arguments.length <= 1) {
            String argument = arguments.length > 0 ? arguments[0] : "";

            return voiceServer.getMuteManager().getMutedPlayers()
                    .stream()
                    .map((muteInfo) -> {
                        Optional<MinecraftGameProfile> player = minecraftServer.getGameProfile(muteInfo.getPlayerUUID());
                        if (!player.isPresent()) return muteInfo.getPlayerUUID().toString();
                        return player.get().getName();
                    })
                    .filter((player) -> player.regionMatches(true, 0, argument, 0, argument.length()))
                    .collect(Collectors.toList());
        }

        return MinecraftCommand.super.suggest(source, arguments);
    }
}
