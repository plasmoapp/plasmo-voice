package su.plo.voice.proto.packets.udp;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.packets.PacketHandler;
import su.plo.voice.proto.packets.udp.bothbound.CustomPacket;
import su.plo.voice.proto.packets.udp.bothbound.PingPacket;

public interface PacketUdpHandler extends PacketHandler {

    void handle(@NotNull PingPacket packet);

    void handle(@NotNull CustomPacket packet);
}
