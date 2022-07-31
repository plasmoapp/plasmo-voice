package su.plo.voice.proto.packets.udp;

import lombok.Data;
import su.plo.voice.proto.packets.Packet;

import java.util.UUID;

@Data
public class PacketUdp {

    public static final int TTL = 2_000;

    private final UUID secret;
    private final long timestamp;
    private final Packet<?> packet;
}
