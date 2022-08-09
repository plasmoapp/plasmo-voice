package su.plo.voice.proto.packets.udp.serverbound;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.packets.udp.PacketUdpHandler;

public interface ServerPacketUdpHandler extends PacketUdpHandler {

    void handle(@NotNull PlayerAudioPacket packet);
}
