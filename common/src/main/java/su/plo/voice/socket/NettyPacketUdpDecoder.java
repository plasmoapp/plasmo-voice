package su.plo.voice.socket;

import com.google.common.io.ByteStreams;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import su.plo.voice.BaseVoice;
import su.plo.voice.proto.packets.udp.PacketUdp;
import su.plo.voice.proto.packets.udp.PacketUdpCodec;

import java.util.List;
import java.util.Optional;

public final class NettyPacketUdpDecoder extends MessageToMessageDecoder<DatagramPacket> {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        BaseVoice.DEBUG_LOGGER.log("Failed to decode packet", cause);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket packet, List<Object> out) throws Exception {
        byte[] bytes = ByteBufUtil.getBytes(packet.content());

        Optional<PacketUdp> packetUdp = PacketUdpCodec.decode(ByteStreams.newDataInput(bytes));
        if (!packetUdp.isPresent()) return;
        PacketUdp decoded = packetUdp.get();

        out.add(new NettyPacketUdp(packet, bytes, decoded));
    }
}
