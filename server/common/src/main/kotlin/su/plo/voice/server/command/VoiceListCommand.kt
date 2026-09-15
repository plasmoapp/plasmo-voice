package su.plo.voice.server.command

import dev.apehum.mcdsl.command.literalCommand
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.server.entity.player.McServerPlayer
import su.plo.voice.api.server.player.VoicePlayerManager

fun voiceListCommand(
    playerManager: () -> VoicePlayerManager<*>,
) =
    literalCommand<McBrigadierSource>("vlist") {
        requires { it.source.hasPermission(Permission.LIST) }

        executes {
            val visiblePlayers =
                playerManager().players
                    .filter {
                        (source.source as? McServerPlayer)?.canSee(it.instance) ?: true
                    }

            val playerNamesWithVoice =
                visiblePlayers
                    .filter { it.hasVoiceChat() }
                    .map { it.instance.name }
                    .sorted()
            val totalPlayerCount = visiblePlayers.size

            source.sendFeedback(
                McTextComponent.translatable(
                    "pv.command.list.message",
                    playerNamesWithVoice.size,
                    totalPlayerCount,
                    if (playerNamesWithVoice.isNotEmpty())
                        playerNamesWithVoice.joinToString(", ")
                    else
                        McTextComponent.translatable("pv.command.list.empty")
                )
            )
        }
    }
