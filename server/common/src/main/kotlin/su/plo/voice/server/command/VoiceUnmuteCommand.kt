package su.plo.voice.server.command

import dev.apehum.mcdsl.command.literalCommand
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.entity.player.McGameProfile
import su.plo.slib.api.server.McServerLib
import su.plo.voice.api.server.config.ServerConfig
import su.plo.voice.api.server.mute.MuteManager
import su.plo.voice.server.command.argument.UnmuteTargetsType

fun voiceUnmuteCommand(
    muteManager: () -> MuteManager,
    minecraftServer: () -> McServerLib,
    config: () -> ServerConfig,
) =
    literalCommand<McBrigadierSource>("vunmute") {
        requires { it.source.hasPermission(Permission.UNMUTE) }

        val targets by argument("targets", UnmuteTargetsType(muteManager, minecraftServer))

        executes {
            val profiles = targets.resolve(source)
            if (profiles.isEmpty()) {
                source.sendFeedback(McTextComponent.translatable("pv.error.player_not_found"))
                return@executes
            }

            val silent = !config().notifications().unmuted()

            profiles.forEach {
                unmutePlayer(muteManager(), source, it, silent)
            }
        }
    }

private fun unmutePlayer(
    muteManager: MuteManager,
    source: McBrigadierSource,
    target: McGameProfile,
    silent: Boolean,
) {
    if (!muteManager.unmute(target.id, silent).isPresent) {
        source.sendFeedback(
            McTextComponent.translatable("pv.command.unmute.not_muted", target.name)
        )
        return
    }

    source.sendFeedback(
        McTextComponent.translatable("pv.command.unmute.unmuted", target.name)
    )
}

