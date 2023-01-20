package su.plo.voice.socket;

import io.netty.channel.socket.DatagramPacket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import su.plo.voice.proto.packets.udp.PacketUdp;

@RequiredArgsConstructor
public final class NettyPacketUdp {

    @Getter
    private final DatagramPacket datagramPacket;
    @Getter
    private final byte[] packetData;
    @Getter
    private final PacketUdp packetUdp;
}
