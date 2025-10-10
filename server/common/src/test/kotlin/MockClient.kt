import com.google.common.io.ByteStreams
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.DatagramPacket
import io.netty.channel.socket.nio.NioDatagramChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import su.plo.slib.api.position.Pos3d
import su.plo.slib.api.server.world.McServerWorld
import su.plo.voice.BaseVoice
import su.plo.voice.BuildConstants
import su.plo.voice.proto.data.audio.capture.VoiceActivation
import su.plo.voice.proto.packets.Packet
import su.plo.voice.proto.packets.tcp.PacketTcpCodec
import su.plo.voice.proto.packets.tcp.clientbound.ActivationRegisterPacket
import su.plo.voice.proto.packets.tcp.clientbound.ActivationUnregisterPacket
import su.plo.voice.proto.packets.tcp.clientbound.AnimatedActionBarPacket
import su.plo.voice.proto.packets.tcp.clientbound.ClientPacketTcpHandler
import su.plo.voice.proto.packets.tcp.clientbound.ConfigPacket
import su.plo.voice.proto.packets.tcp.clientbound.ConfigPlayerInfoPacket
import su.plo.voice.proto.packets.tcp.clientbound.ConnectionPacket
import su.plo.voice.proto.packets.tcp.clientbound.DistanceVisualizePacket
import su.plo.voice.proto.packets.tcp.clientbound.LanguagePacket
import su.plo.voice.proto.packets.tcp.clientbound.PlayerDisconnectPacket
import su.plo.voice.proto.packets.tcp.clientbound.PlayerInfoRequestPacket
import su.plo.voice.proto.packets.tcp.clientbound.PlayerInfoUpdatePacket
import su.plo.voice.proto.packets.tcp.clientbound.PlayerListPacket
import su.plo.voice.proto.packets.tcp.clientbound.SelfSourceInfoPacket
import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket
import su.plo.voice.proto.packets.tcp.clientbound.SourceInfoPacket
import su.plo.voice.proto.packets.tcp.clientbound.SourceLinePlayerAddPacket
import su.plo.voice.proto.packets.tcp.clientbound.SourceLinePlayerRemovePacket
import su.plo.voice.proto.packets.tcp.clientbound.SourceLinePlayersListPacket
import su.plo.voice.proto.packets.tcp.clientbound.SourceLineRegisterPacket
import su.plo.voice.proto.packets.tcp.clientbound.SourceLineUnregisterPacket
import su.plo.voice.proto.packets.tcp.serverbound.PlayerInfoPacket
import su.plo.voice.proto.packets.tcp.serverbound.ServerPacketTcpHandler
import su.plo.voice.proto.packets.udp.PacketUdpCodec
import su.plo.voice.proto.packets.udp.bothbound.PingPacket
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket
import su.plo.voice.server.BaseVoiceServer
import su.plo.voice.socket.NettyExceptionHandler
import su.plo.voice.socket.NettyPacketUdp
import su.plo.voice.socket.NettyPacketUdpDecoder
import java.security.KeyPairGenerator
import java.util.UUID

class MockClient(
    world: McServerWorld,
    position: Pos3d,
) : MockServerPlayer(world, position) {
    val packetHandler = MockClientConnection(this)

    override fun sendPacket(channel: String, data: ByteArray) {
        if (channel != BaseVoiceServer.CHANNEL_STRING) return

        PacketTcpCodec.decode<ClientPacketTcpHandler>(ByteStreams.newDataInput(data))
            .ifPresent { it.handle(packetHandler) }
    }

    fun sendVoice() = CoroutineScope(Dispatchers.Default).launch {
        // TOC byte: config 12 (20ms SILK-only NB), stereo=0 (mono), code 0
        val toc = (12 shl 3) or 0
        // Add some dummy payload
        val payload = (0..150).map {
            (Byte.MIN_VALUE..Byte.MAX_VALUE).random().toByte()
        }

        var sequenceNumber = 0L
        val opusPayload = byteArrayOf(toc.toByte()) + payload

        while (true) {
            val packet = PlayerAudioPacket(
                sequenceNumber++,
                opusPayload,
                VoiceActivation.PROXIMITY_ID,
                16,
                false
            )

            packetHandler.udpClient?.sendPacket(packet)

            delay(20L)
        }
    }
}

