package su.plo.voice.minestom

import su.plo.slib.minestom.MinestomServerLib
import su.plo.voice.server.BaseVoiceServer
import su.plo.voice.util.version.PlatformLoader
import java.io.File

class MinestomVoiceServer(
    private val dataDirectory: File
) : BaseVoiceServer(PlatformLoader.MINESTOM) {

    private val minecraftServerLib = MinestomServerLib(dataDirectory)

    public override fun onInitialize() {
        minecraftServerLib.onInitialize()

        super.onInitialize()

        minecraftServerLib.players.forEach { player ->
            playerManager.getPlayerById(player.uuid)
                .ifPresent { voicePlayer ->
                    if (player.registeredChannels.contains(CHANNEL_STRING)) {
                        tcpPacketManager.requestPlayerInfo(voicePlayer)
                    }
                }
        }
    }

    public override fun onShutdown() {
        super.onShutdown()
        minecraftServerLib.onShutdown()
    }

    override fun getConfigFolder(): File = dataDirectory

    override fun getMinecraftServer() = minecraftServerLib
}
