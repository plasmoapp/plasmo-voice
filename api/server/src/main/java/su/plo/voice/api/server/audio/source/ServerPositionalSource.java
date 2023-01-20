package su.plo.voice.api.server.audio.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.server.world.ServerPos3d;
import su.plo.voice.proto.data.audio.source.SourceInfo;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket;

import java.util.UUID;

public interface ServerPositionalSource<S extends SourceInfo>
        extends ServerAudioSource<S> {

    @NotNull ServerPos3d getPosition();

    boolean sendAudioPacket(@NotNull SourceAudioPacket packet, short distance);

    boolean sendAudioPacket(@NotNull SourceAudioPacket packet, short distance, @Nullable UUID activationId);

    boolean sendPacket(Packet<?> packet, short distance);
}
