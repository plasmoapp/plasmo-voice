package su.plo.lib.bungee.server

import net.md_5.bungee.api.config.ServerInfo
import su.plo.lib.api.proxy.server.MinecraftProxyServerInfo
import java.net.SocketAddress

class BungeeProxyServerInfo(
    val instance: ServerInfo
) : MinecraftProxyServerInfo {

    override fun getName(): String =
        instance.name

    override fun getAddress(): SocketAddress =
        instance.socketAddress

    override fun getPlayerCount(): Int =
        instance.players.filter { it.isConnected }.size
}
