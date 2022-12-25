package su.plo.voice.api.server.audio.capture;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.source.ServerAudioSource;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.data.audio.source.SelfSourceInfo;
import su.plo.voice.proto.data.audio.source.SourceInfo;
import su.plo.voice.proto.packets.tcp.clientbound.SelfSourceInfoPacket;
import su.plo.voice.proto.packets.udp.clientbound.SelfAudioInfoPacket;
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket;

import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public final class SelfActivationInfo {

    private final PlasmoVoiceServer voiceServer;
    @Getter
    private final Map<UUID, UUID> lastPlayerActivationIds = Maps.newConcurrentMap();

    public void sendAudioInfo(@NotNull VoicePlayer player,
                              @NotNull ServerAudioSource<?> source,
                              @NotNull UUID activationId,
                              @NotNull SourceAudioPacket packet) {
        sendAudioInfo(player, source, activationId, packet, false);
    }

    public void sendAudioInfo(@NotNull VoicePlayer player,
                              @NotNull ServerAudioSource<?> source,
                              @NotNull UUID activationId,
                              @NotNull SourceAudioPacket packet,
                              boolean dataChanged) {
        UUID lastActivationId = lastPlayerActivationIds.put(player.getInstance().getUUID(), activationId);
        if (lastActivationId == null || !lastActivationId.equals(activationId)) {
            updateSelfSourceInfo(player, source, null);
        }

        voiceServer.getUdpConnectionManager().getConnectionByUUID(player.getInstance().getUUID()).ifPresent((connection) -> {
            connection.sendPacket(new SelfAudioInfoPacket(
                    source.getId(),
                    packet.getSequenceNumber(),
                    dataChanged ? packet.getData() : null,
                    packet.getDistance()
            ));
        });
    }

    public void updateSelfSourceInfo(@NotNull VoicePlayer player,
                                      @NotNull ServerAudioSource<?> source,
                                      @Nullable SourceInfo sourceInfo) {
        UUID lastActivationId = lastPlayerActivationIds.get(player.getInstance().getUUID());
        if (lastActivationId == null) return;

        if (sourceInfo == null) sourceInfo = source.getInfo();

        player.sendPacket(new SelfSourceInfoPacket(new SelfSourceInfo(
                sourceInfo,
                player.getInstance().getUUID(),
                lastActivationId,
                -1L
        )));
    }
}
