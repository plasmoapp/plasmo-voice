package su.plo.voice.server.player;

import com.google.common.collect.Maps;
import lombok.Setter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.server.entity.MinecraftServerPlayer;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.capture.ServerActivation;
import su.plo.voice.api.server.event.connection.TcpPacketSendEvent;
import su.plo.voice.api.server.event.player.PlayerActivationDistanceUpdateEvent;
import su.plo.voice.api.server.player.PlayerModLoader;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.data.VoicePlayerInfo;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.tcp.PacketTcpCodec;
import su.plo.voice.proto.packets.tcp.clientbound.DistanceVisualizePacket;
import su.plo.voice.server.BaseVoiceServer;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

@ToString
public final class VoiceServerPlayer implements VoicePlayer {

    private final PlasmoVoiceServer voiceServer;
    private final MinecraftServerPlayer player;

    @Setter
    private PlayerModLoader modLoader;

    @Setter
    private boolean voiceDisabled;
    @Setter
    private boolean microphoneMuted;

    private final Map<UUID, Integer> distanceByActivationId = Maps.newConcurrentMap();

    public VoiceServerPlayer(@NotNull PlasmoVoiceServer voiceServer,
                             @NotNull MinecraftServerPlayer player) {
        this.voiceServer = voiceServer;
        this.player = checkNotNull(player);
    }

    @Override
    public MinecraftServerPlayer getInstance() {
        return player;
    }

    @Override
    public void sendPacket(Packet<?> packet) {
        byte[] encoded = PacketTcpCodec.encode(packet);

        TcpPacketSendEvent event = new TcpPacketSendEvent(this, packet);
        voiceServer.getEventBus().call(event);
        if (event.isCancelled()) return;

        player.sendPacket(BaseVoiceServer.CHANNEL_STRING, encoded);

        LogManager.getLogger().info("Channel packet {} sent to {}", packet, this);
    }

    @Override
    public boolean hasVoiceChat() {
        return voiceServer.getUdpConnectionManager()
                .getConnectionByUUID(player.getUUID())
                .isPresent();
    }

    @Override
    public Optional<PlayerModLoader> getModLoader() {
        return Optional.ofNullable(modLoader);
    }

    @Override
    public VoicePlayerInfo getInfo() {
        if (!hasVoiceChat()) throw new IllegalStateException("Player is not connected to UDP server");

        return new VoicePlayerInfo(
                player.getUUID(),
                player.getName(),
                voiceServer.getMuteManager()
                        .getMute(player.getUUID())
                        .isPresent(),
                isVoiceDisabled(),
                isMicrophoneMuted()
        );
    }

    @Override
    public boolean isVoiceDisabled() {
        if (!hasVoiceChat()) throw new IllegalStateException("Player is not connected to UDP server");
        return voiceDisabled;
    }

    @Override
    public boolean isMicrophoneMuted() {
        if (!hasVoiceChat()) throw new IllegalStateException("Player is not connected to UDP server");
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
    public void visualizeDistance(int radius) {
        sendPacket(new DistanceVisualizePacket(radius, 0x00a000));
    }

    public void setActivationDistance(@NotNull ServerActivation activation, int distance) {
        distanceByActivationId.put(activation.getId(), distance);
        voiceServer.getEventBus().call(new PlayerActivationDistanceUpdateEvent(this, activation, distance));
    }

    public void removeActivationDistance(@NotNull ServerActivation activation) {
        distanceByActivationId.remove(activation.getId());
        voiceServer.getEventBus().call(new PlayerActivationDistanceUpdateEvent(this, activation, -1));
    }
}
