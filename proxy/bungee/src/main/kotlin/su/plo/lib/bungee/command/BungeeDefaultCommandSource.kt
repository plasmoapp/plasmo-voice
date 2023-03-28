package su.plo.lib.bungee.command

import net.md_5.bungee.api.CommandSender
import su.plo.lib.api.chat.MinecraftTextComponent
import su.plo.lib.api.server.command.MinecraftCommandSource
import su.plo.lib.api.server.permission.PermissionTristate
import su.plo.lib.bungee.chat.BaseComponentTextConverter

class BungeeDefaultCommandSource(
    private val source: CommandSender,
    private val textConverter: BaseComponentTextConverter
) : MinecraftCommandSource {

    override fun sendMessage(text: MinecraftTextComponent) {
        source.sendMessage(textConverter.convert(this, text))
    }

    override fun sendMessage(text: String) {
        source.sendMessage(text)
    }

    override fun sendActionBar(text: String) {
        /* do nothing */
    }

    override fun sendActionBar(text: MinecraftTextComponent) {
        /* do nothing */
    }

    override fun getLanguage() = "en_us"

    override fun hasPermission(permission: String) =
        source.hasPermission(permission)

    override fun getPermission(permission: String) =
        if (source.permissions.contains(permission)) {
            if (source.hasPermission(permission)) PermissionTristate.TRUE
            else PermissionTristate.FALSE
        } else PermissionTristate.UNDEFINED
}
