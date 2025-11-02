package su.plo.voice.server.metrics

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.socket.DatagramPacket
import su.plo.voice.server.BaseVoiceServer
import su.plo.voice.socket.NettyPacketUdp
import java.time.Duration
import kotlin.system.measureNanoTime

class NettyPacketExceptionHandlerMetrics(
    private val voiceServer: BaseVoiceServer,
    private val stage: Metrics.PacketHandlerErrorStage,
) : ChannelInboundHandlerAdapter() {
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable?) {
        voiceServer.metrics?.handlerError(stage)
        super.exceptionCaught(ctx, cause)
    }
}

class NettyPacketHandlerPreDecoderMetrics(
    private val voiceServer: BaseVoiceServer,
) : SimpleChannelInboundHandler<DatagramPacket>(false) {
    override fun channelRead0(ctx: ChannelHandlerContext, packet: DatagramPacket) {
        val duration = measureNanoTime { ctx.fireChannelRead(packet) }

        voiceServer.metrics?.recordPipelineTimeSeconds(Duration.ofNanos(duration))
    }
}

class NettyPacketHandlerPreHandlerMetrics(
    private val voiceServer: BaseVoiceServer,
) : SimpleChannelInboundHandler<NettyPacketUdp>(false) {
    override fun channelRead0(ctx: ChannelHandlerContext, packet: NettyPacketUdp) {
        val duration = measureNanoTime { ctx.fireChannelRead(packet) }

        voiceServer.metrics?.recordHandlerTimeSeconds(Duration.ofNanos(duration))
    }
}

class NettyPacketHandlerPostDecoderMetrics(
    private val voiceServer: BaseVoiceServer,
) : SimpleChannelInboundHandler<NettyPacketUdp>(false) {
    override fun channelRead0(ctx: ChannelHandlerContext, nettyPacket: NettyPacketUdp) {
        voiceServer.metrics?.recordPacket(
            Metrics.PacketDirection.In,
            getPacketKind(nettyPacket.packetUdp.packetUntyped),
            nettyPacket.datagramPacket.content().readableBytes(),
        )
        ctx.fireChannelRead(nettyPacket)
    }
}
