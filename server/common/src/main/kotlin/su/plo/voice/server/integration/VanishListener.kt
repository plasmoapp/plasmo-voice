package su.plo.voice.server.integration

import su.plo.slib.api.event.player.McPlayerVisibilityChangedEvent
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.proto.packets.tcp.clientbound.PlayerDisconnectPacket
import kotlin.jvm.optionals.getOrNull

fun registerVanishListener(voiceServer: PlasmoVoiceServer) {
    McPlayerVisibilityChangedEvent.registerListener { player, hidden ->
        val voicePlayer = voiceServer.playerManager.getPlayerById(player.uuid, false).getOrNull()
            ?: return@registerListener
        if (!voicePlayer.hasVoiceChat()) return@registerListener

        if (hidden) {
            voiceServer.tcpPacketManager.broadcast(
                PlayerDisconnectPacket(player.uuid),
            ) { other ->
                other.instance.uuid != player.uuid && !other.instance.canSee(player)
            }
        } else {
            voiceServer.tcpPacketManager.broadcastPlayerInfoUpdate(voicePlayer)
        }
    }
}
