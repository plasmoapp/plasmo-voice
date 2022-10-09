package su.plo.voice.server.command;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.chat.TextComponent;
import su.plo.lib.profile.MinecraftGameProfile;
import su.plo.lib.server.MinecraftServerLib;
import su.plo.lib.server.command.MinecraftCommand;
import su.plo.lib.server.command.MinecraftCommandSource;
import su.plo.voice.api.server.mute.ServerMuteInfo;
import su.plo.voice.server.BaseVoiceServer;
import su.plo.voice.server.config.ServerConfig;
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
        VoiceMuteManager muteManager = (VoiceMuteManager) voiceServer.getMuteManager();
        ServerConfig config = voiceServer.getConfig();

        Collection<ServerMuteInfo> mutedPlayers = muteManager.getMutedPlayers();

        source.sendMessage(TextComponent.translatable("commands.plasmovoice.mute_list.header"));
        if (mutedPlayers.isEmpty()) {
            source.sendMessage(TextComponent.translatable("commands.plasmovoice.mute_list.empty"));
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
                    config.getCommands().getMuteList().getExpirationDate()
            );
            SimpleDateFormat expirationFormatTime = new SimpleDateFormat(
                    config.getCommands().getMuteList().getExpirationTime()
            );

            TextComponent expires = muteInfo.getMutedToTime() > 0
                    ? TextComponent.translatable("commands.plasmovoice.mute_list.expire_at", expirationFormatDate.format(date), expirationFormatTime.format(date))
                    : TextComponent.translatable("commands.plasmovoice.mute_list.never_expires");

            TextComponent reason = muteManager.formatMuteReason(muteInfo.getReason());

            if (mutedBy.isPresent()) {
                source.sendMessage(TextComponent.translatable(
                        "commands.plasmovoice.mute_list.entry_muted_by",
                        player.get().getName(),
                        mutedBy.get().getName(),
                        expires,
                        reason
                ));
            } else {
                source.sendMessage(TextComponent.translatable(
                        "commands.plasmovoice.mute_list.entry",
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
