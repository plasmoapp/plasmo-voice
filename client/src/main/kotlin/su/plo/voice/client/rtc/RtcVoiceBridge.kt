package su.plo.voice.client.rtc

//#if MC>=260200
//$$ import com.google.common.io.ByteStreams
//$$ import dev.onvoid.webrtc.RTCDataChannel
//$$ import dev.onvoid.webrtc.RTCDataChannelBuffer
//$$ import dev.onvoid.webrtc.RTCDataChannelInit
//$$ import dev.onvoid.webrtc.RTCDataChannelObserver
//$$ import dev.onvoid.webrtc.RTCDataChannelState
//$$ import dev.onvoid.webrtc.RTCPeerConnection
//$$ import su.plo.slib.api.logging.McLoggerFactory
//$$ import su.plo.voice.BaseVoice
//$$ import su.plo.voice.api.client.event.socket.UdpClientClosedEvent
//$$ import su.plo.voice.api.client.event.socket.UdpClientConnectEvent
//$$ import su.plo.voice.api.event.EventSubscribe
//$$ import su.plo.voice.api.server.event.connection.UdpClientDisconnectedEvent
//$$ import su.plo.voice.client.BaseVoiceClient
//$$ import su.plo.voice.client.ModVoiceClient
//$$ import su.plo.voice.proto.packets.PacketDirection
//$$ import su.plo.voice.proto.packets.udp.PacketUdpCodec
//$$ import su.plo.voice.proto.packets.udp.bothbound.PingPacket
//$$ import su.plo.voice.server.ModVoiceServer
//$$ import java.net.InetSocketAddress
//$$ import java.util.concurrent.ConcurrentHashMap
//$$ import java.util.concurrent.atomic.AtomicReference
//$$ import kotlin.jvm.optionals.getOrNull
//$$
//$$ object RtcVoiceBridge {
//$$     private val LOGGER = McLoggerFactory.createLogger(RtcVoiceBridge::class.java.name)
//$$
//$$     const val VOICE_DATA_CHANNEL_LABEL: String = "plasmovoice"
//$$
//$$     private val voiceChannel = AtomicReference<VoiceChannel?>(null)
//$$     private val voiceChannels = ConcurrentHashMap<RTCPeerConnection, VoiceChannelHandler>()
//$$
//$$     private data class VoiceChannel(
//$$         val peerConnection: RTCPeerConnection,
//$$         val voiceDataChannel: RTCDataChannel,
//$$         val udpClient: RtcUdpClient? = null,
//$$     )
//$$
//$$     @JvmStatic
//$$     fun openVoiceChannel(peerConnection: RTCPeerConnection) {
//$$         val init = RTCDataChannelInit().apply {
//$$             ordered = false
//$$             maxRetransmits = 0
//$$         }
//$$
//$$         val dataChannel = try {
//$$             peerConnection.createDataChannel(VOICE_DATA_CHANNEL_LABEL, init)
//$$         } catch (e: Throwable) {
//$$             LOGGER.warn("Failed to create voice RTC DataChannel", e)
//$$             return
//$$         }
//$$
//$$         // fixes a leak when RtcUdpClient is not created (eg Plasmo Voice is not installed on the server)
//$$         dataChannel.registerObserver(object : RTCDataChannelObserver {
//$$             override fun onMessage(buf: RTCDataChannelBuffer) {}
//$$             override fun onBufferedAmountChange(previousAmount: Long) {}
//$$             override fun onStateChange() {
//$$                 when (dataChannel.state) {
//$$                     RTCDataChannelState.CLOSING, RTCDataChannelState.CLOSED ->
//$$                         onPeerConnectionClosed(peerConnection)
//$$                     else -> {}
//$$                 }
//$$             }
//$$         })
//$$
//$$         voiceChannel.getAndSet(VoiceChannel(peerConnection, dataChannel))
//$$             ?.udpClient
//$$             ?.close(UdpClientClosedEvent.Reason.RECONNECT)
//$$
//$$         LOGGER.info("Created voice RTC DataChannel")
//$$     }
//$$
//$$     @JvmStatic
//$$     fun acceptVoiceChannel(peerConnection: RTCPeerConnection, dataChannel: RTCDataChannel) {
//$$         voiceChannels.remove(peerConnection)?.close()
//$$         voiceChannels[peerConnection] = VoiceChannelHandler(peerConnection, dataChannel)
//$$     }
//$$
//$$     @JvmStatic
//$$     fun onPeerConnectionClosed(peerConnection: RTCPeerConnection) {
//$$         voiceChannels.remove(peerConnection)?.close()
//$$
//$$         val currentChannel = voiceChannel.get()
//$$             ?.takeIf { it.peerConnection == peerConnection }
//$$             ?: return
//$$
//$$         voiceChannel.compareAndSet(currentChannel, null)
//$$         currentChannel.udpClient?.close(UdpClientClosedEvent.Reason.DISCONNECT)
//$$     }
//$$
//$$     @EventSubscribe
//$$     fun onUdpClientClosed(event: UdpClientClosedEvent) {
//$$         val current = voiceChannel.get() ?: return
//$$         if (current.udpClient === event.client) {
//$$             voiceChannel.compareAndSet(current, null)
//$$         }
//$$     }
//$$
//$$     @EventSubscribe
//$$     fun onUdpConnect(event: UdpClientConnectEvent) {
//$$         val currentChannel = voiceChannel.get() ?: return
//$$
//$$         val voiceClient = ModVoiceClient.INSTANCE as? BaseVoiceClient ?: return
//$$         if (currentChannel.voiceDataChannel.state == RTCDataChannelState.CLOSING ||
//$$             currentChannel.voiceDataChannel.state == RTCDataChannelState.CLOSED
//$$         ) {
//$$             LOGGER.warn(
//$$                 "Voice DataChannel not open (state={}); leaving Plasmo Voice UDP target unchanged",
//$$                 currentChannel.voiceDataChannel.state,
//$$             )
//$$             return
//$$         }
//$$
//$$         val rtcUdpClient = RtcUdpClient(
//$$             voiceClient,
//$$             voiceClient.config,
//$$             event.connectionPacket.secret,
//$$             currentChannel.voiceDataChannel,
//$$         )
//$$         voiceChannel.compareAndSet(
//$$             currentChannel,
//$$             currentChannel.copy(udpClient = rtcUdpClient),
//$$         )
//$$         event.client = rtcUdpClient
//$$
//$$         LOGGER.info("Routing Plasmo Voice UDP through P2P voice DataChannel")
//$$     }
//$$
//$$     private class VoiceChannelHandler(
//$$         private val peerConnection: RTCPeerConnection,
//$$         private val voiceDataChannel: RTCDataChannel,
//$$     ) {
//$$         private val voiceServer by lazy { ModVoiceServer.INSTANCE }
//$$
//$$         @Volatile private var registeredConnection: RtcUdpServerConnection? = null
//$$         @Volatile private var closed = false
//$$
//$$         init {
//$$             voiceDataChannel.registerObserver(object : RTCDataChannelObserver {
//$$                 override fun onMessage(buf: RTCDataChannelBuffer) = handle(buf)
//$$
//$$                 override fun onBufferedAmountChange(previousAmount: Long) {
//$$                 }
//$$
//$$                 override fun onStateChange() {
//$$                     when (voiceDataChannel.state) {
//$$                         RTCDataChannelState.CLOSING, RTCDataChannelState.CLOSED -> close()
//$$                         else -> {}
//$$                     }
//$$                 }
//$$             })
//$$         }
//$$
//$$         fun close() {
//$$             if (closed) return
//$$             closed = true
//$$
//$$             voiceChannels.remove(peerConnection, this)
//$$
//$$             try {
//$$                 voiceDataChannel.unregisterObserver()
//$$             } catch (_: Throwable) {
//$$             }
//$$
//$$             registeredConnection?.let { connection ->
//$$                 voiceServer
//$$                     .udpConnectionManager
//$$                     .removeConnection(connection, UdpClientDisconnectedEvent.Reason.DISCONNECT)
//$$             }
//$$         }
//$$
//$$         private fun handle(buf: RTCDataChannelBuffer) {
//$$             if (closed) return
//$$
//$$             val bytes = ByteArray(buf.data.remaining())
//$$             buf.data.get(bytes)
//$$
//$$             val decoded = try {
//$$                 PacketUdpCodec.decode(ByteStreams.newDataInput(bytes), PacketDirection.SERVER)
//$$             } catch (e: Throwable) {
//$$                 BaseVoice.DEBUG_LOGGER.warn("Failed to decode packet", e)
//$$                 return
//$$             }
//$$
//$$             if (!decoded.isPresent) return
//$$             val packetUdp = decoded.get()
//$$             val secret = packetUdp.secret
//$$
//$$             val udpConnectionManager = voiceServer.udpConnectionManager
//$$
//$$             val existing = udpConnectionManager.getConnectionBySecret(secret).getOrNull()
//$$             if (existing != null) {
//$$                 try {
//$$                     existing.handlePacket(packetUdp.getPacket())
//$$                 } catch (e: Throwable) {
//$$                     BaseVoice.DEBUG_LOGGER.warn("Failed to handle packet", e)
//$$                 }
//$$                 return
//$$             }
//$$
//$$             val playerId = udpConnectionManager.getPlayerIdBySecret(secret).getOrNull() ?: return
//$$             val player = voiceServer.playerManager.getPlayerById(playerId).getOrNull() ?: return
//$$
//$$             val connection = RtcUdpServerConnection(voiceServer, voiceDataChannel, secret, player)
//$$             try {
//$$                 val pingPacket = packetUdp.packetUntyped as? PingPacket
//$$                 if (pingPacket?.serverIp != null) {
//$$                     connection.setConnectionAddress(InetSocketAddress(pingPacket.serverIp, pingPacket.serverPort))
//$$                 }
//$$             } catch (_: Throwable) {
//$$             }
//$$
//$$             registeredConnection = connection
//$$             udpConnectionManager.addConnection(connection)
//$$
//$$             voiceServer.tcpPacketManager.sendConfigInfo(player)
//$$             voiceServer.tcpPacketManager.sendPlayerList(player)
//$$             voiceServer.tcpPacketManager.broadcastPlayerInfoUpdate(player)
//$$
//$$             try {
//$$                 connection.handlePacket(packetUdp.getPacket())
//$$             } catch (e: Throwable) {
//$$                 BaseVoice.DEBUG_LOGGER.warn("Failed to handle packet", e)
//$$             }
//$$         }
//$$     }
//$$ }
//#endif
