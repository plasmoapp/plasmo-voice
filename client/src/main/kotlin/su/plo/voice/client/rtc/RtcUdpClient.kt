package su.plo.voice.client.rtc

//#if MC>=260200
//$$ import com.google.common.io.ByteStreams
//$$ import dev.onvoid.webrtc.RTCDataChannel
//$$ import dev.onvoid.webrtc.RTCDataChannelBuffer
//$$ import dev.onvoid.webrtc.RTCDataChannelObserver
//$$ import dev.onvoid.webrtc.RTCDataChannelState
//$$ import su.plo.voice.BaseVoice
//$$ import su.plo.voice.api.client.event.connection.UdpClientPacketReceivedEvent
//$$ import su.plo.voice.api.client.event.connection.UdpClientPacketSendEvent
//$$ import su.plo.voice.api.client.event.socket.UdpClientClosedEvent
//$$ import su.plo.voice.api.client.event.socket.UdpClientConnectedEvent
//$$ import su.plo.voice.api.client.event.socket.UdpClientTimedOutEvent
//$$ import su.plo.voice.api.client.socket.UdpClient
//$$ import su.plo.voice.client.BaseVoiceClient
//$$ import su.plo.voice.client.audio.source.VoiceClientSelfSourceInfo
//$$ import su.plo.voice.client.config.VoiceClientConfig
//$$ import su.plo.voice.proto.packets.Packet
//$$ import su.plo.voice.proto.packets.PacketDirection
//$$ import su.plo.voice.proto.packets.udp.PacketUdpCodec
//$$ import su.plo.voice.proto.packets.udp.bothbound.CustomPacket
//$$ import su.plo.voice.proto.packets.udp.bothbound.PingPacket
//$$ import su.plo.voice.proto.packets.udp.clientbound.ClientPacketUdpHandler
//$$ import su.plo.voice.proto.packets.udp.clientbound.SelfAudioInfoPacket
//$$ import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket
//$$ import java.net.InetSocketAddress
//$$ import java.nio.ByteBuffer
//$$ import java.util.Optional
//$$ import java.util.UUID
//$$ import java.util.concurrent.ScheduledFuture
//$$ import java.util.concurrent.TimeUnit
//$$ import kotlin.jvm.optionals.getOrNull
//$$
//$$ class RtcUdpClient(
//$$     private val voiceClient: BaseVoiceClient,
//$$     private val config: VoiceClientConfig,
//$$     private val secret: UUID,
//$$     private val dataChannel: RTCDataChannel,
//$$ ) : UdpClient, ClientPacketUdpHandler {
//$$
//$$     @Volatile private var closed = false
//$$     @Volatile private var timedOut = false
//$$     @Volatile private var connected = false
//$$     @Volatile private var keepAlive: Long = System.currentTimeMillis()
//$$
//$$     private val ticker: ScheduledFuture<*> = voiceClient.backgroundExecutor.scheduleAtFixedRate(
//$$         ::tick, 0L, 1L, TimeUnit.SECONDS,
//$$     )
//$$
//$$     init {
//$$         dataChannel.registerObserver(object : RTCDataChannelObserver {
//$$             override fun onMessage(buf: RTCDataChannelBuffer) = handle(buf)
//$$
//$$             override fun onBufferedAmountChange(previousAmount: Long) {
//$$             }
//$$
//$$             override fun onStateChange() {
//$$                 when (dataChannel.state) {
//$$                     RTCDataChannelState.CLOSING, RTCDataChannelState.CLOSED ->
//$$                         close(UdpClientClosedEvent.Reason.DISCONNECT)
//$$                     else -> {}
//$$                 }
//$$             }
//$$         })
//$$     }
//$$
//$$     override fun connect(ip: String, port: Int) {
//$$         BaseVoice.LOGGER.info("RtcUdpClient ready (dataChannelState={})", dataChannel.state)
//$$     }
//$$
//$$     override fun close(reason: UdpClientClosedEvent.Reason) {
//$$         if (closed) return
//$$
//$$         closed = true
//$$         connected = false
//$$
//$$         ticker.cancel(false)
//$$         try {
//$$             dataChannel.unregisterObserver()
//$$         } catch (_: Throwable) {
//$$         }
//$$
//$$         voiceClient.eventBus.unregister(voiceClient, this)
//$$         voiceClient.eventBus.fire(UdpClientClosedEvent(this, reason))
//$$     }
//$$
//$$     override fun sendPacket(packet: Packet<*>) {
//$$         if (closed) return
//$$         if (dataChannel.state != RTCDataChannelState.OPEN) return
//$$
//$$         val event = UdpClientPacketSendEvent(this, packet)
//$$         if (!voiceClient.eventBus.fire(event)) return
//$$
//$$         val encoded = PacketUdpCodec.encode(packet, secret) ?: return
//$$         try {
//$$             dataChannel.send(RTCDataChannelBuffer(ByteBuffer.wrap(encoded), true))
//$$         } catch (e: Throwable) {
//$$             BaseVoice.DEBUG_LOGGER.warn("Failed to send via voice DataChannel", e)
//$$         }
//$$     }
//$$
//$$     override fun getSecret(): UUID = secret
//$$
//$$     override fun getRemoteAddress(): Optional<InetSocketAddress> = Optional.of(SYNTHETIC_REMOTE)
//$$
//$$     override fun isClosed(): Boolean = closed
//$$
//$$     override fun isConnected(): Boolean = !closed && connected
//$$
//$$     override fun isTimedOut(): Boolean = timedOut
//$$
//$$     override fun getKeepAlive(): Long = keepAlive
//$$
//$$     private fun handle(buffer: RTCDataChannelBuffer) {
//$$         val bytes = ByteArray(buffer.data.remaining())
//$$         buffer.data.get(bytes)
//$$
//$$         try {
//$$             val decoded = PacketUdpCodec.decode(ByteStreams.newDataInput(bytes), PacketDirection.CLIENT)
//$$             if (!decoded.isPresent) return
//$$             val packet: Packet<ClientPacketUdpHandler> = decoded.get().getPacket()
//$$
//$$             val event = UdpClientPacketReceivedEvent(this, packet)
//$$             voiceClient.eventBus.fire(event)
//$$             if (event.isCancelled) return
//$$
//$$             packet.handle(this)
//$$         } catch (e: Throwable) {
//$$             BaseVoice.DEBUG_LOGGER.warn("Failed to decode packet", e)
//$$         }
//$$     }
//$$
//$$     override fun handle(packet: PingPacket) {
//$$         setKeepAlive(System.currentTimeMillis())
//$$         sendPacket(PingPacket())
//$$     }
//$$
//$$     override fun handle(packet: CustomPacket) {
//$$     }
//$$
//$$     override fun handle(packet: SourceAudioPacket) {
//$$         if (config.voice.disabled.value()) return
//$$
//$$         val source = voiceClient.sourceManager.getSourceById(packet.sourceId).getOrNull() ?: return
//$$
//$$         if (source.sourceInfo.state != packet.sourceState) {
//$$             voiceClient.sourceManager.sendSourceInfoRequest(packet.sourceId, true)
//$$         }
//$$
//$$         source.process(packet)
//$$     }
//$$
//$$     override fun handle(packet: SelfAudioInfoPacket) {
//$$         if (config.voice.disabled.value()) return
//$$
//$$         val selfSourceInfo = voiceClient
//$$             .sourceManager
//$$             .getSelfSourceInfo(packet.sourceId)
//$$             .getOrNull() as? VoiceClientSelfSourceInfo
//$$             ?: return
//$$
//$$         selfSourceInfo.sequenceNumber = packet.sequenceNumber
//$$         selfSourceInfo.distance = packet.distance
//$$     }
//$$
//$$     private fun setKeepAlive(now: Long) {
//$$         val wasConnected = connected
//$$
//$$         keepAlive = now
//$$         setTimedOut(false)
//$$         connected = true
//$$
//$$         if (!wasConnected) {
//$$             BaseVoice.LOGGER.info("Voice DataChannel connected ({})", dataChannel.label)
//$$             voiceClient.eventBus.fire(UdpClientConnectedEvent(this))
//$$         }
//$$     }
//$$
//$$     private fun setTimedOut(value: Boolean) {
//$$         if (value != timedOut) {
//$$             voiceClient.eventBus.fire(UdpClientTimedOutEvent(this, value))
//$$         }
//$$         timedOut = value
//$$     }
//$$
//$$     private fun tick() {
//$$         if (closed) return
//$$
//$$         if (!connected) {
//$$             sendPacket(PingPacket())
//$$         }
//$$
//$$         val diff = System.currentTimeMillis() - keepAlive
//$$         if (diff > MAX_KEEP_ALIVE_TIMEOUT) {
//$$             BaseVoice.LOGGER.warn("Voice DataChannel timed out. Disconnecting...")
//$$             close(UdpClientClosedEvent.Reason.TIMED_OUT)
//$$         } else if (diff > MAX_SOFT_KEEP_ALIVE_TIMEOUT) {
//$$             setTimedOut(true)
//$$         }
//$$     }
//$$
//$$     companion object {
//$$         private const val MAX_KEEP_ALIVE_TIMEOUT = 30_000L
//$$         private const val MAX_SOFT_KEEP_ALIVE_TIMEOUT = 7_000L
//$$         private val SYNTHETIC_REMOTE = InetSocketAddress.createUnresolved("rtc-peer", 0)
//$$     }
//$$ }
//#endif
