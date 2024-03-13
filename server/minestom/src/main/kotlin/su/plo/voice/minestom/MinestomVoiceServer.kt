package su.plo.voice.minestom

import net.minestom.server.extensions.Extension
import su.plo.slib.minestom.MinestomServerLib
import su.plo.voice.server.BaseVoiceServer
import su.plo.voice.util.version.ModrinthLoader
import java.io.File

class MinestomVoiceServer(
    private val extension: Extension
) : BaseVoiceServer(ModrinthLoader.MINESTOM) {

    private val minecraftServerLib = MinestomServerLib(extension)

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

    override fun getConfigFolder(): File = extension.dataDirectory.toFile()

    override fun getMinecraftServer() = minecraftServerLib
}
