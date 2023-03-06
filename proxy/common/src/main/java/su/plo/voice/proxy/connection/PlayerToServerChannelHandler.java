package su.plo.voice.proxy.connection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.proxy.PlasmoVoiceProxy;
import su.plo.voice.api.proxy.player.VoiceProxyPlayer;
import su.plo.voice.api.server.audio.capture.ServerActivation;
import su.plo.voice.api.server.audio.source.ServerAudioSource;
import su.plo.voice.api.server.event.audio.source.PlayerSpeakEndEvent;
import su.plo.voice.proto.packets.tcp.clientbound.SourceInfoPacket;
import su.plo.voice.proto.packets.tcp.serverbound.*;
import su.plo.voice.server.player.BaseVoicePlayer;

import java.util.Objects;
import java.util.Optional;

// should be two channel handlers
// server -> proxy -> player (ServerToPlayerChannelHandler)
// player -> proxy -> server (PlayerToServerChannelHandler)
@AllArgsConstructor
public final class PlayerToServerChannelHandler implements ServerPacketTcpHandler {

    private final PlasmoVoiceProxy voiceProxy;
    @Setter
    @Getter
    private VoiceProxyPlayer player;

    @Override
    public void handle(@NotNull PlayerInfoPacket packet) {

    }

    @Override
    public void handle(@NotNull PlayerStatePacket packet) {

    }

    @Override
    public void handle(@NotNull PlayerActivationDistancesPacket packet) {
        BaseVoicePlayer<?> voicePlayer = (BaseVoicePlayer<?>) player;
        packet.getDistanceByActivationId().forEach((activationId, distance) -> {
            Optional<ServerActivation> activation = voiceProxy.getActivationManager().getActivationById(activationId);
            if (!activation.isPresent()) return;

            voicePlayer.setActivationDistance(activation.get(), distance);
        });
    }

    @Override
    public void handle(@NotNull PlayerAudioEndPacket packet) {
        if (!voiceProxy.getEventBus().call(new PlayerSpeakEndEvent(player, packet))) {
            throw new CancelForwardingException();
        }
    }

    @Override
    public void handle(@NotNull SourceInfoRequestPacket packet) {
        Optional<? extends ServerAudioSource<?>> source = voiceProxy.getSourceLineManager()
                .getLines()
                .stream()
                .map(line -> line.getSourceById(packet.getSourceId()).orElse((ServerAudioSource<?>) null))
                .filter(Objects::nonNull)
                .findFirst();
        if (!source.isPresent()) return;

        if (source.get().notMatchFilters(player)) {
            LogManager.getLogger().warn(
                    "{} tried to request a source {} to which he doesn't have access",
                    player.getInstance().getName(), source.get().getSourceInfo()
            );
            return;
        }

        player.sendPacket(new SourceInfoPacket(source.get().getSourceInfo()));
        throw new CancelForwardingException();
    }

    @Override
    public void handle(@NotNull LanguageRequestPacket packet) {
    }
}
