package su.plo.voice.server.socket;

import com.google.common.io.ByteStreams;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import su.plo.voice.proto.packets.udp.PacketUdp;
import su.plo.voice.proto.packets.udp.PacketUdpCodec;

import java.util.List;

public class NettyMessageDecoder extends MessageToMessageDecoder<DatagramPacket> {
    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket packet, List<Object> out) throws Exception {
        int readableBytes = packet.content().readableBytes();
        if (readableBytes <= 0) return;

        byte[] bytes = new byte[readableBytes];
        packet.content().readBytes(bytes);

        PacketUdp decoded = PacketUdpCodec.decode(ByteStreams.newDataInput(bytes));
        if (decoded == null) return;

        if (System.currentTimeMillis() - decoded.getTimestamp() > PacketUdp.TTL) return;

        out.add(decoded);
    }
}
