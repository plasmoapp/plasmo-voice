package su.plo.voice.server.command;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.profile.MinecraftGameProfile;
import su.plo.lib.api.server.MinecraftServerLib;
import su.plo.lib.api.server.command.MinecraftCommand;
import su.plo.lib.api.server.command.MinecraftCommandSource;
import su.plo.voice.api.server.mute.ServerMuteInfo;
import su.plo.voice.server.BaseVoiceServer;
import su.plo.voice.server.config.ServerLanguage;
import su.plo.voice.server.mute.VoiceMuteManager;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

@RequiredArgsConstructor
public final class VoiceMuteListCommand implements MinecraftCommand {

    private final BaseVoiceServer voiceServer;
    private final MinecraftServerLib minecraftServer;

    @Override
    public void execute(@NotNull MinecraftCommandSource source, @NotNull String[] arguments) {
        ServerLanguage language = voiceServer.getLanguages().getLanguage(source);

        VoiceMuteManager muteManager = (VoiceMuteManager) voiceServer.getMuteManager();

        Collection<ServerMuteInfo> mutedPlayers = muteManager.getMutedPlayers();

        source.sendMessage(language.commands().muteList().header());
        if (mutedPlayers.isEmpty()) {
            source.sendMessage(language.commands().muteList().empty());
            return;
        }

        mutedPlayers.forEach((muteInfo) -> {
            Optional<MinecraftGameProfile> player = minecraftServer.getGameProfile(muteInfo.getPlayerUUID());
            Optional<MinecraftGameProfile> mutedBy = Optional.empty();
            if (muteInfo.getMutedByPlayerUUID() != null) {
                mutedBy = minecraftServer.getGameProfile(muteInfo.getMutedByPlayerUUID());
            }
            if (!player.isPresent()) return;

            Date date = new Date(muteInfo.getMutedToTime());
            SimpleDateFormat expirationFormatDate = new SimpleDateFormat(
                    language.commands().muteList().expirationDate()
            );
            SimpleDateFormat expirationFormatTime = new SimpleDateFormat(
                    language.commands().muteList().expirationTime()
            );

            String expires = muteInfo.getMutedToTime() > 0
                    ? String.format(language.commands().muteList().expireAt(), expirationFormatDate.format(date), expirationFormatTime.format(date))
                    : language.commands().muteList().neverExpires();

            String reason = muteManager.formatMuteReason(language, muteInfo.getReason());

            if (mutedBy.isPresent()) {
                source.sendMessage(String.format(
                        language.commands().muteList().entryMutedBy(),
                        player.get().getName(),
                        mutedBy.get().getName(),
                        expires,
                        reason
                ));
            } else {
                source.sendMessage(String.format(
                        language.commands().muteList().entry(),
                        player.get().getName(),
                        expires,
                        reason
                ));
            }
        });
    }

    @Override
    public boolean hasPermission(@NotNull MinecraftCommandSource source, @Nullable String[] arguments) {
        return source.hasPermission("voice.mutelist");
    }

}
