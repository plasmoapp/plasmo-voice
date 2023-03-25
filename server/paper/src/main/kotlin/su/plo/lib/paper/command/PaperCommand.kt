package su.plo.lib.paper.command

import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import su.plo.lib.api.chat.MinecraftTextComponent
import su.plo.lib.api.server.MinecraftServerLib
import su.plo.lib.api.server.command.MinecraftCommand
import su.plo.lib.api.server.command.MinecraftCommandSource
import su.plo.lib.paper.chat.BaseComponentTextConverter

class PaperCommand(
    private val minecraftServer: MinecraftServerLib,
    private val textConverter: BaseComponentTextConverter,
    private val command: MinecraftCommand,
    name: String
) : Command(name) {

    override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
        val source = getCommandSource(sender)
        if (!command.hasPermission(source, args)) {
            source.sendMessage(MinecraftTextComponent.translatable("pv.error.no_permissions"))
            return true
        }

        command.execute(source, args)
        return true
    }

    @Throws(IllegalArgumentException::class)
    override fun tabComplete(
        sender: CommandSender,
        alias: String,
        args: Array<String>,
        location: Location?
    ): List<String> =
        command.suggest(getCommandSource(sender), args)

    override fun testPermissionSilent(target: CommandSender): Boolean =
        command.hasPermission(getCommandSource(target), null)

    private fun getCommandSource(source: CommandSender): MinecraftCommandSource =
        if (source is Player) minecraftServer.getPlayerByInstance(source)
        else PaperDefaultCommandSource(source, textConverter)
}
