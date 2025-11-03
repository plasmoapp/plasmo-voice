package su.plo.voice.socket

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import su.plo.voice.BaseVoice

class NettyExceptionHandler(
    private val message: String = "Failed to handle packet",
) : ChannelInboundHandlerAdapter() {
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        BaseVoice.DEBUG_LOGGER.log(message, cause)
    }
}
