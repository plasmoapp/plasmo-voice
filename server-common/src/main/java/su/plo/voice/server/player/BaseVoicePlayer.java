package su.plo.voice.server.player;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.player.MinecraftServerPlayer;
import su.plo.voice.api.PlasmoVoice;
import su.plo.voice.api.server.audio.capture.ServerActivation;
import su.plo.voice.api.server.event.player.PlayerActivationDistanceUpdateEvent;
import su.plo.voice.api.server.player.PlayerModLoader;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.tcp.PacketTcpCodec;
import su.plo.voice.proto.packets.tcp.clientbound.DistanceVisualizePacket;

import java.security.PublicKey;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@ToString(doNotUseGetters = true, exclude = "publicKey")
public abstract class BaseVoicePlayer<P extends MinecraftServerPlayer> implements VoicePlayer {

    private final PlasmoVoice voice;
    @Getter
    protected final @NotNull P instance;

    @Setter
    protected boolean voiceDisabled;
    @Setter
    protected boolean microphoneMuted;

    @Setter
    private PlayerModLoader modLoader;
    @Setter
    private PublicKey publicKey;

    private final Map<UUID, Integer> distanceByActivationId = Maps.newConcurrentMap();

    @Override
    public void sendPacket(@NotNull Packet<?> packet) {
        instance.sendPacket("plasmo:voice/v2", PacketTcpCodec.encode(packet));
    }

    @Override
    public Optional<PlayerModLoader> getModLoader() {
        return Optional.ofNullable(modLoader);
    }

    @Override
    public boolean isVoiceDisabled() {
        checkVoiceChat();
        return voiceDisabled;
    }

    @Override
    public boolean isMicrophoneMuted() {
        checkVoiceChat();
        return microphoneMuted;
    }

    @Override
    public int getActivationDistanceById(@NotNull UUID activationId) {
        return distanceByActivationId.getOrDefault(activationId, -1);
    }

    @Override
    public void visualizeDistance(int radius, int hexColor) {
        sendPacket(new DistanceVisualizePacket(radius, hexColor));
    }

    @Override
    public Optional<PublicKey> getPublicKey() {
        return Optional.ofNullable(publicKey);
    }

    public void setActivationDistance(@NotNull ServerActivation activation, int distance) {
        Integer oldDistance = distanceByActivationId.put(activation.getId(), distance);
        voice.getEventBus().call(new PlayerActivationDistanceUpdateEvent(
                this,
                activation,
                distance,
                oldDistance == null ? -1 : oldDistance
        ));
    }

    public void removeActivationDistance(@NotNull ServerActivation activation) {
        Integer oldDistance = distanceByActivationId.remove(activation.getId());
        voice.getEventBus().call(new PlayerActivationDistanceUpdateEvent(
                this,
                activation,
                -1,
                oldDistance == null ? -1 : oldDistance
        ));
    }

    protected void checkVoiceChat() {
        if (!hasVoiceChat()) throw new IllegalStateException("Player is not connected to UDP server");
    }
}
