package su.plo.lib.bungee.command

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.ChatEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.event.EventHandler
import su.plo.lib.api.proxy.MinecraftProxyLib
import su.plo.lib.api.proxy.command.MinecraftProxyCommand
import su.plo.lib.api.proxy.event.command.ProxyCommandExecuteEvent
import su.plo.lib.api.server.command.MinecraftCommandManager
import su.plo.lib.api.server.command.MinecraftCommandSource
import su.plo.lib.bungee.chat.BaseComponentTextConverter

class BungeeCommandManager(
    private val minecraftProxy: MinecraftProxyLib,
    private val textConverter: BaseComponentTextConverter
) : MinecraftCommandManager<MinecraftProxyCommand>(), Listener {

    @EventHandler
    fun onChat(event: ChatEvent) {
        if (!event.isProxyCommand) return

        val command: String = event.message.substringBefore("/")
        val commandSource = getCommandSource(event.sender)
        ProxyCommandExecuteEvent.invoker.onCommandExecute(commandSource, command)
    }

    @Synchronized
    fun registerCommands(plugin: Plugin, proxyServer: ProxyServer) {
        commandByName.forEach { (name: String, command: MinecraftProxyCommand) ->
            proxyServer.pluginManager.registerCommand(plugin, BungeeCommand(this, command, name))
        }
        registered = true
    }

    override fun getCommandSource(source: Any): MinecraftCommandSource {
        require(source is CommandSender) { "source is not ${CommandSender::class.java}" }

        return if (source is ProxiedPlayer) {
            minecraftProxy.getPlayerByInstance(source)
        } else BungeeDefaultCommandSource(source, textConverter)
    }
}
