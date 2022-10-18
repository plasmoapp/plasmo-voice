package su.plo.voice.server.mute;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.voice.api.server.mute.MuteDurationUnit;
import su.plo.voice.api.server.mute.MuteManager;
import su.plo.voice.api.server.mute.ServerMuteInfo;
import su.plo.voice.api.server.mute.storage.MuteStorage;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.player.VoicePlayerManager;
import su.plo.voice.server.BaseVoiceServer;
import su.plo.voice.server.config.ServerLanguage;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class VoiceMuteManager implements MuteManager {

    public static boolean isMuteValid(@NotNull ServerMuteInfo muteInfo) {
        return muteInfo.getMutedToTime() == 0 || muteInfo.getMutedToTime() > System.currentTimeMillis();
    }

    private final BaseVoiceServer voiceServer;
    private final MuteStorage storage;
    private final VoicePlayerManager playerManager;

    public VoiceMuteManager(@NotNull BaseVoiceServer voiceServer,
                            @NotNull MuteStorage storage,
                            @NotNull ScheduledExecutorService executor) {
        this.voiceServer = voiceServer;
        this.storage = storage;
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
        VoicePlayer player = playerManager.getPlayerById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        ServerLanguage language = voiceServer.getLanguages().getLanguage(player.getInstance());

        if (duration > 0 && durationUnit == null) {
            throw new IllegalArgumentException("durationUnit cannot be null if duration > 0");
        }

        if (durationUnit == MuteDurationUnit.TIMESTAMP &&
                duration - System.currentTimeMillis() <= 0L) {
            throw new IllegalArgumentException("TIMESTAMP duration should be in the future");
        }

        String durationMessage = durationUnit == null
                ? ""
                : language.mutes().durations().format(duration, durationUnit);
        if (duration > 0) {
            duration = durationUnit.multiply(duration);

            if (durationUnit != MuteDurationUnit.TIMESTAMP) {
                duration += System.currentTimeMillis();
            }
        }

        ServerMuteInfo muteInfo = new ServerMuteInfo(playerId, mutedById, System.currentTimeMillis(), duration, reason);

        storage.putPlayerMute(player.getInstance().getUUID(), muteInfo);

        voiceServer.getTcpConnectionManager().broadcastPlayerInfoUpdate(player);
        if (duration > 0) {
            player.getInstance().sendMessage(String.format(
                    language.mutes().temporallyMuted(),
                    durationMessage,
                    formatMuteReason(language, reason)
            ));
        } else {
            player.getInstance().sendMessage(MinecraftTextComponent.translatable(
                    language.mutes().permanentlyMuted(),
                    formatMuteReason(language, reason)
            ));
        }

        // todo: voice mute event

        return Optional.of(muteInfo);
    }

    @Override
    public Optional<ServerMuteInfo> unmute(@NotNull UUID playerId, boolean silent) {
        return storage.removeMuteByPlayerId(playerId)
                .map(muteInfo -> {
                    playerManager.getPlayerById(playerId)
                            .ifPresent(player -> {
                                ServerLanguage language = voiceServer.getLanguages().getLanguage(player.getInstance());

                                voiceServer.getTcpConnectionManager().broadcastPlayerInfoUpdate(player);
                                player.getInstance().sendMessage(language.mutes().unmuted());
                            });

                    // todo: voice unmute event

                    return muteInfo;
                });
    }

    @Override
    public Optional<ServerMuteInfo> getMute(@NotNull UUID playerId) {
        return storage.getMuteByPlayerId(playerId);
    }

    @Override
    public Collection<ServerMuteInfo> getMutedPlayers() {
        return storage.getMutedPlayers();
    }

    public String formatMuteReason(@NotNull ServerLanguage language, @Nullable String reason) {
        return reason == null
                ? language.mutes().emptyReason()
                : reason;
    }

    private void tick() {
        getMutedPlayers().forEach((muteInfo) -> {
            if (!isMuteValid(muteInfo)) {
                unmute(muteInfo.getPlayerUUID(), false);
            }
        });
    }
}
