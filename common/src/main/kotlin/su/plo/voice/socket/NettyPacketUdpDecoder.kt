package su.plo.voice.socket

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.socket.DatagramPacket
import io.netty.handler.codec.MessageToMessageDecoder
import su.plo.voice.proto.packets.PacketDirection
import su.plo.voice.proto.packets.udp.PacketUdpCodec

class NettyPacketUdpDecoder(
    private val direction: PacketDirection,
) : MessageToMessageDecoder<DatagramPacket>() {
    override fun decode(
        context: ChannelHandlerContext,
        packet: DatagramPacket,
        out: MutableList<Any>,
    ) {
        val duplicate = packet.retainedDuplicate()

        try {
            val packetUdp = PacketUdpCodec.decodeThrowing(ByteBufDataInput(duplicate.content()), direction)
            out.add(NettyPacketUdp(duplicate, packetUdp))
        } catch (e: Throwable) {
            duplicate.release()
            throw e
        }
    }
}
