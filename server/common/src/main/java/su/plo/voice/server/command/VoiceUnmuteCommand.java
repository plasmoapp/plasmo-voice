package su.plo.voice.server.command;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.chat.TextComponent;
import su.plo.lib.profile.MinecraftGameProfile;
import su.plo.lib.server.MinecraftServerLib;
import su.plo.lib.server.command.MinecraftCommand;
import su.plo.lib.server.command.MinecraftCommandSource;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.mute.MuteManager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class VoiceUnmuteCommand implements MinecraftCommand {

    private final PlasmoVoiceServer voiceServer;
    private final MinecraftServerLib minecraftServer;

    @Override
    public void execute(@NotNull MinecraftCommandSource source, @NotNull String[] arguments) {
        if (arguments.length == 0) {
            source.sendMessage(TextComponent.translatable("commands.plasmovoice.unmute.usage"));
            return;
        }

        Optional<MinecraftGameProfile> player;
        try {
            player = minecraftServer.getGameProfile(UUID.fromString(arguments[0]));
        } catch (Exception e) {
            player = minecraftServer.getGameProfile(arguments[0]);
        }

        if (!player.isPresent()) {
            source.sendMessage(TextComponent.translatable("commands.plasmovoice.player_not_found"));
            return;
        }

        MuteManager muteManager = voiceServer.getMuteManager();

        if (!muteManager.unmute(player.get().getId(), false).isPresent()) {
            source.sendMessage(TextComponent.translatable(
                    "commands.plasmovoice.unmute.not_muted",
                    player.get().getName()
            ));
            return;
        }

        source.sendMessage(TextComponent.translatable(
                "commands.plasmovoice.unmute.unmuted",
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
