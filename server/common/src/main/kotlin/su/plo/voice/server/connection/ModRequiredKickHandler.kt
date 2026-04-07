package su.plo.voice.server.connection

import com.google.common.collect.Maps
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.entity.player.McPlayer
import su.plo.slib.api.event.player.McPlayerJoinEvent
import su.plo.slib.api.event.player.McPlayerQuitEvent
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.api.server.event.connection.TcpPacketReceivedEvent
import su.plo.voice.server.BaseVoiceServer
import java.util.UUID
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class ModRequiredKickHandler(
    private val voiceServer: BaseVoiceServer,
) {

    private val kickFutures: MutableMap<UUID, ScheduledFuture<*>> = Maps.newConcurrentMap()

    init {
        McPlayerJoinEvent.registerListener(::handlePlayerJoin)
        McPlayerQuitEvent.registerListener(::handlePlayerQuit)
    }

    @EventSubscribe
    fun TcpPacketReceivedEvent.onTcpPacketReceived() {
        cancelKickFuture(player.instance.uuid)
    }

    fun clear() {
        McPlayerJoinEvent.unregisterListener(::handlePlayerJoin)
        McPlayerQuitEvent.unregisterListener(::handlePlayerQuit)

        kickFutures.values.forEach { it.cancel(false) }
        kickFutures.clear()
    }

    private fun handlePlayerJoin(player: McPlayer) {
        if (!voiceServer.udpServer.isPresent || voiceServer.config == null) return
        if (!shouldKick(player)) return

        cancelKickFuture(player.uuid)

        val timeoutMs = voiceServer.config!!.voice().clientModRequiredCheckTimeoutMs()
        kickFutures[player.uuid] = voiceServer.backgroundExecutor.schedule(
            {
                voiceServer.minecraftServer.executeInMainThread {
                    player.kick(McTextComponent.translatable("pv.error.mod_missing_kick_message"))
                }
            },
            timeoutMs,
            TimeUnit.MILLISECONDS,
        )
    }

    private fun handlePlayerQuit(player: McPlayer) {
        cancelKickFuture(player.uuid)
    }

    private fun shouldKick(player: McPlayer): Boolean =
        voiceServer.config!!.voice().clientModRequired() && !player.hasPermission("pv.bypass_mod_requirement")

    private fun cancelKickFuture(playerId: UUID) {
        kickFutures.remove(playerId)?.cancel(false)
    }
}
