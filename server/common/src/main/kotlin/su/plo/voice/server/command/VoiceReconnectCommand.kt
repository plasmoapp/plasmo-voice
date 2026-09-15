package su.plo.voice.server.command

import dev.apehum.mcdsl.command.literalCommand
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.server.entity.player.McServerPlayer
import su.plo.voice.api.server.connection.TcpServerPacketManager
import su.plo.voice.api.server.connection.UdpServerConnectionManager
import su.plo.voice.api.server.event.connection.UdpClientDisconnectedEvent
import su.plo.voice.api.server.player.VoicePlayerManager
import su.plo.voice.api.server.player.VoiceServerPlayer

fun voiceReconnectCommand(
    playerManager: () -> VoicePlayerManager<VoiceServerPlayer>,
    udpConnectionManager: () -> UdpServerConnectionManager,
    tcpPacketManager: () -> TcpServerPacketManager,
) =
    literalCommand<McBrigadierSource>("vrc") {
        requires { it.source.hasPermission(Permission.RECONNECT) }

        executes {
            val player = source.executor as? McServerPlayer
                ?: run {
                    source.sendFeedback(McTextComponent.translatable("pv.error.player_only_command"))
                    return@executes
                }

            val voicePlayer = playerManager().getPlayerById(player.uuid)
                .orElseThrow { IllegalArgumentException("Player ${player.name} not found") }

            source.sendFeedback(McTextComponent.translatable("pv.command.reconnect.message"))
            udpConnectionManager().removeConnection(voicePlayer, UdpClientDisconnectedEvent.Reason.RECONNECT)
            tcpPacketManager().requestPlayerInfo(voicePlayer)
        }
    }
