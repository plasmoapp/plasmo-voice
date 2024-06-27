package su.plo.voice.server.player;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.slib.api.entity.player.McPlayer;
import su.plo.slib.api.position.Pos3d;
import su.plo.voice.api.server.PlasmoBaseVoiceServer;
import su.plo.voice.api.server.audio.capture.ServerActivation;
import su.plo.voice.api.server.event.player.PlayerActivationDistanceUpdateEvent;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.tcp.PacketTcpCodec;
import su.plo.voice.proto.packets.tcp.clientbound.AnimatedActionBarPacket;
import su.plo.voice.proto.packets.tcp.clientbound.DistanceVisualizePacket;

import java.security.PublicKey;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@ToString(doNotUseGetters = true, exclude = "publicKey")
public abstract class BaseVoicePlayer<P extends McPlayer>
        implements VoicePlayer {

    private final PlasmoBaseVoiceServer voiceServer;
    @Getter
    protected final @NotNull P instance;

    @Setter
    protected boolean voiceDisabled;
    @Setter
    protected boolean microphoneMuted;
    @Setter
    protected @Nullable String modVersion;

    @Setter
    private PublicKey publicKey;

    private final Map<UUID, Integer> distanceByActivationId = Maps.newConcurrentMap();
    @Getter
    private final Set<ServerActivation> activeActivations = Sets.newConcurrentHashSet();
    @Getter
    private final Map<UUID, Long> lastActivationSequenceNumber = Maps.newConcurrentMap();

    @Override
    public void sendPacket(@NotNull Packet<?> packet) {
        instance.sendPacket("plasmo:voice/v2", PacketTcpCodec.encode(packet));
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
    public @NotNull Optional<String> getModVersion() {
        if (!hasVoiceChat()) {
            return Optional.empty();
        }

        return Optional.ofNullable(modVersion);
    }

    @Override
    public int getActivationDistanceById(@NotNull UUID activationId) {
        return distanceByActivationId.getOrDefault(activationId, -1);
    }

    @Override
    public void visualizeDistance(int radius, int hexColor) {
        sendPacket(new DistanceVisualizePacket(radius, hexColor, null));
    }

    @Override
    public void visualizeDistance(@NotNull Pos3d position, int radius, int hexColor) {
        sendPacket(new DistanceVisualizePacket(radius, hexColor, position));
    }

    @Override
    public void sendAnimatedActionBar(@NotNull McTextComponent text) {
        if (!hasVoiceChat()) {
            instance.sendActionBar(text);
            return;
        }

        String json = voiceServer.getMinecraftServer().getTextConverter().convertToJson(instance, text);
        sendPacket(new AnimatedActionBarPacket(json));
    }

    @Override
    public Optional<PublicKey> getPublicKey() {
        return Optional.ofNullable(publicKey);
    }

    public void setActivationDistance(@NotNull ServerActivation activation, int distance) {
        Integer oldDistance = distanceByActivationId.put(activation.getId(), distance);
        voiceServer.getEventBus().fire(new PlayerActivationDistanceUpdateEvent(
                this,
                activation,
                distance,
                oldDistance == null ? -1 : oldDistance
        ));
    }

    public void removeActivationDistance(@NotNull ServerActivation activation) {
        Integer oldDistance = distanceByActivationId.remove(activation.getId());
        voiceServer.getEventBus().fire(new PlayerActivationDistanceUpdateEvent(
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
