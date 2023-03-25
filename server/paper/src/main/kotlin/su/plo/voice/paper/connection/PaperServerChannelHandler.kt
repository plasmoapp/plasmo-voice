package su.plo.voice.paper.connection

import com.google.common.collect.Maps
import com.google.common.io.ByteStreams
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerRegisterChannelEvent
import org.bukkit.plugin.messaging.PluginMessageListener
import su.plo.voice.proto.packets.PacketHandler
import su.plo.voice.proto.packets.tcp.PacketTcpCodec
import su.plo.voice.server.BaseVoiceServer
import su.plo.voice.server.connection.BaseServerChannelHandler
import su.plo.voice.server.connection.PlayerChannelHandler
import java.io.IOException
import java.util.*
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class PaperServerChannelHandler(voiceServer: BaseVoiceServer) : BaseServerChannelHandler(voiceServer),
    PluginMessageListener, Listener {

    private val channelsUpdates: MutableMap<UUID, MutableList<String>> = Maps.newConcurrentMap()
    private val channelsFutures: MutableMap<UUID, ScheduledFuture<*>> = Maps.newConcurrentMap()

    override fun onPluginMessageReceived(channelName: String, player: Player, message: ByteArray) {
        try {
            PacketTcpCodec.decode<PacketHandler>(ByteStreams.newDataInput(message))
                .ifPresent { packet ->
//                        LogManager.getLogger().info("Channel packet received {}", packet);

                    val voicePlayer = voiceServer.playerManager.wrap(player)

                    val channel = channels.computeIfAbsent(
                        player.uniqueId
                    ) { _ -> PlayerChannelHandler(voiceServer, voicePlayer) }

                    channel.handlePacket(packet)
                }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @EventHandler
    fun onPlayerRegisterChannel(event: PlayerRegisterChannelEvent) {
        val player = event.player
        val channel = event.channel

        val updates = channelsUpdates.computeIfAbsent(
            player.uniqueId
        ) { _ -> ArrayList() }
        if (updates.contains(channel)) return
        updates.add(channel)

        val future = channelsFutures[player.uniqueId]
        future?.cancel(false)

        channelsFutures[player.uniqueId] = voiceServer.backgroundExecutor.schedule({
            channelsFutures.remove(player.uniqueId)

            val channels = channelsUpdates.remove(player.uniqueId) ?: return@schedule

            handleRegisterChannels(channels, voiceServer.playerManager.wrap(player))
        }, 500L, TimeUnit.MILLISECONDS)
    }
}
