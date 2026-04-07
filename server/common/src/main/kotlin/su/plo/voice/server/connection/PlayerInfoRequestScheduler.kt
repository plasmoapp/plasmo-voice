package su.plo.voice.server.connection

import com.google.common.collect.Maps
import su.plo.slib.api.entity.player.McPlayer
import su.plo.slib.api.event.player.McPlayerJoinEvent
import su.plo.slib.api.event.player.McPlayerQuitEvent
import su.plo.voice.BaseVoice
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.api.server.event.connection.TcpPacketReceivedEvent
import su.plo.voice.api.server.player.VoiceServerPlayer
import su.plo.voice.server.BaseVoiceServer
import java.util.UUID
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class PlayerInfoRequestScheduler(
    private val voiceServer: BaseVoiceServer
) {

    private val retryDelaysMs = longArrayOf(
        1_000,
        3_000,
        5_000,
        10_000,
        15_000,
    )
    private val tickIntervalMs = 500L

    private val pendingPlayers: MutableMap<UUID, PlayerRequestState> = Maps.newConcurrentMap()

    private val tickFuture: ScheduledFuture<*> = voiceServer.backgroundExecutor.scheduleAtFixedRate(
        ::tick,
        0L,
        tickIntervalMs,
        TimeUnit.MILLISECONDS,
    )

    init {
        McPlayerJoinEvent.registerListener(::handlePlayerJoin)
        McPlayerQuitEvent.registerListener(::handlePlayerQuit)
    }

    fun clear() {
        McPlayerJoinEvent.unregisterListener(::handlePlayerJoin)
        McPlayerQuitEvent.unregisterListener(::handlePlayerQuit)

        tickFuture.cancel(false)
        pendingPlayers.clear()
    }

    @EventSubscribe
    fun TcpPacketReceivedEvent.onTcpPacketReceived() {
        pendingPlayers.remove(player.instance.uuid)
    }

    private fun handlePlayerJoin(player: McPlayer) {
        if (!voiceServer.udpServer.isPresent || voiceServer.config == null) return

        val voicePlayer = voiceServer.playerManager.getPlayerByInstance(player.getInstance())
        val now = System.currentTimeMillis()

        pendingPlayers[player.uuid] = PlayerRequestState(voicePlayer, now, 0)

        voiceServer.backgroundExecutor.execute {
            voiceServer.minecraftServer.executeInMainThread {
                voiceServer.tcpPacketManager.requestPlayerInfo(voicePlayer)
            }
        }
    }

    private fun handlePlayerQuit(player: McPlayer) {
        pendingPlayers.remove(player.uuid)
    }

    private fun tick() {
        pendingPlayers.entries.removeIf { (_, state) ->
            state.player.publicKey.isPresent || state.attempt >= retryDelaysMs.size
        }

        pendingPlayers.entries.forEach { (_, state) ->
            val now = System.currentTimeMillis()
            val targetTime = state.lastSendTime + retryDelaysMs[state.attempt]
            if (now < targetTime) return@forEach

            state.lastSendTime = now
            state.attempt++

            val player = state.player

            if (player.publicKey.isPresent) return@forEach

            BaseVoice.DEBUG_LOGGER.log(
                "Resending player info request to {} (entityId: {}, attempt: {}/{})",
                player.instance.name,
                player.instance.id,
                state.attempt,
                retryDelaysMs.size,
            )
            voiceServer.tcpPacketManager.requestPlayerInfo(player)
        }
    }

    private class PlayerRequestState(
        val player: VoiceServerPlayer,
        var lastSendTime: Long,
        var attempt: Int,
    )
}
