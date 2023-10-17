package su.plo.voice.server.mute;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.voice.api.server.event.mute.PlayerVoiceMutedEvent;
import su.plo.voice.api.server.event.mute.PlayerVoiceUnmutedEvent;
import su.plo.voice.api.server.mute.MuteDurationUnit;
import su.plo.voice.api.server.mute.MuteManager;
import su.plo.voice.api.server.mute.ServerMuteInfo;
import su.plo.voice.api.server.mute.storage.MuteStorage;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.server.BaseVoiceServer;
import su.plo.voice.server.player.VoiceServerPlayerManagerImpl;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class VoiceMuteManager implements MuteManager {

    public static boolean isMuteValid(@NotNull ServerMuteInfo muteInfo) {
        return muteInfo.getMutedToTime() == 0 || muteInfo.getMutedToTime() > System.currentTimeMillis();
    }

    private final BaseVoiceServer voiceServer;
    private final VoiceServerPlayerManagerImpl playerManager;

    @Getter
    private MuteStorage muteStorage;

    public VoiceMuteManager(@NotNull BaseVoiceServer voiceServer,
                            @NotNull MuteStorage muteStorage,
                            @NotNull ScheduledExecutorService executor) {
        this.voiceServer = voiceServer;
        this.muteStorage = muteStorage;
        this.playerManager = voiceServer.getPlayerManager();

        executor.scheduleAtFixedRate(this::tick, 0L, 5L, TimeUnit.SECONDS);
    }

    @Override
    public Optional<ServerMuteInfo> mute(@NotNull UUID playerId,
                                         @Nullable UUID mutedById,
                                         long duration,
                                         @Nullable MuteDurationUnit durationUnit,
                                         @Nullable String reason,
                                         boolean silent) {
        VoiceServerPlayer player = playerManager.getPlayerById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        if (duration > 0 && durationUnit == null) {
            throw new IllegalArgumentException("durationUnit cannot be null if duration > 0");
        }

        if (durationUnit == MuteDurationUnit.TIMESTAMP &&
                duration - System.currentTimeMillis() <= 0L) {
            throw new IllegalArgumentException("TIMESTAMP duration should be in the future");
        }

        McTextComponent durationMessage = durationUnit == null
                ? McTextComponent.empty()
                : durationUnit.translate(duration);
        if (duration > 0) {
            duration = durationUnit.multiply(duration);

            if (durationUnit != MuteDurationUnit.TIMESTAMP) {
                duration += System.currentTimeMillis();
            }
        }

        ServerMuteInfo muteInfo = new ServerMuteInfo(playerId, mutedById, System.currentTimeMillis(), duration, reason);

        muteStorage.putPlayerMute(player.getInstance().getUuid(), muteInfo);

        voiceServer.getTcpPacketManager().broadcastPlayerInfoUpdate(player);
        if (duration > 0) {
            player.getInstance().sendMessage(
                    McTextComponent.translatable(
                            "pv.mutes.temporarily_muted",
                            durationMessage,
                            formatMuteReason(reason)
                    )
            );
        } else {
            player.getInstance().sendMessage(
                    McTextComponent.translatable("pv.mutes.permanently_muted", formatMuteReason(reason))
            );
        }

        voiceServer.getEventBus().fire(new PlayerVoiceMutedEvent(this, muteInfo));

        return Optional.of(muteInfo);
    }

    @Override
    public Optional<ServerMuteInfo> unmute(@NotNull UUID playerId, boolean silent) {
        return muteStorage.removeMuteByPlayerId(playerId)
                .map(muteInfo -> {
                    playerManager.getPlayerById(playerId)
                            .ifPresent(player -> {
                                voiceServer.getTcpPacketManager().broadcastPlayerInfoUpdate(player);
                                player.getInstance().sendMessage(McTextComponent.translatable("pv.mutes.unmuted"));
                            });

                    voiceServer.getEventBus().fire(new PlayerVoiceUnmutedEvent(this, muteInfo));

                    return muteInfo;
                });
    }

    @Override
    public Optional<ServerMuteInfo> getMute(@NotNull UUID playerId) {
        return muteStorage.getMuteByPlayerId(playerId);
    }

    @Override
    public void setMuteStorage(@NotNull MuteStorage muteStorage) {
        this.muteStorage = muteStorage;
    }

    public McTextComponent formatMuteReason(@Nullable String reason) {
        return reason == null
                ? McTextComponent.translatable("pv.mutes.empty_reason")
                : McTextComponent.literal(reason);
    }

    private void tick() {
        muteStorage.getMutedPlayers().forEach((muteInfo) -> {
            if (!isMuteValid(muteInfo)) {
                unmute(muteInfo.getPlayerUUID(), false);
            }
        });
    }
}
