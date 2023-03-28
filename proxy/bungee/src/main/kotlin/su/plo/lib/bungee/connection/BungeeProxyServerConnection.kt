package su.plo.lib.bungee.connection

import net.md_5.bungee.api.connection.Server
import su.plo.lib.api.proxy.MinecraftProxyLib
import su.plo.lib.api.proxy.connection.MinecraftProxyServerConnection

class BungeeProxyServerConnection(
    private val minecraftProxy: MinecraftProxyLib,
    val instance: Server
) : MinecraftProxyServerConnection {

    override fun sendPacket(channel: String, data: ByteArray) =
        instance.sendData(channel, data)

    override fun getServerInfo() =
        minecraftProxy.getServerInfoByServerInstance(instance.info)
}
