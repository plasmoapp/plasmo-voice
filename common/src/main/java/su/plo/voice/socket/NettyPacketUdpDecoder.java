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

        out.add(new NettyPacketUdp(packet.retainedDuplicate(), packetUdp));
    }
}
