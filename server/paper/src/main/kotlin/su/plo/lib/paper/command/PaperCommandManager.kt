package su.plo.lib.paper.command

import org.bukkit.command.CommandSender
import org.bukkit.command.SimpleCommandMap
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import su.plo.lib.api.server.MinecraftServerLib
import su.plo.lib.api.server.command.MinecraftCommand
import su.plo.lib.api.server.command.MinecraftCommandManager
import su.plo.lib.api.server.command.MinecraftCommandSource
import su.plo.lib.paper.chat.BaseComponentTextConverter

class PaperCommandManager(
    private val minecraftServer: MinecraftServerLib,
    private val textConverter: BaseComponentTextConverter
) : MinecraftCommandManager<MinecraftCommand>() {

    @Synchronized
    fun registerCommands(loader: JavaPlugin) {
        commandByName.forEach { (name, command) ->
            val paperCommand = PaperCommand(this, command, name)

            val commandMap = loader.server.javaClass
                .getDeclaredField("commandMap").also {
                    it.isAccessible = true
                }
                .get(loader.server) as SimpleCommandMap

            commandMap.register("plasmovoice", paperCommand)
        }

        registered = true
    }

    override fun getCommandSource(source: Any): MinecraftCommandSource  {
        require(source is CommandSender) { "source is not ${CommandSender::class.java}" }

        return if (source is Player) minecraftServer.getPlayerByInstance(source)
        else PaperDefaultCommandSource(source, textConverter)
    }
}
