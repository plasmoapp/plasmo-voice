package su.plo.voice.server.command;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.server.MinecraftServerLib;
import su.plo.lib.api.server.command.MinecraftCommand;
import su.plo.lib.api.server.command.MinecraftCommandSource;
import su.plo.voice.api.server.mute.ServerMuteInfo;
import su.plo.voice.proto.data.player.MinecraftGameProfile;
import su.plo.voice.server.BaseVoiceServer;
import su.plo.voice.server.mute.VoiceMuteManager;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public final class VoiceMuteListCommand implements MinecraftCommand {

    private final BaseVoiceServer voiceServer;
    private final MinecraftServerLib minecraftServer;

    @Override
    public void execute(@NotNull MinecraftCommandSource source, @NotNull String[] arguments) {
        VoiceMuteManager muteManager = (VoiceMuteManager) voiceServer.getMuteManager();

        Collection<ServerMuteInfo> mutedPlayers = muteManager.getMuteStorage().getMutedPlayers();

        source.sendMessage(MinecraftTextComponent.translatable("pv.command.mute_list.header"));
        if (mutedPlayers.isEmpty()) {
            source.sendMessage(MinecraftTextComponent.translatable("pv.command.mute_list.empty"));
            return;
        }

        mutedPlayers.forEach((muteInfo) -> {
            Optional<MinecraftGameProfile> player = minecraftServer.getGameProfile(muteInfo.getPlayerUUID());
            Optional<MinecraftGameProfile> mutedBy = Optional.empty();
            if (muteInfo.getMutedByPlayerUUID() != null) {
                mutedBy = minecraftServer.getGameProfile(muteInfo.getMutedByPlayerUUID());
            }
            if (!player.isPresent()) return;

            Map<String, String> language = voiceServer.getLanguages().getServerLanguage(source);

            Date date = new Date(muteInfo.getMutedToTime());
            SimpleDateFormat expirationFormatDate = new SimpleDateFormat(
                    language.getOrDefault("pv.command.mute_list.expiration_date", "yyyy.MM.dd")
            );
            SimpleDateFormat expirationFormatTime = new SimpleDateFormat(
                    language.getOrDefault("pv.command.mute_list.expiration_time", "HH:mm:ss")
            );

            MinecraftTextComponent expires = muteInfo.getMutedToTime() > 0
                    ? MinecraftTextComponent.translatable("pv.command.mute_list.expire_at", expirationFormatDate.format(date), expirationFormatTime.format(date))
                    : MinecraftTextComponent.translatable("pv.command.mute_list.never_expires");

            MinecraftTextComponent reason = muteManager.formatMuteReason(muteInfo.getReason());

            if (mutedBy.isPresent()) {
                source.sendMessage(MinecraftTextComponent.translatable(
                        "pv.command.mute_list.entry_muted_by",
                        player.get().getName(),
                        mutedBy.get().getName(),
                        expires,
                        reason
                ));
            } else {
                source.sendMessage(MinecraftTextComponent.translatable(
                        "pv.command.mute_list.entry",
                        player.get().getName(),
                        expires,
                        reason
                ));
            }
        });
    }

    @Override
    public boolean hasPermission(@NotNull MinecraftCommandSource source, @Nullable String[] arguments) {
        return source.hasPermission("pv.mutelist");
    }

}
