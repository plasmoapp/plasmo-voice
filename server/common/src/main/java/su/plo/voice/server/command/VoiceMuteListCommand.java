package su.plo.voice.server.command;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.slib.api.command.McCommand;
import su.plo.slib.api.command.McCommandSource;
import su.plo.slib.api.entity.player.McGameProfile;
import su.plo.slib.api.server.McServerLib;
import su.plo.voice.api.server.mute.ServerMuteInfo;
import su.plo.voice.server.BaseVoiceServer;
import su.plo.voice.server.mute.VoiceMuteManager;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

@RequiredArgsConstructor
public final class VoiceMuteListCommand implements McCommand {

    private final BaseVoiceServer voiceServer;
    private final McServerLib minecraftServer;

    @Override
    public void execute(@NotNull McCommandSource source, @NotNull String[] arguments) {
        VoiceMuteManager muteManager = (VoiceMuteManager) voiceServer.getMuteManager();

        Collection<ServerMuteInfo> mutedPlayers = muteManager.getMuteStorage().getMutedPlayers();

        source.sendMessage(McTextComponent.translatable("pv.command.mute_list.header"));
        if (mutedPlayers.isEmpty()) {
            source.sendMessage(McTextComponent.translatable("pv.command.mute_list.empty"));
            return;
        }

        mutedPlayers.forEach((muteInfo) -> {
            McGameProfile player = minecraftServer.getGameProfile(muteInfo.getPlayerUUID());
            McGameProfile mutedBy = null;
            if (muteInfo.getMutedByPlayerUUID() != null) {
                mutedBy = minecraftServer.getGameProfile(muteInfo.getMutedByPlayerUUID());
            }
            if (player == null) return;

            Map<String, String> language = voiceServer.getLanguages().getServerLanguage(source);

            Date date = new Date(muteInfo.getMutedToTime());
            SimpleDateFormat expirationFormatDate = new SimpleDateFormat(
                    language.getOrDefault("pv.command.mute_list.expiration_date", "yyyy.MM.dd")
            );
            SimpleDateFormat expirationFormatTime = new SimpleDateFormat(
                    language.getOrDefault("pv.command.mute_list.expiration_time", "HH:mm:ss")
            );

            McTextComponent expires = muteInfo.getMutedToTime() > 0
                    ? McTextComponent.translatable("pv.command.mute_list.expire_at", expirationFormatDate.format(date), expirationFormatTime.format(date))
                    : McTextComponent.translatable("pv.command.mute_list.never_expires");

            McTextComponent reason = muteManager.formatMuteReason(muteInfo.getReason());

            if (mutedBy != null) {
                source.sendMessage(
                        McTextComponent.translatable(
                                "pv.command.mute_list.entry_muted_by",
                                player.getName(),
                                mutedBy.getName(),
                                expires,
                                reason
                        )
                );
            } else {
                source.sendMessage(
                        McTextComponent.translatable(
                                "pv.command.mute_list.entry",
                                player.getName(),
                                expires,
                                reason
                        )
                );
            }
        });
    }

    @Override
    public boolean hasPermission(@NotNull McCommandSource source, @Nullable String[] arguments) {
        return source.hasPermission("pv.mutelist");
    }

}
