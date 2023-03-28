package su.plo.voice.bungee.connection

import com.google.common.io.ByteStreams
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.connection.Server
import net.md_5.bungee.api.event.PluginMessageEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import org.apache.logging.log4j.LogManager
import su.plo.voice.proto.packets.PacketHandler
import su.plo.voice.proto.packets.PacketUtil
import su.plo.voice.proto.packets.tcp.PacketTcpCodec
import su.plo.voice.proxy.BaseVoiceProxy
import su.plo.voice.proxy.connection.CancelForwardingException
import su.plo.voice.proxy.connection.PlayerToServerChannelHandler
import su.plo.voice.proxy.connection.ServerToPlayerChannelHandler
import su.plo.voice.proxy.server.VoiceRemoteServer
import java.io.IOException
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class BungeeProxyChannelHandler(
    private val voiceProxy: BaseVoiceProxy
) : Listener {

    private val playerToServerChannels: MutableMap<UUID, PlayerToServerChannelHandler> = HashMap()
    private val serverToPlayerChannels: MutableMap<UUID, ServerToPlayerChannelHandler> = HashMap()

    init {
        ProxyServer.getInstance().registerChannel(BaseVoiceProxy.CHANNEL_STRING)
        ProxyServer.getInstance().registerChannel(BaseVoiceProxy.SERVICE_CHANNEL_STRING)
    }

    @EventHandler
    fun onPlasmoVoiceServicePacket(event: PluginMessageEvent) {
        if (event.isCancelled) return
        if (event.tag != BaseVoiceProxy.SERVICE_CHANNEL_STRING) return

        val connection = event.sender
        if (connection !is Server) return

        try {
            val input = ByteStreams.newDataInput(event.data)
            val signature = PacketUtil.readBytes(input, 32)

            val aesEncryptionKey = voiceProxy.config.aesEncryptionKey()

            val key = SecretKeySpec(
                PacketUtil.getUUIDBytes(voiceProxy.config.forwardingSecret()),
                "HmacSHA256"
            )
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(key)
            mac.update(aesEncryptionKey, 0, aesEncryptionKey.size)

            if (!MessageDigest.isEqual(signature, mac.doFinal())) {
                LogManager.getLogger().warn("Received invalid AES key signature from {}", connection)
                return
            }

            voiceProxy.remoteServerManager
                .getServer(connection.info.name)
                .orElse(null)
                ?.let { server ->
                    (server as VoiceRemoteServer).isAesEncryptionKeySet = true
                }
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } finally {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlasmoVoicePacket(event: PluginMessageEvent) {
        if (event.isCancelled) return
        if (event.tag != BaseVoiceProxy.CHANNEL_STRING) return

        try {
            val packet = PacketTcpCodec
                .decode<PacketHandler>(ByteStreams.newDataInput(event.data))
                .orElse(null) ?: return

            val sender = event.sender
            val receiver = event.receiver

            val handler = if (sender is ProxiedPlayer) {
                val voicePlayer = voiceProxy.playerManager.wrap(sender)

                val playerToServerHandler = playerToServerChannels.computeIfAbsent(
                    sender.uniqueId
                ) { _ ->
                    PlayerToServerChannelHandler(
                        voiceProxy,
                        voicePlayer
                    )
                }

                if (playerToServerHandler.player != voicePlayer) {
                    playerToServerHandler.player = voicePlayer
                }
                playerToServerHandler
            } else if (receiver is ProxiedPlayer) {
                val voicePlayer = voiceProxy.playerManager.wrap(receiver)

                val serverToPlayerHandler = serverToPlayerChannels.computeIfAbsent(
                    receiver.uniqueId
                ) { _ ->
                    ServerToPlayerChannelHandler(
                        voiceProxy,
                        voicePlayer
                    )
                }

                if (serverToPlayerHandler.player != voicePlayer) {
                    serverToPlayerHandler.player = voicePlayer
                }

                serverToPlayerHandler
            } else return

            try {
                packet.handle(handler)
            } catch (ignored: CancelForwardingException) {
                event.isCancelled = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
