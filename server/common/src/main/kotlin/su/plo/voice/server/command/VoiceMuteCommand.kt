package su.plo.voice.server.command

import com.mojang.brigadier.arguments.StringArgumentType
import dev.apehum.mcdsl.command.literalCommand
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.server.command.brigadier.McArgumentTypes
import su.plo.slib.api.server.entity.player.McServerPlayer
import su.plo.voice.api.server.config.ServerConfig
import su.plo.voice.api.server.mute.MuteDurationUnit
import su.plo.voice.server.command.argument.MuteDuration
import su.plo.voice.server.command.argument.MuteDurationType
import su.plo.voice.server.mute.VoiceMuteManager

fun voiceMuteCommand(
    muteManager: () -> VoiceMuteManager,
    config: () -> ServerConfig,
) =
    literalCommand<McBrigadierSource>("vmute") {
        requires { it.source.hasPermission(Permission.MUTE) }

        val target by argument("targets", McArgumentTypes.players())
        val duration by argumentOr("duration", MuteDurationType(), MuteDuration.Permanent)
        val reason by argumentOr("reason", StringArgumentType.greedyString(), "")

        executes {
            val players = target.resolve(this.source)
            if (players.isEmpty()) {
                source.sendFeedback(McTextComponent.translatable("pv.error.player_not_found"))
                return@executes
            }

            val silent = !config().notifications().muted()

            players.forEach { player ->
                mutePlayer(
                    muteManager(),
                    source,
                    player,
                    duration,
                    reason.takeIf { it.isNotBlank() },
                    silent,
                )
            }
        }
    }

private fun mutePlayer(
    muteManager: VoiceMuteManager,
    source: McBrigadierSource,
    target: McServerPlayer,
    duration: MuteDuration,
    reason: String?,
    silent: Boolean,
) {
    if (muteManager.getMute(target.uuid).isPresent) {
        source.sendFeedback(
            McTextComponent.translatable("pv.command.mute.already_muted", target.name)
        )
        return
    }

    val formattedReason = muteManager.formatMuteReason(reason)
    val (durationAmount, durationUnit, feedback) =
        when (duration) {
            MuteDuration.Permanent ->
                Triple(
                    0L,
                    null,
                    McTextComponent.translatable("pv.command.mute.permanently_muted", target.name, formattedReason),
                )

            is MuteDuration.Time -> {
                val amount =
                    if (duration.unit == MuteDurationUnit.TIMESTAMP) duration.duration * 1_000L
                    else duration.duration

                Triple(
                    amount,
                    duration.unit,
                    McTextComponent.translatable(
                        "pv.command.mute.temporarily_muted",
                        target.name,
                        duration.unit.translate(amount),
                        formattedReason,
                    ),
                )
            }
        }

    val mutedBy = (source.source as? McServerPlayer)?.uuid

    muteManager.mute(
        target.uuid,
        mutedBy,
        durationAmount,
        durationUnit,
        reason,
        silent,
    )

    source.sendFeedback(feedback)
}
