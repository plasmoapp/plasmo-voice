package su.plo.voice.proto.packets.tcp.serverbound;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.packets.tcp.PacketTcpHandler;

public interface ServerPacketTcpHandler extends PacketTcpHandler {

    void handle(@NotNull PlayerAudioEndPacket packet);
}
