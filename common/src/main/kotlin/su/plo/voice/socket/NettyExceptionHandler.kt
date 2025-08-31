package su.plo.voice.socket

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import su.plo.voice.BaseVoice

class NettyExceptionHandler : ChannelInboundHandlerAdapter() {
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        BaseVoice.DEBUG_LOGGER.log("Failed to decode packet", cause)
    }
}