class NettyClient(
    private val secret: UUID,
) : SimpleChannelInboundHandler<NettyPacketUdp>() {
    private val workGroup = NioEventLoopGroup()
    private lateinit var channel: NioDatagramChannel

    fun sendPacket(packet: Packet<*>) {
        if (!::channel.isInitialized) return

        val buf = try {
            Unpooled.wrappedBuffer(PacketUdpCodec.encode(packet, secret))
        } catch (e: Throwable) {
            BaseVoice.LOGGER.info("Failed to encode packet", e)
            return
        }

        channel.writeAndFlush(DatagramPacket(buf, channel.remoteAddress()))
    }

    fun connect(serverIp: String, serverPort: Int) {
        val bootstrap = Bootstrap()
        bootstrap.group(workGroup)
        bootstrap.channel(NioDatagramChannel::class.java)
        bootstrap.handler(
            object : ChannelInitializer<NioDatagramChannel>() {
                @Throws(Exception::class)
                override fun initChannel(ch: NioDatagramChannel) {
                    val pipeline = ch.pipeline()
                    pipeline.addLast("decoder", NettyPacketUdpDecoder())

                    pipeline.addLast("handler", this@NettyClient)
                    pipeline.addLast("exception_handler", NettyExceptionHandler())
                }
            },
        )

        try {
            val channelFuture = bootstrap.connect(serverIp, serverPort).sync()
            channel = channelFuture.channel() as NioDatagramChannel
            sendPacket(PingPacket(serverIp, serverPort))

            BaseVoice.LOGGER.info("Connecting to $serverIp:$serverPort")
        } catch (_: InterruptedException) {
            close()
        } catch (e: Exception) {
            close()
            throw e
        }
    }

    fun close() {
        workGroup.shutdownGracefully()
    }

    override fun channelRead0(context: ChannelHandlerContext, packet: NettyPacketUdp) {
        if (packet.packetUdp.packetUntyped is PingPacket) {
            sendPacket(PingPacket())
        }
    }
}

class MockClientConnection(
    private val serverPlayer: MockServerPlayer,
) : ClientPacketTcpHandler {
    var udpClient: NettyClient? = null
        private set

    private val keyPair by lazy {
        val generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(2048)
        generator.generateKeyPair()
    }

    fun sendPacket(packet: Packet<ServerPacketTcpHandler>) {
        val encoded = PacketTcpCodec.encode(packet)
        MockServerChannelManager.receivePacket(BaseVoiceServer.CHANNEL_STRING, serverPlayer, encoded)
    }

    override fun handle(packet: ConnectionPacket) {
        if (udpClient != null) return

        udpClient = NettyClient(packet.secret)
        udpClient?.connect(
            packet.ip.takeUnless { it == "0.0.0.0" } ?: "127.0.0.1",
            packet.port,
        )
    }

    override fun handle(packet: ConfigPlayerInfoPacket) {
    }

    override fun handle(packet: PlayerInfoRequestPacket) {
        sendPacket(
            PlayerInfoPacket(
                "1.0.0",
                BuildConstants.VERSION,
                keyPair.public.encoded,
                false,
                false,
            )
        )
    }

    override fun handle(packet: LanguagePacket) {
    }

    override fun handle(packet: ConfigPacket) {
    }

    override fun handle(packet: PlayerListPacket) {
    }

    override fun handle(packet: PlayerInfoUpdatePacket) {
    }

    override fun handle(packet: PlayerDisconnectPacket) {
    }

    override fun handle(packet: SourceAudioEndPacket) {
    }

    override fun handle(packet: SourceInfoPacket) {
    }

    override fun handle(packet: SelfSourceInfoPacket) {
    }

    override fun handle(packet: SourceLineRegisterPacket) {
    }

    override fun handle(packet: SourceLineUnregisterPacket) {
    }

    override fun handle(packet: SourceLinePlayerAddPacket) {
    }

    override fun handle(packet: SourceLinePlayerRemovePacket) {
    }

    override fun handle(packet: SourceLinePlayersListPacket) {
    }

    override fun handle(packet: ActivationRegisterPacket) {
    }

    override fun handle(packet: ActivationUnregisterPacket) {
    }

    override fun handle(packet: DistanceVisualizePacket) {
    }

    override fun handle(packet: AnimatedActionBarPacket) {
    }
}
