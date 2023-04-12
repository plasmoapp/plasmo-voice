package su.plo.lib.paper.entity

import net.kyori.adventure.text.Component
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.DisplaySlot
import su.plo.lib.api.chat.MinecraftTextComponent
import su.plo.lib.api.server.MinecraftServerLib
import su.plo.lib.api.server.entity.MinecraftServerEntity
import su.plo.lib.api.server.entity.MinecraftServerPlayerEntity
import su.plo.lib.paper.chat.BaseComponentTextConverter
import su.plo.voice.server.player.PermissionSupplier
import java.util.*

class PaperServerPlayer(
    private val loader: JavaPlugin,
    minecraftServer: MinecraftServerLib,
    private val textConverter: BaseComponentTextConverter,
    private val permissions: PermissionSupplier,
    player: Player
) : PaperServerEntity<Player>(minecraftServer, player), MinecraftServerPlayerEntity {

    private var spectatorTarget: MinecraftServerEntity? = null

    override fun isOnline() = instance.isOnline

    override fun getGameProfile() =
        minecraftServer.getGameProfile(instance.uniqueId)
            .orElseThrow { IllegalStateException("Game profile not found") }

    override fun getName() = instance.name

    override fun isSpectator() = instance.gameMode == GameMode.SPECTATOR

    override fun isSneaking() = instance.isSneaking

    override fun hasLabelScoreboard() =
        instance.scoreboard.getObjective(DisplaySlot.BELOW_NAME) != null

    override fun sendMessage(text: MinecraftTextComponent) =
        instance.sendMessage(textConverter.convert(this, text))

    override fun sendMessage(text: String) =
        instance.sendMessage(text)

    override fun sendActionBar(text: String) =
        instance.sendActionBar(Component.text(text))

    override fun sendActionBar(text: MinecraftTextComponent) =
        instance.sendActionBar(textConverter.convert(this, text))

    override fun getLanguage() =
        instance.locale

    override fun hasPermission(permission: String) =
        permissions.hasPermission(instance, permission)

    override fun getPermission(permission: String) =
        permissions.getPermission(instance, permission)

    override fun sendPacket(channel: String, data: ByteArray) {
        if (!isOnline) return
        instance.sendPluginMessage(loader, channel, data)
    }

    override fun kick(reason: MinecraftTextComponent) {
        instance.kickPlayer(textConverter.convert(this, reason).toLegacyText())
    }

    override fun canSee(player: MinecraftServerPlayerEntity) =
        instance.canSee((player as PaperServerPlayer).instance)

    override fun getRegisteredChannels(): Set<String> =
        instance.listeningPluginChannels

    override fun getSpectatorTarget(): Optional<MinecraftServerEntity> {
        if (instance.spectatorTarget == null) {
            spectatorTarget = null
        } else if (spectatorTarget == null ||
            instance.spectatorTarget != spectatorTarget!!.getInstance()
        ) {
            spectatorTarget = minecraftServer.getEntity(instance.spectatorTarget!!)
        }

        return Optional.ofNullable(spectatorTarget)
    }
}
