package su.plo.voice.server.audio.capture

import com.google.common.collect.Maps
import lombok.Getter
import lombok.RequiredArgsConstructor
import su.plo.voice.api.event.EventPriority
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.api.server.PlasmoBaseVoiceServer
import su.plo.voice.api.server.audio.capture.ServerActivation
import su.plo.voice.api.server.audio.source.ServerAudioSource
import su.plo.voice.api.server.event.audio.source.PlayerSpeakEndEvent
import su.plo.voice.api.server.event.audio.source.PlayerSpeakEvent
import su.plo.voice.api.server.event.audio.source.ServerSourceAudioPacketEvent
import su.plo.voice.api.server.event.audio.source.ServerSourcePacketEvent
import su.plo.voice.api.server.event.connection.UdpClientDisconnectedEvent
import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.proto.data.audio.source.SelfSourceInfo
import su.plo.voice.proto.data.audio.source.SourceInfo
import su.plo.voice.proto.packets.tcp.clientbound.SelfSourceInfoPacket
import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket
import su.plo.voice.proto.packets.tcp.clientbound.SourceInfoPacket
import su.plo.voice.proto.packets.udp.clientbound.SelfAudioInfoPacket
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.jvm.optionals.getOrNull

@RequiredArgsConstructor
class SelfActivationHelper(
    private val voiceServer: PlasmoBaseVoiceServer
) {

    private val lastPlayerActivationIds: MutableMap<UUID, UUID> = Maps.newConcurrentMap()

    @Getter
    private val sourceIdToPlayerId: MutableMap<UUID, UUID> = Maps.newConcurrentMap()

    @Getter
    private val playerIdToSourceIds: MutableMap<UUID, MutableSet<UUID>> = Maps.newConcurrentMap()

    fun cleanupPlayer(playerId: UUID) {
        lastPlayerActivationIds.remove(playerId)
        playerIdToSourceIds.remove(playerId)?.let { playerSources ->
            playerSources.forEach { sourceIdToPlayerId.remove(it) }
        }
    }

    fun sendAudioInfo(
        player: VoicePlayer,
        source: ServerAudioSource<*>,
        playerPacket: PlayerAudioPacket,
        sourcePacket: SourceAudioPacket
    ) = sendAudioInfo(player, source, playerPacket.activationId, sourcePacket, playerPacket.data.size != sourcePacket.data.size)

    fun sendAudioInfo(
        player: VoicePlayer,
        source: ServerAudioSource<*>,
        activationId: UUID,
        packet: SourceAudioPacket,
        dataChanged: Boolean
    ) {
        sourceIdToPlayerId[source.id] = player.instance.uuid

        playerIdToSourceIds.computeIfAbsent(player.instance.uuid) { _ -> CopyOnWriteArraySet() }
            .also { it.add(source.id) }

        val lastActivationId = lastPlayerActivationIds.put(player.instance.uuid, activationId)
        if (lastActivationId == null || lastActivationId != activationId) {
            updateSelfSourceInfo(player, source, null)
        }

        voiceServer.udpConnectionManager.getConnectionByPlayerId(player.instance.uuid)
            .ifPresent { connection ->
                connection.sendPacket(
                    SelfAudioInfoPacket(
                        source.id,
                        packet.sequenceNumber,
                        if (dataChanged) packet.data else null,
                        packet.distance
                    )
                )
            }
    }

    fun updateSelfSourceInfo(
        player: VoicePlayer,
        source: ServerAudioSource<*>,
        sourceInfo: SourceInfo?
    ) {
        val lastActivationId = lastPlayerActivationIds[player.instance.uuid] ?: return

        player.sendPacket(
            SelfSourceInfoPacket(
                SelfSourceInfo(
                    sourceInfo ?: source.sourceInfo,
                    player.instance.uuid,
                    lastActivationId,
                    -1L
                )
            )
        )
    }

    @EventSubscribe(priority = EventPriority.HIGHEST)
    fun onSourceAudioPacket(event: ServerSourceAudioPacketEvent) {
        val (player, audioPacket) = event.activationInfo ?: return

        val source = event.source
        val packet = event.packet

        sendAudioInfo(player, source, audioPacket, packet)
    }

    @EventSubscribe(priority = EventPriority.HIGHEST)
    fun onSourceSendPacket(event: ServerSourcePacketEvent) {
        val source = event.source
        val packet = event.packet

        val playerId = sourceIdToPlayerId[source.id]
            ?: return

        if (!lastPlayerActivationIds.containsKey(playerId)) return

        val player = voiceServer.playerManager.getPlayerById(playerId, false).getOrNull()
            ?: return cleanupPlayer(playerId)

        if (packet is SourceInfoPacket) {
            updateSelfSourceInfo(
                player,
                source,
                packet.sourceInfo
            )
        } else if (packet is SourceAudioEndPacket) {
            player.sendPacket(
                SourceAudioEndPacket(source.id, packet.sequenceNumber)
            )
        }
    }

    @EventSubscribe
    fun onClientDisconnected(event: UdpClientDisconnectedEvent) =
        cleanupPlayer(event.connection.player.instance.uuid)
}
