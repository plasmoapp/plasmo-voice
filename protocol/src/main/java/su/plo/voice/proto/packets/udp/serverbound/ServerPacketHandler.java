package su.plo.voice.proto.packets.udp.serverbound;

import org.jetbrains.annotations.NotNull;

public interface ServerPacketHandler {
    void handle(@NotNull ServerPingPacket packet);
}
