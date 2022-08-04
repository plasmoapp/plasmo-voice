package su.plo.voice.socket;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import su.plo.voice.proto.packets.udp.PacketUdp;

import java.net.InetSocketAddress;

@RequiredArgsConstructor
public final class NettyPacketUdp {

    @Getter
    private final PacketUdp packetUdp;
    @Getter
    private final InetSocketAddress sender;
}
