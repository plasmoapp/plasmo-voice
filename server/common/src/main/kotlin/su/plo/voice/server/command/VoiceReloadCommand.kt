package su.plo.voice.server.command

import dev.apehum.mcdsl.command.literalCommand
import su.plo.slib.api.chat.component.McTextComponent

fun voiceReloadCommand(reloadAction: Runnable) =
    literalCommand("vreload") {
        requires(Permission.RELOAD.requirement())

        executes {
            reloadAction.run()

            source.sendFeedback(McTextComponent.translatable("pv.command.reload.message"))
        }
    }
