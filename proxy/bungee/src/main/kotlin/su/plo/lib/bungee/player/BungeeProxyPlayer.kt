package su.plo.lib.bungee.player

import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.connection.InitialHandler
import net.md_5.bungee.connection.LoginResult
import su.plo.lib.api.chat.MinecraftTextComponent
import su.plo.lib.api.proxy.MinecraftProxyLib
import su.plo.lib.api.proxy.connection.MinecraftProxyServerConnection
import su.plo.lib.api.proxy.player.MinecraftProxyPlayer
import su.plo.lib.bungee.chat.BaseComponentTextConverter
import su.plo.lib.bungee.connection.BungeeProxyServerConnection
import su.plo.voice.proto.data.player.MinecraftGameProfile
import su.plo.voice.server.player.PermissionSupplier
import java.util.*

class BungeeProxyPlayer(
    private val minecraftProxy: MinecraftProxyLib,
    private val textConverter: BaseComponentTextConverter,
    private val permissions: PermissionSupplier,
    private val instance: ProxiedPlayer
) : MinecraftProxyPlayer {

    private var server: BungeeProxyServerConnection? = null

    override fun hasPermission(permission: String) =
        permissions.hasPermission(instance, permission)

    override fun getPermission(permission: String) =
        permissions.getPermission(instance, permission)

    override fun sendMessage(text: MinecraftTextComponent) =
        instance.sendMessage(textConverter.convert(this, text))

    override fun sendActionBar(text: MinecraftTextComponent) =
        instance.sendMessage(ChatMessageType.ACTION_BAR, textConverter.convert(this, text))

    override fun getLanguage() = instance.locale.toString()

    override fun isOnline() = instance.isConnected

    override fun getGameProfile(): MinecraftGameProfile {
        val connection = instance.pendingConnection as InitialHandler
        val gameProfile: LoginResult? = connection.loginProfile

        // todo: <1.19?
        return MinecraftGameProfile(
            connection.uniqueId,
            connection.name,
            gameProfile?.properties?.map {
                MinecraftGameProfile.Property(
                    it.name,
                    it.value,
                    it.signature
                )
            } ?: emptyList()
        )
    }

    override fun getUUID(): UUID = instance.uniqueId

    override fun getName(): String = instance.name

    override fun sendPacket(channel: String, data: ByteArray?) =
        instance.sendData(channel, data)

    override fun kick(reason: MinecraftTextComponent) =
        instance.disconnect(textConverter.convert(this, reason))

    override fun <T : Any?> getInstance(): T = instance as T

    override fun getServer(): Optional<MinecraftProxyServerConnection> {
        if (instance.server == null) {
            server = null
            return Optional.empty()
        }

        val connection = instance.server
        if (server?.instance == connection) return Optional.of(server!!)

        return Optional.of(BungeeProxyServerConnection(minecraftProxy, connection).also {
            server = it
        })
    }
}
