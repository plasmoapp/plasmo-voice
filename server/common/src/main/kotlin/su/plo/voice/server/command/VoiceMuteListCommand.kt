package su.plo.voice.server.command

import dev.apehum.mcdsl.command.literalCommand
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.server.McServerLib
import su.plo.voice.api.server.language.ServerLanguages
import su.plo.voice.server.mute.VoiceMuteManager
import java.text.SimpleDateFormat
import java.util.Date

fun voiceMuteListCommand(
    muteManager: () -> VoiceMuteManager,
    minecraftServer: () -> McServerLib,
    languages: () -> ServerLanguages,
) =
    literalCommand<McBrigadierSource>("vmutelist") {
        requires { it.source.hasPermission(Permission.MUTE_LIST) }

        executes {
            val muteManager = muteManager()
            val minecraftServer = minecraftServer()
            val mutedPlayers = muteManager.muteStorage.mutedPlayers

            source.sendFeedback(McTextComponent.translatable("pv.command.mute_list.header"))
            if (mutedPlayers.isEmpty()) {
                source.sendFeedback(McTextComponent.translatable("pv.command.mute_list.empty"))
                return@executes
            }

            mutedPlayers.forEach { muteInfo ->
                val player = minecraftServer.getGameProfile(muteInfo.playerUUID) ?: return@forEach
                val mutedBy = muteInfo.mutedByPlayerUUID?.let { minecraftServer.getGameProfile(it) }

                val language = languages().getServerLanguage(source.source)

                val date = Date(muteInfo.mutedToTime)
                val expirationFormatDate = SimpleDateFormat(
                    language.getOrDefault("pv.command.mute_list.expiration_date", "yyyy.MM.dd")
                )
                val expirationFormatTime = SimpleDateFormat(
                    language.getOrDefault("pv.command.mute_list.expiration_time", "HH:mm:ss")
                )

                val expires =
                    if (muteInfo.mutedToTime > 0)
                        McTextComponent.translatable(
                            "pv.command.mute_list.expire_at",
                            expirationFormatDate.format(date),
                            expirationFormatTime.format(date)
                        )
                    else
                        McTextComponent.translatable("pv.command.mute_list.never_expires")

                val reason = muteManager.formatMuteReason(muteInfo.reason)

                if (mutedBy != null) {
                    source.sendFeedback(
                        McTextComponent.translatable(
                            "pv.command.mute_list.entry_muted_by",
                            player.name,
                            mutedBy.name,
                            expires,
                            reason
                        )
                    )
                } else {
                    source.sendFeedback(
                        McTextComponent.translatable(
                            "pv.command.mute_list.entry",
                            player.name,
                            expires,
                            reason
                        )
                    )
                }
            }
        }
    }
