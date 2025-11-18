import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.time.delay
import su.plo.slib.api.position.Pos3d
import su.plo.voice.BaseVoice
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

fun main() = runBlocking {
    val voiceServer = MockVoiceServer
    val world = voiceServer.minecraftServer.worlds.first()

    val totalPlayers = 1000
    val activeSpeakers = 50
    val worldSize = 200.0

    val allClients = (0 until totalPlayers).map {
        async(Dispatchers.Default) {
            val randomPos = Pos3d(
                Math.random() * worldSize - worldSize / 2,
                64.0,
                Math.random() * worldSize - worldSize / 2
            )
            val clientPlayer = MockClient(world, randomPos)
            MockServerLib.addPlayer(clientPlayer)
            clientPlayer
        }
    }.awaitAll()

    (0 until activeSpeakers).forEach { idx ->
        allClients[idx].sendVoice()
    }

    while (true) {
        BaseVoice.LOGGER.info(
            "Connections: {}, Active speakers: {}",
            voiceServer.udpConnectionManager.connections.size,
            activeSpeakers,
        )

        delay(1.seconds.toJavaDuration())
    }
}
