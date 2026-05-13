package su.plo.voice.client.rtc

//#if MC>=260200
//$$ import dev.onvoid.webrtc.RTCDataChannel
//$$ import dev.onvoid.webrtc.RTCDataChannelBuffer
//$$ import dev.onvoid.webrtc.RTCDataChannelState
//$$ import su.plo.voice.BaseVoice
//$$ import su.plo.voice.api.server.event.audio.source.PlayerSpeakEvent
//$$ import su.plo.voice.api.server.event.connection.UdpPacketReceivedEvent
//$$ import su.plo.voice.api.server.event.connection.UdpPacketSendEvent
//$$ import su.plo.voice.api.server.event.connection.UdpPacketSentEvent
//$$ import su.plo.voice.api.server.player.VoiceServerPlayer
//$$ import su.plo.voice.api.server.socket.UdpServerConnection
//$$ import su.plo.voice.proto.packets.Packet
//$$ import su.plo.voice.proto.packets.udp.PacketUdpCodec
//$$ import su.plo.voice.proto.packets.udp.bothbound.CustomPacket
//$$ import su.plo.voice.proto.packets.udp.bothbound.PingPacket
//$$ import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket
//$$ import su.plo.voice.proto.packets.udp.serverbound.ServerPacketUdpHandler
//$$ import su.plo.voice.server.BaseVoiceServer
//$$ import java.net.InetSocketAddress
//$$ import java.nio.ByteBuffer
//$$ import java.util.UUID
//$$
//$$ class RtcUdpServerConnection(
//$$     private val voiceServer: BaseVoiceServer,
//$$     private val voiceDc: RTCDataChannel,
//$$     private val secret: UUID,
//$$     private val player: VoiceServerPlayer,
//$$ ) : UdpServerConnection, ServerPacketUdpHandler {
//$$
//$$     @Volatile private var connectionAddress: InetSocketAddress? = null
//$$     @Volatile private var keepAlive: Long = System.currentTimeMillis()
//$$     @Volatile private var sentKeepAlive: Long = 0L
//$$     @Volatile private var lastReceivedPacketTimestamp: Long = System.currentTimeMillis()
//$$     @Volatile private var connected = true
//$$
//$$     override fun getSecret(): UUID = secret
//$$
//$$     override fun getPlayer(): VoiceServerPlayer = player
//$$
//$$     override fun getRemoteAddress(): InetSocketAddress = SYNTHETIC_REMOTE
//$$
//$$     override fun getConnectionAddress(): InetSocketAddress? = connectionAddress
//$$
//$$     override fun setRemoteAddress(remoteAddress: InetSocketAddress) {
//$$     }
//$$
//$$     override fun setConnectionAddress(connectionAddress: InetSocketAddress) {
//$$         this.connectionAddress = connectionAddress
//$$     }
//$$
//$$     override fun getKeepAlive(): Long = keepAlive
//$$
//$$     override fun getSentKeepAlive(): Long = sentKeepAlive
//$$
//$$     override fun setSentKeepAlive(keepAlive: Long) { this.sentKeepAlive = keepAlive }
//$$
//$$     override fun getLastReceivedPacketTimestamp(): Long = lastReceivedPacketTimestamp
//$$
//$$     override fun isConnected(): Boolean = connected
//$$
//$$     override fun sendPacket(packet: Packet<*>) {
//$$         if (!connected) return
//$$         if (voiceDc.state != RTCDataChannelState.OPEN) return
//$$
//$$         val event = UdpPacketSendEvent(this, packet)
//$$         if (!voiceServer.eventBus.fire(event)) return
//$$
//$$         val encoded = PacketUdpCodec.encode(event.packet, secret) ?: return
//$$         try {
//$$             voiceDc.send(RTCDataChannelBuffer(ByteBuffer.wrap(encoded), true))
//$$             voiceServer.eventBus.fire(UdpPacketSentEvent(this, packet))
//$$         } catch (t: Throwable) {
//$$             BaseVoice.DEBUG_LOGGER.warn("Failed to send via voice DataChannel: {}", t.message)
//$$         }
//$$     }
//$$
//$$     override fun handlePacket(packet: Packet<ServerPacketUdpHandler>) {
//$$         val event = UdpPacketReceivedEvent(this, packet)
//$$         if (!voiceServer.eventBus.fire(event)) return
//$$
//$$         packet.handle(this)
//$$         lastReceivedPacketTimestamp = System.currentTimeMillis()
//$$     }
//$$
//$$     override fun disconnect() {
//$$         connected = false
//$$         voiceServer.tcpPacketManager.broadcastPlayerDisconnect(player)
//$$     }
//$$
//$$     override fun handle(packet: PingPacket) {
//$$         keepAlive = System.currentTimeMillis()
//$$     }
//$$
//$$     override fun handle(packet: CustomPacket) {
//$$     }
//$$
//$$     override fun handle(packet: PlayerAudioPacket) {
//$$         if (voiceServer.muteManager.getMute(player.instance.uuid).isPresent) return
//$$         if (player.isMicrophoneMuted) return
//$$         voiceServer.eventBus.fire(PlayerSpeakEvent(player, packet))
//$$     }
//$$
//$$     companion object {
//$$         private val SYNTHETIC_REMOTE = InetSocketAddress.createUnresolved("rtc-peer", 0)
//$$     }
//$$ }
//#endif
