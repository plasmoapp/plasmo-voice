package su.plo.voice.socket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import su.plo.voice.proto.packets.udp.PacketUdp;
import su.plo.voice.proto.packets.udp.PacketUdpCodec;

import java.util.List;

public final class NettyPacketUdpDecoder extends MessageToMessageDecoder<DatagramPacket> {

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket packet, List<Object> out) throws Exception {
        PacketUdp packetUdp = PacketUdpCodec.decodeThrowing(new ByteBufDataInput(packet.content()));

        // Retain the DatagramPacket here because:
        // The input packet will be released by MessageToMessageDecoder after this method returns,
        // but NettyPacketUdp needs to keep the buffer alive for downstream handlers
        // SimpleChannelInboundHandler will auto-release NettyPacketUdp (ByteBufHolder) after channelRead0()
        out.add(new NettyPacketUdp(packet.retainedDuplicate(), packetUdp));
    }
}
