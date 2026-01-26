package su.plo.voice.socket;

import com.google.common.io.ByteStreams;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.RequiredArgsConstructor;
import su.plo.voice.proto.packets.PacketDirection;
import su.plo.voice.proto.packets.udp.PacketUdp;
import su.plo.voice.proto.packets.udp.PacketUdpCodec;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public final class NettyPacketUdpDecoder extends MessageToMessageDecoder<DatagramPacket> {

    private final PacketDirection direction;

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket packet, List<Object> out) throws Exception {
        byte[] bytes = ByteBufUtil.getBytes(packet.content());

        Optional<PacketUdp> packetUdp = PacketUdpCodec.decode(ByteStreams.newDataInput(bytes), direction);
        if (!packetUdp.isPresent()) throw new IOException("Invalid packet header");
        PacketUdp decoded = packetUdp.get();

        out.add(new NettyPacketUdp(packet, bytes, decoded));
    }
}
