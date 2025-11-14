package su.plo.voice.paper.integration

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerHideEntityEvent
import org.bukkit.event.player.PlayerShowEntityEvent
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.proto.packets.tcp.clientbound.PlayerDisconnectPacket
import su.plo.voice.proto.packets.tcp.clientbound.PlayerInfoUpdatePacket
import kotlin.jvm.optionals.getOrNull

class SpigotVanishIntegration(
    private val voiceServer: PlasmoVoiceServer,
) : Listener {

    @EventHandler
    fun onPlayerHideEntity(event: PlayerHideEntityEvent) {
        val player = event.player
        val hiddenPlayer = event.entity as? Player ?: return

        val voicePlayer = voiceServer.playerManager.getPlayerById(player.uniqueId, false).getOrNull() ?: return
        val hiddenVoicePlayer = voiceServer.playerManager.getPlayerById(hiddenPlayer.uniqueId, false).getOrNull() ?: return
        if (!hiddenVoicePlayer.hasVoiceChat()) return

        voicePlayer.sendPacket(PlayerDisconnectPacket(hiddenPlayer.uniqueId))
    }

    @EventHandler
    fun onPlayerShowEntity(event: PlayerShowEntityEvent) {
        val player = event.player
        val showedPlayer = event.entity as? Player ?: return

        val voicePlayer = voiceServer.playerManager.getPlayerById(player.uniqueId, false).getOrNull() ?: return
        val showedVoicePlayer = voiceServer.playerManager.getPlayerById(showedPlayer.uniqueId, false).getOrNull() ?: return
        if (!showedVoicePlayer.hasVoiceChat()) return

        voicePlayer.sendPacket(PlayerInfoUpdatePacket(showedVoicePlayer.createPlayerInfo()))
    }
}
