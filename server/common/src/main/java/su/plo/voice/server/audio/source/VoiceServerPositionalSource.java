package su.plo.voice.server.audio.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.server.world.ServerPos3d;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.line.ServerSourceLine;
import su.plo.voice.api.server.audio.source.ServerPositionalSource;
import su.plo.voice.api.server.event.audio.source.ServerSourceAudioPacketEvent;
import su.plo.voice.api.server.event.audio.source.ServerSourcePacketEvent;
import su.plo.voice.api.server.socket.UdpConnection;
import su.plo.voice.proto.data.audio.source.SourceInfo;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.tcp.clientbound.SourceInfoPacket;
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket;

import java.util.UUID;

public abstract class VoiceServerPositionalSource<S extends SourceInfo>
        extends BaseServerSource<S>
        implements ServerPositionalSource<S> {

    private final ServerPos3d playerPosition = new ServerPos3d();

    public VoiceServerPositionalSource(@NotNull PlasmoVoiceServer voiceServer,
                                       @NotNull AddonContainer addon,
                                       @NotNull UUID id,
                                       @NotNull ServerSourceLine line,
                                       @Nullable String codec,
                                       boolean stereo) {
        super(voiceServer, addon, id, line, codec, stereo);
    }

    @Override
    public boolean sendAudioPacket(@NotNull SourceAudioPacket packet, short distance) {
        return sendAudioPacket(packet, distance, null);
    }

    @Override
    public boolean sendAudioPacket(@NotNull SourceAudioPacket packet, short distance, @Nullable UUID activationId) {
        ServerSourceAudioPacketEvent event = new ServerSourceAudioPacketEvent(this, packet, distance, activationId);
        if (!voiceServer.getEventBus().call(event)) return false;

        distance = event.getDistance();

        packet.setSourceState((byte) state.get());
        if (dirty.compareAndSet(true, false))
            sendPacket(new SourceInfoPacket(getInfo()), distance);

        distance *= 2;

        ServerPos3d sourcePosition = getPosition();
        double distanceSquared = distance * distance;

        for (UdpConnection connection : voiceServer.getUdpConnectionManager().getConnections()) {
            if (!testPlayer(connection.getPlayer())) continue;

            connection.getPlayer().getInstance().getServerPosition(playerPosition);
            if (sourcePosition.getWorld().equals(playerPosition.getWorld()) &&
                    sourcePosition.distanceSquared(playerPosition) <= distanceSquared
            ) {
                connection.sendPacket(packet);
            }
        }

        return true;
    }

    @Override
    public boolean sendPacket(Packet<?> packet, short distance) {
        ServerSourcePacketEvent event = new ServerSourcePacketEvent(this, packet, distance);
        if (!voiceServer.getEventBus().call(event)) return false;

        distance = (short) (event.getDistance() * 2);

        ServerPos3d sourcePosition = getPosition();
        double distanceSquared = distance * distance;

        for (UdpConnection connection : voiceServer.getUdpConnectionManager().getConnections()) {
            if (!testPlayer(connection.getPlayer())) continue;

            connection.getPlayer().getInstance().getServerPosition(playerPosition);
            if (sourcePosition.getWorld().equals(playerPosition.getWorld()) &&
                    sourcePosition.distanceSquared(playerPosition) <= distanceSquared
            ) {
                connection.getPlayer().sendPacket(packet);
            }
        }

        return true;
    }
}
