package su.plo.voice.proto.packets.udp.clientbound;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.packets.udp.PacketUdpHandler;

public interface ClientPacketUdpHandler extends PacketUdpHandler {

    void handle(@NotNull SourceAudioPacket packet);

    void handle(@NotNull SelfAudioInfoPacket packet);
}
