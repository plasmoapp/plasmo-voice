package su.plo.voice.server.audio.line

import com.google.common.collect.Maps
import com.google.common.collect.Sets
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.api.server.audio.line.BaseServerSourceLine
import su.plo.voice.api.server.audio.line.ServerPlayerSet
import su.plo.voice.api.server.audio.line.ServerPlayerSetManager
import su.plo.voice.api.server.event.connection.UdpClientDisconnectedEvent
import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.proto.packets.Packet
import su.plo.voice.proto.packets.tcp.clientbound.SourceLinePlayerAddPacket
import su.plo.voice.proto.packets.tcp.clientbound.SourceLinePlayerRemovePacket
import su.plo.voice.proto.packets.tcp.clientbound.SourceLinePlayersListPacket
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors

class VoiceServerPlayerSetManager(
    private val sourceLine: BaseServerSourceLine
) : ServerPlayerSetManager {

    private val playerSets: MutableMap<UUID, ServerPlayerSet> = Maps.newConcurrentMap()

    override fun setPlayerSet(player: VoicePlayer, playerSet: ServerPlayerSet?) {
        if (playerSet == null) {
            playerSets.remove(player.instance.uuid)
            player.sendPacket(SourceLinePlayersListPacket(sourceLine.id, emptyList()))
            return
        }

        playerSets[player.instance.uuid] = playerSet
        player.sendPacket(SourceLinePlayersListPacket(
            sourceLine.id,
            playerSet.players
                .map { it.instance.gameProfile }
        ))
    }

    override fun getPlayerSet(player: VoicePlayer): ServerPlayerSet {
        return playerSets.getOrPut(player.instance.uuid) {
            VoicePlayerSet(player)
        }
    }

    override fun createBroadcastSet(): ServerPlayerSet =
        VoiceBroadcastPlayerSet()

    @EventSubscribe
    fun onPlayerQuit(event: UdpClientDisconnectedEvent) {
        playerSets.remove(event.connection.player.instance.uuid)
    }

    inner class VoiceBroadcastPlayerSet : ServerPlayerSet {

        override val players: MutableSet<VoicePlayer> = Sets.newCopyOnWriteArraySet()

        override fun addPlayer(player: VoicePlayer) {
            if (players.contains(player)) return

            broadcast(SourceLinePlayerAddPacket(sourceLine.id, player.instance.gameProfile))
            players.add(player)
            player.sendPacket(SourceLinePlayersListPacket(
                sourceLine.id,
                players.stream()
                    .map { it.instance.gameProfile }
                    .collect(Collectors.toList())
            ))
        }

        override fun removePlayer(playerId: UUID): Boolean {
            return players.stream()
                .filter { player: VoicePlayer ->
                    player.instance.uuid == playerId
                }
                .findFirst()
                .map { player ->
                    broadcast(SourceLinePlayerRemovePacket(sourceLine.id, playerId))
                    players.remove(player)
                    true
                }
                .orElse(false)
        }

        override fun clearPlayers() {
            players.forEach(Consumer { player: VoicePlayer ->
                player.sendPacket(
                    SourceLinePlayersListPacket(sourceLine.id, emptyList())
                )
            })
            players.clear()
        }

        private fun broadcast(packet: Packet<*>) {
            players.forEach(Consumer { player: VoicePlayer -> player.sendPacket(packet) })
        }
    }

    inner class VoicePlayerSet(
        private val player: VoicePlayer
    ) : ServerPlayerSet {

        override val players: MutableSet<VoicePlayer> = Sets.newCopyOnWriteArraySet()

        override fun addPlayer(player: VoicePlayer) {
            players.add(player)
            player.sendPacket(SourceLinePlayerAddPacket(sourceLine.id, player.instance.gameProfile))
        }

        override fun removePlayer(playerId: UUID): Boolean {
            return players.stream()
                .filter { player: VoicePlayer ->
                    player.instance.uuid == playerId
                }
                .findFirst()
                .map { player ->
                    player.sendPacket(SourceLinePlayerRemovePacket(sourceLine.id, playerId))
                    players.remove(player)

                    true
                }
                .orElse(false)
        }

        override fun clearPlayers() {
            player.sendPacket(SourceLinePlayersListPacket(sourceLine.id))
            players.clear()
        }
    }
}
