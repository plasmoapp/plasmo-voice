package su.plo.voice.server.audio.source;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.audio.source.ServerAudioSource;
import su.plo.voice.api.server.connection.UdpServerConnectionManager;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.pos.ServerPos3d;
import su.plo.voice.api.server.socket.UdpConnection;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.udp.bothbound.BaseAudioPacket;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

@RequiredArgsConstructor
public abstract class BaseServerSource implements ServerAudioSource {

    protected final UdpServerConnectionManager udpConnections;
    protected final UUID id;
    protected final @NotNull String codec;
    @Getter
    @Setter
    protected boolean iconVisible = true;

    private final List<Predicate<VoicePlayer>> filters = new CopyOnWriteArrayList<>();
    private final ServerPos3d playerPosition = new ServerPos3d();

    @Override
    public void addFilter(Predicate<VoicePlayer> filter) {
        if (filters.contains(filter)) throw new IllegalArgumentException("Filter already exist");
        filters.add(filter);
    }

    @Override
    public void removeFilter(Predicate<VoicePlayer> filter) {
        filters.remove(filter);
    }

    @Override
    public void sendAudioPacket(BaseAudioPacket<?> packet, short distance) {
        distance *= 2;

        ServerPos3d sourcePosition = getPosition();
        double distanceSquared = distance * distance;

        L:
        for (UdpConnection connection : udpConnections.getConnections()) {
            for (Predicate<VoicePlayer> filter : filters) {
                if (!filter.test(connection.getPlayer())) continue L;
            }

            connection.getPlayer().getPosition(playerPosition);
            if (sourcePosition.distanceSquared(playerPosition) <= distanceSquared) {
                connection.sendPacket(packet);
            }
        }
    }

    @Override
    public void sendPacket(Packet<?> packet, short distance) {
        ServerPos3d sourcePosition = getPosition();
        double distanceSquared = distance * distance;

        L:
        for (UdpConnection connection : udpConnections.getConnections()) {
            for (Predicate<VoicePlayer> filter : filters) {
                if (!filter.test(connection.getPlayer())) continue L;
            }

            connection.getPlayer().getPosition(playerPosition);
            if (sourcePosition.distanceSquared(playerPosition) <= distanceSquared) {
                connection.getPlayer().sendPacket(packet);
            }
        }
    }
}
