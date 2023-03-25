package su.plo.lib.paper.command

import org.bukkit.command.CommandSender
import su.plo.lib.api.chat.MinecraftTextComponent
import su.plo.lib.api.server.command.MinecraftCommandSource
import su.plo.lib.api.server.permission.PermissionTristate
import su.plo.lib.paper.chat.BaseComponentTextConverter

class PaperDefaultCommandSource(
    private val source: CommandSender,
    private val textConverter: BaseComponentTextConverter
) : MinecraftCommandSource {

    override fun sendMessage(text: MinecraftTextComponent) =
        source.sendMessage(textConverter.convert(this, text)!!)

    override fun sendMessage(text: String) =
        source.sendMessage(text)

    override fun sendActionBar(text: String) =
        source.sendMessage(text)

    override fun sendActionBar(text: MinecraftTextComponent) =
        source.sendMessage(textConverter.convert(this, text))

    override fun getLanguage(): String = "en_us"

    override fun hasPermission(permission: String): Boolean = true

    override fun getPermission(permission: String): PermissionTristate = PermissionTristate.FALSE
}
