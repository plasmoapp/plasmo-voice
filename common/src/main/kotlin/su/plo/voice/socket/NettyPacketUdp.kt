package su.plo.voice.socket

import io.netty.channel.socket.DatagramPacket
import io.netty.util.ReferenceCounted
import su.plo.voice.proto.packets.udp.PacketUdp

data class NettyPacketUdp(
    val datagramPacket: DatagramPacket,
    val packetUdp: PacketUdp,
) : ReferenceCounted {
    override fun refCnt() = datagramPacket.refCnt()

    override fun retain() = apply { datagramPacket.retain() }

    override fun retain(increment: Int) = apply { datagramPacket.retain(increment) }

    override fun touch() = apply { datagramPacket.touch() }

    override fun touch(hint: Any?) = apply { datagramPacket.touch(hint) }

    override fun release() = datagramPacket.release()

    override fun release(decrement: Int) = datagramPacket.release(decrement)
}

