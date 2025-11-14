package su.plo.voice.paper.integration

import de.myzelyam.api.vanish.PostPlayerHideEvent
import de.myzelyam.api.vanish.PostPlayerShowEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.proto.packets.tcp.clientbound.PlayerDisconnectPacket
import kotlin.jvm.optionals.getOrNull

class SuperVanishIntegration(
    private val voiceServer: PlasmoVoiceServer,
) : Listener {

    @EventHandler
    fun onPlayerHide(event: PostPlayerHideEvent) {
        val player = voiceServer.playerManager.getPlayerById(event.player.uniqueId, false).getOrNull() ?: return
        if (!player.hasVoiceChat()) return

        voiceServer.tcpPacketManager.broadcast(
            PlayerDisconnectPacket(player.instance.uuid),
        ) { other ->
            other.instance.uuid != player.instance.uuid && !other.instance.canSee(player.instance)
        }
    }

    @EventHandler
    fun onPlayerShow(event: PostPlayerShowEvent) {
        val player = voiceServer.playerManager.getPlayerById(event.player.uniqueId, false).getOrNull() ?: return
        if (!player.hasVoiceChat()) return

        voiceServer.tcpPacketManager.broadcastPlayerInfoUpdate(player)
    }
}
