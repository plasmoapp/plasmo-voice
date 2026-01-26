package su.plo.voice.proxy.connection

import com.google.common.collect.Maps
import com.google.common.io.ByteStreams
import su.plo.slib.api.entity.player.McPlayer
import su.plo.slib.api.event.player.McPlayerQuitEvent
import su.plo.slib.api.proxy.channel.McProxyChannelHandler
import su.plo.slib.api.proxy.connection.McProxyConnection
import su.plo.slib.api.proxy.player.McProxyPlayer
import su.plo.voice.api.proxy.event.connection.TcpPacketReceivedEvent
import su.plo.voice.api.proxy.player.VoiceProxyPlayer
import su.plo.voice.proto.packets.PacketDirection
import su.plo.voice.proto.packets.PacketHandler
import su.plo.voice.proto.packets.tcp.PacketTcpCodec
import su.plo.voice.proxy.BaseVoiceProxy
import java.util.UUID

class ProxyChannelHandler(
    private val voiceProxy: BaseVoiceProxy
) : McProxyChannelHandler {

    private val playerToServerChannels: MutableMap<UUID, PlayerToServerChannelHandler> = Maps.newConcurrentMap()
    private val serverToPlayerChannels: MutableMap<UUID, ServerToPlayerChannelHandler> = Maps.newConcurrentMap()

    init {
        McPlayerQuitEvent.registerListener(::onPlayerQuit)
    }

    override fun receive(source: McProxyConnection, destination: McProxyConnection, data: ByteArray): Boolean {
        try {
            if (source is McProxyPlayer) {
                val packet = PacketTcpCodec.decode<PacketHandler>(
                    ByteStreams.newDataInput(data),
                    PacketDirection.SERVER,
                ).orElse(null) ?: return false

                val voicePlayer: VoiceProxyPlayer = voiceProxy.playerManager.getPlayerByInstance(source.getInstance())

                val playerToServerHandler = playerToServerChannels.computeIfAbsent(source.uuid) {
                    PlayerToServerChannelHandler(
                        voiceProxy,
                        voicePlayer
                    )
                }

                if (playerToServerHandler.player != voicePlayer) {
                    playerToServerHandler.player = voicePlayer
                }

                val event = TcpPacketReceivedEvent(voicePlayer, packet)
                if (!voiceProxy.eventBus.fire(event)) return true

                try {
                    packet.handle(playerToServerHandler)
                } catch (_: CancelForwardingException) {
                    return true
                }
            } else if (destination is McProxyPlayer) {
                val packet = PacketTcpCodec.decode<PacketHandler>(
                    ByteStreams.newDataInput(data),
                    PacketDirection.CLIENT,
                ).orElse(null) ?: return false

                val voicePlayer = voiceProxy.playerManager.getPlayerByInstance(destination.getInstance())

                val serverToPlayerHandler = serverToPlayerChannels.computeIfAbsent(destination.uuid) {
                    ServerToPlayerChannelHandler(
                        voiceProxy,
                        voicePlayer
                    )
                }

                if (serverToPlayerHandler.player != voicePlayer) {
                    serverToPlayerHandler.player = voicePlayer
                }

                try {
                    packet.handle(serverToPlayerHandler)
                } catch (_: CancelForwardingException) {
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    private fun onPlayerQuit(player: McPlayer) {
        playerToServerChannels.remove(player.uuid)
        serverToPlayerChannels.remove(player.uuid)
    }
}
