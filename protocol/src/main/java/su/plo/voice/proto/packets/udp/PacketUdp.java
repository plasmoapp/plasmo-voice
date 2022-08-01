package su.plo.voice.proto.packets.udp;

import lombok.Data;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.PacketHandler;

import java.util.UUID;

@Data
public class PacketUdp {

    public static final int TTL = 2_000;

    private final UUID secret;
    private final long timestamp;
    private final Packet<?> packet;

    public <T extends PacketHandler> Packet<T> getPacket() {
        return (Packet<T>) packet;
    }
}
