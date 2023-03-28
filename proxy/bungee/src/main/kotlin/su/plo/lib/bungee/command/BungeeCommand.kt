package su.plo.lib.bungee.command

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.TabExecutor
import su.plo.lib.api.chat.MinecraftTextComponent
import su.plo.lib.api.proxy.command.MinecraftProxyCommand

class BungeeCommand(
    private val commandManager: BungeeCommandManager,
    private val command: MinecraftProxyCommand,
    name: String
) : Command(name), TabExecutor {

    override fun execute(sender: CommandSender, arguments: Array<String>) {
        val source = commandManager.getCommandSource(sender)

        if (!command.hasPermission(source, arguments)) {
            source.sendMessage(MinecraftTextComponent.translatable("pv.error.no_permissions"))
            return
        }

        command.execute(source, arguments)
    }

    override fun onTabComplete(sender: CommandSender, arguments: Array<String>): List<String> =
        command.suggest(commandManager.getCommandSource(sender), arguments)

    override fun hasPermission(sender: CommandSender) =
        command.hasPermission(commandManager.getCommandSource(sender), null)
}
