import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.time.delay
import su.plo.slib.api.position.Pos3d
import su.plo.voice.BaseVoice
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

fun main() = runBlocking {
    val voiceServer = MockVoiceServer

    (0 until 4).forEach {
        val clientPlayer = MockClient(voiceServer.minecraftServer.worlds.first(), Pos3d())
        MockServerLib.addPlayer(clientPlayer)
        clientPlayer.sendVoice()
    }

    (0 until 512).forEach {
        val clientPlayer = MockClient(voiceServer.minecraftServer.worlds.first(), Pos3d())
        MockServerLib.addPlayer(clientPlayer)
    }

    while (true) {
        BaseVoice.LOGGER.info("Connections: {}", voiceServer.udpConnectionManager.connections.size)

        delay(1.seconds.toJavaDuration())
    }
}
