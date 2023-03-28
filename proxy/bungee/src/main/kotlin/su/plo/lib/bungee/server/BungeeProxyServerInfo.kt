package su.plo.lib.bungee.server

import net.md_5.bungee.api.config.ServerInfo
import su.plo.lib.api.proxy.server.MinecraftProxyServerInfo

class BungeeProxyServerInfo(
    val instance: ServerInfo
) : MinecraftProxyServerInfo {

    override fun getName(): String =
        instance.name
}
