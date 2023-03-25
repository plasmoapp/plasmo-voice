package su.plo.voice.paper.connection

import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import su.plo.voice.server.BaseVoiceServer
import su.plo.voice.server.connection.BaseServerServiceChannelHandler
import java.io.IOException

class PaperServerServiceChannelHandler(voiceServer: BaseVoiceServer) : BaseServerServiceChannelHandler(voiceServer),
    PluginMessageListener {

    override fun onPluginMessageReceived(channelName: String, player: Player, message: ByteArray) {
        try {
            val voicePlayer = voiceServer.playerManager.wrap(player)
            handlePacket(voicePlayer, message)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
