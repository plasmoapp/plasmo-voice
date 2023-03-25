package su.plo.voice.server.audio.line

import com.google.common.collect.Maps
import com.google.common.collect.Sets
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.api.server.audio.line.BaseServerSourceLine
import su.plo.voice.api.server.audio.line.ServerPlayersSet
import su.plo.voice.api.server.audio.line.ServerSourceLinePlayersSets
import su.plo.voice.api.server.event.connection.UdpClientDisconnectedEvent
import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.proto.packets.Packet
import su.plo.voice.proto.packets.tcp.clientbound.SourceLinePlayerAddPacket
import su.plo.voice.proto.packets.tcp.clientbound.SourceLinePlayerRemovePacket
import su.plo.voice.proto.packets.tcp.clientbound.SourceLinePlayersListPacket
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors

class VoiceServerSourceLinePlayersSets(
    private val sourceLine: BaseServerSourceLine
) : ServerSourceLinePlayersSets {

    private val playersSets: MutableMap<UUID, ServerPlayersSet> = Maps.newConcurrentMap()

    override fun setPlayersSet(player: VoicePlayer, playersSet: ServerPlayersSet?) {
        if (playersSet == null) {
            playersSets.remove(player.instance.uuid)
            player.sendPacket(SourceLinePlayersListPacket(sourceLine.id, emptyList()))
            return
        }

        playersSets[player.instance.uuid] = playersSet
        player.sendPacket(SourceLinePlayersListPacket(
            sourceLine.id,
            playersSet.getPlayers()
                .map { it.instance.gameProfile }
        ))
    }

    override fun getPlayersSet(player: VoicePlayer): ServerPlayersSet {
        return playersSets.getOrPut(player.instance.uuid) {
            VoicePlayersSet(player)
        }
    }

    override fun createBroadcastSet(): ServerPlayersSet =
        VoiceBroadcastPlayersSet()

    @EventSubscribe
    fun onPlayerQuit(event: UdpClientDisconnectedEvent) {
        playersSets.remove(event.connection.player.instance.uuid)
    }

    inner class VoiceBroadcastPlayersSet : ServerPlayersSet {

        private val players: MutableSet<VoicePlayer> = Sets.newCopyOnWriteArraySet()

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

        override fun getPlayers(): Collection<VoicePlayer> {
            return players
        }

        private fun broadcast(packet: Packet<*>) {
            players.forEach(Consumer { player: VoicePlayer -> player.sendPacket(packet) })
        }
    }

    inner class VoicePlayersSet(
        private val player: VoicePlayer
    ) : ServerPlayersSet {

        private val players: MutableSet<VoicePlayer> = Sets.newCopyOnWriteArraySet()

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

        override fun getPlayers(): Collection<VoicePlayer> {
            return players
        }
    }
}
