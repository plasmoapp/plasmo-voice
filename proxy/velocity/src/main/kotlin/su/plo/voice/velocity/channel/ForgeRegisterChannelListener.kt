package su.plo.voice.velocity.channel

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.PlayerChannelRegisterEvent
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import su.plo.slib.api.entity.player.McPlayer
import su.plo.slib.api.event.player.McPlayerQuitEvent
import java.util.*
import kotlin.jvm.optionals.getOrNull

// https://github.com/PaperMC/Velocity/pull/591
// After this PR was merged, Forge clients are now broken
// Forge only sends channels on login state (or configuration state on 1.20.2+) for some reason
// (or I'm doing something wrong)
class ForgeRegisterChannelListener {

    private val forgePlayersWithPlasmoVoice: MutableSet<UUID> = HashSet()
    private val installedChannel = MinecraftChannelIdentifier.create("plasmo", "voice/v2/installed")

    private val forgeChannels = listOf(
        MinecraftChannelIdentifier.create("forge", "handshake"),
        MinecraftChannelIdentifier.create("fml", "handshake")
    )

    private val minecraftRegisterChannel = MinecraftChannelIdentifier.forDefaultNamespace("register")

    init {
        McPlayerQuitEvent.registerListener(this::onPlayerQuit)
    }

    @Subscribe(order = PostOrder.FIRST)
    fun onChannelRegister(event: PlayerChannelRegisterEvent) {
        val player = event.player
        val channels = event.channels

        if (!channels.any { it in forgeChannels }) return
        if (installedChannel !in channels) return

        forgePlayersWithPlasmoVoice.add(player.uniqueId)
    }

    @Subscribe
    fun onServerSwitch(event: ServerPostConnectEvent) {
        if (event.previousServer == null) return

        val player = event.player
        if (player.uniqueId !in forgePlayersWithPlasmoVoice) return

        player.currentServer.getOrNull()?.sendPluginMessage(
            minecraftRegisterChannel,
            installedChannel.id.toByteArray(Charsets.UTF_8)
        )
    }

    private fun onPlayerQuit(player: McPlayer) {
        forgePlayersWithPlasmoVoice.remove(player.uuid)
    }
}
