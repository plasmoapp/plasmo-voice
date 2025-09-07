package su.plo.voice.socket

import io.netty.buffer.ByteBufHolder
import io.netty.channel.socket.DatagramPacket
import su.plo.voice.proto.packets.udp.PacketUdp

data class NettyPacketUdp(
    val datagramPacket: DatagramPacket,
    val packetUdp: PacketUdp,
) : ByteBufHolder by datagramPacket

