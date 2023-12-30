package su.plo.voice.paper.integration

import de.myzelyam.api.vanish.PlayerHideEvent
import de.myzelyam.api.vanish.PostPlayerShowEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import su.plo.voice.api.server.PlasmoVoiceServer

class SuperVanishIntegration(
    private val voiceServer: PlasmoVoiceServer
) : Listener {

    @EventHandler
    fun onPlayerHide(event: PlayerHideEvent) {
        val player = voiceServer.playerManager.getPlayerByInstance(event.player)
        if (!player.hasVoiceChat()) return

        voiceServer.tcpPacketManager.broadcastPlayerDisconnect(player)
    }

    @EventHandler
    fun onPlayerShow(event: PostPlayerShowEvent) {
        val player = voiceServer.playerManager.getPlayerByInstance(event.player)
        if (!player.hasVoiceChat()) return

        voiceServer.tcpPacketManager.broadcastPlayerInfoUpdate(player)
    }
}
