package su.plo.voice.paper.integration

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player
import su.plo.voice.api.server.PlasmoVoiceServer

class VoicePlaceholder(
    private val voiceServer: PlasmoVoiceServer
) : PlaceholderExpansion() {

    override fun getIdentifier(): String {
        return "plasmovoice"
    }

    override fun getAuthor(): String {
        return "Apehum"
    }

    override fun getVersion(): String {
        return voiceServer.version
    }

    override fun persist(): Boolean {
        return true
    }

    override fun onPlaceholderRequest(player: Player?, params: String): String? {
        return player?.let {
            val voicePlayer = voiceServer.playerManager.getPlayerById(it.uniqueId)
                .orElse(null) ?: return null

            when(params) {
                "installed" -> voicePlayer.hasVoiceChat().toString()

                "hasVoiceChat" -> voicePlayer.hasVoiceChat().toString()

                "muted" -> voiceServer.muteManager.getMute(player.uniqueId).isPresent.toString()

                "microphoneMuted" -> (voicePlayer.hasVoiceChat() && voicePlayer.isMicrophoneMuted).toString()

                "voiceDisabled" -> (voicePlayer.hasVoiceChat() && voicePlayer.isVoiceDisabled).toString()

                else -> null
            }
        } ?: when(params.lowercase()) {
            "players" -> voiceServer.udpConnectionManager.connections.size.toString()

            else -> null
        }
    }
}
