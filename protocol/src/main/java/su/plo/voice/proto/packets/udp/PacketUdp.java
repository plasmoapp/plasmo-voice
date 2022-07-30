package su.plo.voice.proto.packets.udp;

import lombok.Data;
import su.plo.voice.proto.packets.Packet;

import java.util.UUID;

@Data
public class PacketUdp {

    private final UUID secret;
    private final Packet<?> packet;
}
