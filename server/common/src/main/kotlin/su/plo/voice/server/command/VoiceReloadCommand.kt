package su.plo.voice.server.command

import dev.apehum.mcdsl.command.literalCommand
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.command.brigadier.McBrigadierSource

fun voiceReloadCommand(reloadAction: Runnable) =
    literalCommand<McBrigadierSource>("vreload") {
        requires { it.source.hasPermission(Permission.RELOAD) }

        executes {
            reloadAction.run()

            source.sendFeedback(McTextComponent.translatable("pv.command.reload.message"))
        }
    }
